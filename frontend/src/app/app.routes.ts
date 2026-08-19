import { Routes } from '@angular/router';

import { authGuard, guestGuard } from './core/auth.guard';
import { Dashboard } from './dashboard/dashboard';
import { Landing } from './landing/landing';

export const routes: Routes = [
  { path: '', component: Landing, canActivate: [guestGuard] },
  { path: 'me', component: Dashboard, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
