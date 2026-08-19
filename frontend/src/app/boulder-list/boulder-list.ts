import { HttpClient, httpResource } from '@angular/common/http';
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

@Component({
  selector: 'app-boulder-list',
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
      .subscribe({ error: () => undefined });
  }
}
