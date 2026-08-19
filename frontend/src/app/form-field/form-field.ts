import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Label, control and validation message as one unit, so no field can end up with a
 * differently shaped error or forget to show one. The control itself is projected:
 * the parent stays in charge of its type, autocomplete and placeholder.
 */
@Component({
  selector: 'app-form-field',
  templateUrl: './form-field.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormField {
  readonly label = input.required<string>();
  readonly error = input<string | null>(null);
}
