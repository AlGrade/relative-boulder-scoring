import { HttpClient, httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

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
  protected readonly ascents = httpResource<Ascent[]>(() => Api.myAscents, { defaultValue: [] });
  protected readonly pending = signal(false);

  private readonly byNumber = computed(
    () => new Map(this.ascents.value().map((ascent) => [ascent.boulderNumber, ascent])),
  );

  protected isSent(boulderNumber: number): boolean {
    return this.byNumber().has(boulderNumber);
  }

  protected isFlashed(boulderNumber: number): boolean {
    return this.byNumber().get(boulderNumber)?.flashed ?? false;
  }

  /** Begehung zurücknehmen löscht auch einen gesetzten Flash — der hängt daran. */
  protected toggleSent(boulderNumber: number): void {
    this.send(
      this.isSent(boulderNumber)
        ? this.http.delete<void>(Api.ascent(boulderNumber))
        : this.http.put<void>(Api.ascent(boulderNumber), { flashed: false }),
    );
  }

  /** Ein Flash legt die Begehung mit an, ein Un-Flash lässt sie stehen. */
  protected toggleFlashed(boulderNumber: number): void {
    this.send(
      this.http.put<void>(Api.ascent(boulderNumber), { flashed: !this.isFlashed(boulderNumber) }),
    );
  }

  private send(request: Observable<void>): void {
    if (this.pending()) {
      return;
    }
    this.pending.set(true);
    request.subscribe({
      next: () => this.ascents.reload(),
      error: () => this.pending.set(false),
      complete: () => this.pending.set(false),
    });
  }
}
