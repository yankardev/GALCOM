import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule,Validators} from '@angular/forms';

@Component({selector: 'app-login', imports: [ReactiveFormsModule],templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {loginForm = new FormGroup({
    usuario: new FormControl('', [ Validators.required]),
    password: new FormControl('', [
      Validators.required, Validators.minLength(4)])
  });
  ingresar(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    console.log(this.loginForm.value);
  }
}