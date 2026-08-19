import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of, tap } from 'rxjs';

import { Api } from './api';
import { Competitor, Gender } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly current = signal<Competitor | null>(null);

  readonly competitor = this.current.asReadonly();
  readonly isLoggedIn = computed(() => this.current() !== null);

  /**
   * Once at app start: is there a session? A 401 simply means "not logged in".
   * The call also sets the CSRF cookie for the first writing request.
   */
  loadCurrent(): Observable<Competitor | null> {
    return this.http.get<Competitor>(Api.me).pipe(
      catchError(() => of(null)),
      tap((competitor) => this.current.set(competitor)),
    );
  }

  register(name: string, gender: Gender, password: string): Observable<Competitor> {
    return this.http
      .post<Competitor>(Api.register, { name, gender, password })
      .pipe(tap((competitor) => this.current.set(competitor)));
  }

  login(name: string, password: string): Observable<Competitor> {
    return this.http
      .post<Competitor>(Api.login, { name, password })
      .pipe(tap((competitor) => this.current.set(competitor)));
  }

  logout(): Observable<void> {
    return this.http.post<void>(Api.logout, {}).pipe(tap(() => this.current.set(null)));
  }
}
