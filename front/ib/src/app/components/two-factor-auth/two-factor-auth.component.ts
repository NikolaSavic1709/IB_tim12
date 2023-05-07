import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginMFARequest } from 'src/app/model/LoginRequest';
import { AuthService } from 'src/app/service/auth-service/auth.service';

@Component({
  selector: 'app-two-factor-auth',
  templateUrl: './two-factor-auth.component.html',
  styleUrls: ['./two-factor-auth.component.css']
})
export class TwoFactorAuthComponent {
  hasError: boolean;
  token: number;
  email: string|null|undefined;
  password: string|null|undefined;
  hidePassword = true;
  hideConfirmPassword = true;

  verify = new FormGroup({
    token: new FormControl('', [Validators.minLength(6), Validators.maxLength(6), Validators.required]),
  });

  constructor(private router: Router,
    private route: ActivatedRoute, private authService: AuthService) {
    this.token = 0;
    this.email = '';
    this.password = '';
    this.hasError = false;
  }

  ngOnInit(): void {
    //this.route.queryParams
    // .subscribe(params => {
    //   // this.token = params['token'];
    //   this.email = params['email'];
    //   this.password = params['password'];
    // }
    // );
    this.email = this.authService.email;
    this.password = this.authService.password;
    this.authService.hasErrorObs.subscribe((value) => {
      this.hasError = value;
    })
  }

  toLoginMFA() {
    let token = Number(this.verify.value.token)
    if (this.verify.valid && !isNaN(token)) {
      const loginMFA: LoginMFARequest = {
        token: token,
        email: this.email,
        password: this.password,
      }

      this.authService.loginMFA(loginMFA);
    }
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}
