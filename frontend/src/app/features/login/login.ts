import { Component, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login', imports: [ReactiveFormsModule], templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
    loginForm = new FormGroup({
      usuario: new FormControl('', [Validators.required]),
      password: new FormControl('', [
        Validators.required, Validators.minLength(4)])
    });
  readonly loading = signal(false);
  readonly error = signal('');
  constructor(private readonly auth: AuthService, private readonly router: Router) {
    if (auth.authenticated()) router.navigateByUrl('/recibos');
  }
  ingresar(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loading.set(true); this.error.set('');
    this.auth.login(this.loginForm.getRawValue() as { usuario: string; password: string }).subscribe({
      next: () => this.router.navigateByUrl('/recibos'),
      error: () => { this.loading.set(false); this.error.set('Usuario o contraseña incorrectos.'); }
    });
  }
}
