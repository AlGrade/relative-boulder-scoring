import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';

export interface TabOption<T extends string> {
  readonly id: T;
  readonly label: string;
}

/**
 * The one switcher: login vs. registration, the dashboard views, the scoring class.
 *
 * These are plain toggle buttons carrying aria-pressed, not role="tab". A real tab
 * widget owes assistive technology a role="tabpanel" it controls and arrow-key
 * navigation between the tabs; announcing "tab" without either is worse than not
 * claiming the role at all.
 */
@Component({
  selector: 'app-tabs',
  templateUrl: './tabs.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Tabs<T extends string> {
  readonly options = input.required<readonly TabOption<T>[]>();
  readonly selected = model.required<T>();
  /** Names the group, so a screen reader says what is being switched. */
  readonly label = input.required<string>();
}
