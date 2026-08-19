import { ChangeDetectionStrategy, Component, model } from '@angular/core';

import { GENDERS, Gender } from '../core/models';

/** Switch between the scoring classes, shared by ranking and boulder points. */
@Component({
  selector: 'app-gender-tabs',
  templateUrl: './gender-tabs.html',
  styleUrl: './gender-tabs.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenderTabs {
  readonly gender = model.required<Gender>();

  protected readonly options = GENDERS;
}
