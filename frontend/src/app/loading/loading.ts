import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * The single loading indicator used everywhere something is on its way. Its styles
 * live in styles.scss because index.html renders the same markup before Angular has
 * booted, and component styles would not reach it there.
 */
@Component({
  selector: 'app-loading',
  templateUrl: './loading.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Loading {}
