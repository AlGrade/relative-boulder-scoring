import { ChangeDetectionStrategy, Component, model } from '@angular/core';

import { GENDERS, Gender } from '../core/models';

/** Umschalter zwischen den Wertungsklassen, geteilt von Ranking und Boulderwerten. */
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
