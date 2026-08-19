import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

import { Api } from '../core/api';
import { BoulderPoints as BoulderPointsEntry, Gender } from '../core/models';
import { GenderTabs } from '../gender-tabs/gender-tabs';

@Component({
  selector: 'app-boulder-points',
  imports: [GenderTabs],
  templateUrl: './boulder-points.html',
  styleUrl: './boulder-points.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoulderPoints {
  protected readonly gender = signal<Gender>('MALE');

  protected readonly points = httpResource<BoulderPointsEntry[]>(
    () => Api.boulderPoints(this.gender()),
    { defaultValue: [] },
  );
}
