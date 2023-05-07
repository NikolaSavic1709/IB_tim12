import {Component, OnInit} from '@angular/core';
import {FormGroup, FormControl, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import { LoginRequest } from 'src/app/model/LoginRequest';
import { AuthService } from 'src/app/service/auth-service/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  hide = true;
  submitted=false;
  loginForm = new FormGroup({
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', [Validators.minLength(6), Validators.required]),
    twoFactor: new FormControl('', [Validators.required]),
    keepLogin: new FormControl(),
  });
  hasError = false;

  constructor(private authService: AuthService, private router: Router) {

    this.authService.setUser();
  }

  ngOnInit(): void {
    this.authService.hasErrorObs.subscribe((value)=>{
      this.hasError=value;
    })
  }

  login() {
    this.submitted = true;
    const login: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password,
      mfaType: this.loginForm.value.twoFactor,
    }

    if (this.loginForm.valid) {
        this.authService.loginUserObs(login);
    }
  }

  toForgot() {
    this.router.navigate(['/forgot-password']);
  }

  toGoogleOAuth() {
    window.location.href = "https://accounts.google.com";
  }

  toSignup() {
    this.router.navigate(['/register']);
  }
}
