import { provideHttpClient, withFetch } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { AuthService } from './core/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Angular sends the value of the XSRF-TOKEN cookie as X-XSRF-TOKEN by itself,
    // which is exactly what Spring Security expects.
    provideHttpClient(withFetch()),
    // Resolve the session once before the first route activates. After that the
    // guards can decide synchronously, and the CSRF cookie is in place.
    provideAppInitializer(() => inject(AuthService).loadCurrent()),
  ],
};
