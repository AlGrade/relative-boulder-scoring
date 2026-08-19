import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

import { Api } from '../core/api';
import { Gender, RankingEntry } from '../core/models';
import { GenderTabs } from '../gender-tabs/gender-tabs';

@Component({
  selector: 'app-ranking',
  imports: [GenderTabs],
  templateUrl: './ranking.html',
  styleUrl: './ranking.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Ranking {
  protected readonly gender = signal<Gender>('MALE');

  // Laedt bei jedem Wechsel der Wertungsklasse neu.
  protected readonly entries = httpResource<RankingEntry[]>(() => Api.ranking(this.gender()), {
    defaultValue: [],
  });
}
