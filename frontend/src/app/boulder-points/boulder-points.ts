import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

import { Api } from '../core/api';
import { BoulderPoints as BoulderPointsEntry, GENDERS, Gender } from '../core/models';
import { Loading } from '../loading/loading';
import { Tabs } from '../tabs/tabs';

@Component({
  selector: 'app-boulder-points',
  imports: [Loading, Tabs],
  templateUrl: './boulder-points.html',
  styleUrl: './boulder-points.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoulderPoints {
  protected readonly genders = GENDERS;
  protected readonly gender = signal<Gender>('MALE');

  protected readonly points = httpResource<BoulderPointsEntry[]>(
    () => Api.boulderPoints(this.gender()),
    { defaultValue: [] },
  );
}
