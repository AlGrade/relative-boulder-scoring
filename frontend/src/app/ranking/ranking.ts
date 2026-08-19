import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

import { Api } from '../core/api';
import { GENDERS, Gender, RankingEntry } from '../core/models';
import { Loading } from '../loading/loading';
import { Tabs } from '../tabs/tabs';

@Component({
  selector: 'app-ranking',
  imports: [Loading, Tabs],
  templateUrl: './ranking.html',
  styleUrl: './ranking.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Ranking {
  protected readonly genders = GENDERS;
  protected readonly gender = signal<Gender>('MALE');

  // Reloads whenever the scoring class changes.
  protected readonly entries = httpResource<RankingEntry[]>(() => Api.ranking(this.gender()), {
    defaultValue: [],
  });
}
