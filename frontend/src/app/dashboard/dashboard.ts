import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { BoulderList } from '../boulder-list/boulder-list';
import { BoulderPoints } from '../boulder-points/boulder-points';
import { Ranking } from '../ranking/ranking';

type Tab = 'boulders' | 'ranking' | 'points';

@Component({
  selector: 'app-dashboard',
  imports: [BoulderList, BoulderPoints, Ranking],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Dashboard {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly tab = signal<Tab>('boulders');
  protected readonly firstName = computed(() => this.auth.competitor()?.name.split(' ')[0] ?? '');

  protected readonly tabs: readonly { id: Tab; label: string }[] = [
    { id: 'boulders', label: 'Boulder' },
    { id: 'ranking', label: 'Ranking' },
    { id: 'points', label: 'Punkte' },
  ];

  protected logout(): void {
    this.auth.logout().subscribe(() => this.router.navigateByUrl('/'));
  }
}
