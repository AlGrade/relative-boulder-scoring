import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/**
 * Both guards may decide synchronously: the session was resolved at app start
 * (see `provideAppInitializer` in `app.config.ts`).
 */
export const authGuard: CanActivateFn = () =>
  inject(AuthService).isLoggedIn() || inject(Router).createUrlTree(['/']);

export const guestGuard: CanActivateFn = () =>
  !inject(AuthService).isLoggedIn() || inject(Router).createUrlTree(['/me']);
