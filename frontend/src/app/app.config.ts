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
    // Angular schickt den Wert des XSRF-TOKEN-Cookies automatisch als X-XSRF-TOKEN
    // mit — passend zu dem, was Spring Security erwartet.
    provideHttpClient(withFetch()),
    // Session einmal aufloesen, bevor die erste Route aktiviert wird. Danach koennen
    // die Guards synchron entscheiden, und das CSRF-Cookie steht.
    provideAppInitializer(() => inject(AuthService).loadCurrent()),
  ],
};
