import { HttpErrorResponse } from '@angular/common/http';

import { Gender } from './models';

/** Every endpoint in one place - the paths are relative so the dev proxy kicks in. */
export const Api = {
  register: '/api/auth/register',
  login: '/api/auth/login',
  logout: '/api/auth/logout',
  me: '/api/auth/me',
  competition: '/api/competition',
  boulders: '/api/boulders',
  myAscents: '/api/me/ascents',
  ascent: (boulderNumber: number) => `/api/me/ascents/${boulderNumber}`,
  ranking: (gender: Gender) => `/api/ranking?gender=${gender}`,
  boulderPoints: (gender: Gender) => `/api/boulder-points?gender=${gender}`,
} as const;

/** Pulls the plain text out of the backend's ProblemDetail, otherwise the fallback. */
export function errorMessage(error: unknown, fallback: string): string {
  const detail = error instanceof HttpErrorResponse ? error.error?.detail : null;
  return typeof detail === 'string' && detail.length > 0 ? detail : fallback;
}
