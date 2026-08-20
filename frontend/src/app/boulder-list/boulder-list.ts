import { HttpClient, HttpErrorResponse, httpResource } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  linkedSignal,
  signal,
} from '@angular/core';
import { Observable, finalize } from 'rxjs';

import { Api } from '../core/api';
import { Ascent, Boulder } from '../core/models';
import { Loading } from '../loading/loading';

@Component({
  selector: 'app-boulder-list',
  imports: [Loading],
  templateUrl: './boulder-list.html',
  styleUrl: './boulder-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoulderList {
  private readonly http = inject(HttpClient);

  protected readonly boulders = httpResource<Boulder[]>(() => Api.boulders, { defaultValue: [] });

  private readonly serverAscents = httpResource<Ascent[]>(() => Api.myAscents, {
    defaultValue: [],
  });

  /**
   * Local copy that a tap updates right away, so a button flips without waiting for
   * the round trip. Every server answer overwrites it again.
   */
  private readonly ascents = linkedSignal(() => this.serverAscents.value());

  private readonly inFlight = signal(0);
  /** A failed save is the one error a competitor must not miss in the gym. */
  protected readonly failure = signal('');

  private readonly byNumber = computed(
    () => new Map(this.ascents().map((ascent) => [ascent.boulderNumber, ascent])),
  );

  protected isSent(boulderNumber: number): boolean {
    return this.byNumber().has(boulderNumber);
  }

  protected isFlashed(boulderNumber: number): boolean {
    return this.byNumber().get(boulderNumber)?.flashed ?? false;
  }

  /** Taking the ascent back also drops a flash, which hangs off it. */
  protected toggleSent(boulderNumber: number): void {
    if (this.isSent(boulderNumber)) {
      this.apply(boulderNumber, null);
      this.send(this.http.delete<void>(Api.ascent(boulderNumber)));
    } else {
      this.apply(boulderNumber, { boulderNumber, flashed: false });
      this.send(this.http.put<void>(Api.ascent(boulderNumber), { flashed: false }));
    }
  }

  /** A flash creates the ascent as well; un-flashing leaves it in place. */
  protected toggleFlashed(boulderNumber: number): void {
    const flashed = !this.isFlashed(boulderNumber);
    this.apply(boulderNumber, { boulderNumber, flashed });
    this.send(this.http.put<void>(Api.ascent(boulderNumber), { flashed }));
  }

  private apply(boulderNumber: number, ascent: Ascent | null): void {
    this.ascents.update((current) => {
      const others = current.filter((entry) => entry.boulderNumber !== boulderNumber);
      return ascent ? [...others, ascent] : others;
    });
  }

  /**
   * The optimistic state is a guess; the reload confirms it, or puts the real state
   * back if the request failed. Only the last request still in flight reloads, so a
   * late answer cannot overwrite a tap that is still on its way.
   */
  private send(request: Observable<void>): void {
    this.failure.set('');
    this.inFlight.update((count) => count + 1);
    request
      .pipe(
        finalize(() => {
          this.inFlight.update((count) => count - 1);
          if (this.inFlight() === 0) {
            this.serverAscents.reload();
          }
        }),
      )
      // The reload above puts the real state back; this only says why it jumped.
      // A 403 can only mean the window has closed, and saying so is worth more
      // than "try again" - trying again will not help. Anything else really is
      // worth another tap, so it must not claim the competition is over.
      .subscribe({
        error: (cause: unknown) =>
          this.failure.set(
            cause instanceof HttpErrorResponse && cause.status === 403
              ? 'Der Wettkampf ist schon vorbei, logging nicht mehr möglich.'
              : 'Die letzte Änderung wurde nicht gespeichert. Bitte noch einmal tippen.',
          ),
      });
  }
}
