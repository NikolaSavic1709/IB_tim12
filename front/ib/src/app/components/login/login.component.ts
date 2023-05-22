import { AfterContentInit, AfterViewInit, Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginRequest } from 'src/app/model/LoginRequest';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { ReCaptchaV3Service } from 'ng-recaptcha';
import {GoogleLoginProvider, SocialAuthService, SocialUser} from "@abacritt/angularx-social-login";
import { OauthService } from 'src/app/service/oauth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// import { RecaptchaResponse, RecaptchaService } from 'src/app/service/recaptcha.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements AfterContentInit{

  hide = true;
  submitted = false;
  loginForm = new FormGroup({
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', [Validators.minLength(6), Validators.required]),
    twoFactor: new FormControl('', [Validators.required]),
    // recaptcha: new FormControl('', [Validators.required]),
    keepLogin: new FormControl(),
  });
  hasError = false;
  error = '';

  constructor(private authService: AuthService,
    private router: Router,
    private socialAuthService:SocialAuthService,
    private reCaptchaV3Service: ReCaptchaV3Service,
    private oauthService: OauthService,
    // private recaptchaService: RecaptchaService
    ) {

    this.authService.setUser();
  }
  
  
  ngAfterContentInit(): void {
    this.hasError = false;
    this.error = '';
  }

  ngOnInit(): void {
    this.authService.hasErrorObs.subscribe((value) => {
      this.hasError = value;
    })

    this.authService.errorObs.subscribe((value) => {
      this.error = value;
    })

    this.oauthService.getUserState().subscribe((user) => {
      if (user){
        this.oauthService.clearUserState();
        this.socialAuthService.signOut(true);
        this.oauthService.login(user.idToken).subscribe({
          next: (result) => {
            this.oauthService.loggedWithGoogle = true;
            localStorage.setItem('user', JSON.stringify(result.accessToken));
            this.authService.setUser();
            this.router.navigate(['/certificates']);
          },
          error: (error) => {
            if (error instanceof HttpErrorResponse) {
              const errorCode = error.status;
    
              if (errorCode === 400) {
                // account not registered
                this.hasError=true;
                this.error = 'You can not login using this Goggle account!';
              } else {
                this.hasError=true;
                this.error = 'You can not login using this Goggle account!';
              }
            }
          },
        });

      }
      
    });

    this.socialAuthService.authState.subscribe((user: SocialUser) => {
      // console.log(user);
      this.oauthService.setUserState(user);
      
    });
  }

  login() {
    this.submitted = true;
    const login: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password,
      mfaType: this.loginForm.value.twoFactor,
    }

    if (this.loginForm.valid) {
      this.reCaptchaV3Service.execute('homepage')
        .subscribe((token) => {
          //console.log(token);
          //this.handleToken(token));
          this.authService.loginUserObs(login, token);
        });

    }
  }

  toForgot() {
    this.router.navigate(['/forgot-password']);
  }

  toSignup() {
    this.router.navigate(['/register']);
  }
}
