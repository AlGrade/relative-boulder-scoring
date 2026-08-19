import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { errorMessage } from '../core/api';
import { AuthService } from '../core/auth.service';
import { GENDERS, Gender } from '../core/models';
import { FormField } from '../form-field/form-field';
import { Loading } from '../loading/loading';
import { Ranking } from '../ranking/ranking';
import { TabOption, Tabs } from '../tabs/tabs';

type Mode = 'login' | 'register';

@Component({
  selector: 'app-landing',
  imports: [FormField, Loading, ReactiveFormsModule, Ranking, Tabs],
  templateUrl: './landing.html',
  styleUrl: './landing.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Landing {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly genders = GENDERS;
  protected readonly modes: readonly TabOption<Mode>[] = [
    { id: 'login', label: 'Login' },
    { id: 'register', label: 'Registrierung' },
  ];
  // Die meisten kommen zum ersten Mal und legen erst ein Konto an.
  protected readonly mode = signal<Mode>('register');
  protected readonly error = signal('');
  protected readonly busy = signal(false);
  protected readonly showRanking = signal(false);
  /** Messages appear once someone has tried to submit, not while they are typing. */
  private readonly submitted = signal(false);

  protected readonly loginForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected readonly registerForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    gender: ['' as Gender | '', Validators.required],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });

  protected setMode(mode: Mode): void {
    this.mode.set(mode);
    this.error.set('');
    this.submitted.set(false);
  }

  protected loginError(field: 'name' | 'password'): string | null {
    if (!this.submitted() || this.loginForm.controls[field].valid) {
      return null;
    }
    return field === 'name' ? 'Bitte gib deinen Namen ein.' : 'Bitte gib dein Passwort ein.';
  }

  protected registerError(field: 'name' | 'gender' | 'password'): string | null {
    const control = this.registerForm.controls[field];
    if (!this.submitted() || control.valid) {
      return null;
    }
    if (control.hasError('minlength')) {
      return 'Das Passwort braucht mindestens 4 Zeichen.';
    }
    return {
      name: 'Bitte gib deinen Namen ein.',
      gender: 'Bitte wähle dein Geschlecht.',
      password: 'Bitte wähle ein Passwort.',
    }[field];
  }

  protected submitLogin(): void {
    this.submitted.set(true);
    if (this.loginForm.invalid) {
      return;
    }
    const { name, password } = this.loginForm.getRawValue();
    this.run(this.auth.login(name, password), 'Name oder Passwort ist nicht korrekt.');
  }

  protected submitRegister(): void {
    this.submitted.set(true);
    if (this.registerForm.invalid) {
      return;
    }
    const { name, gender, password } = this.registerForm.getRawValue();
    this.run(
      this.auth.register(name, gender as Gender, password),
      'Registrierung fehlgeschlagen. Bitte noch einmal versuchen.',
    );
  }

  private run(request: Observable<unknown>, fallback: string): void {
    this.busy.set(true);
    this.error.set('');
    request.subscribe({
      next: () => this.router.navigateByUrl('/me'),
      error: (cause: unknown) => {
        this.error.set(errorMessage(cause, fallback));
        this.busy.set(false);
      },
    });
  }
}
