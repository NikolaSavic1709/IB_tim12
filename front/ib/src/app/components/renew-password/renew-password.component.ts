import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { match } from '../register/register.component';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { RenewPasswordRequest } from 'src/app/model/RenewPasswordRequest';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-renew-password',
  templateUrl: './renew-password.component.html',
  styleUrls: ['./renew-password.component.css']
})
export class RenewPasswordComponent {
  hasError: boolean;
  activationType:string|null|undefined;
  activationResource:string|null|undefined;
  hidePassword = true;
  hideConfirmPassword = true;

  reset = new FormGroup({
    oldPassword: new FormControl('', [Validators.required]),
    newPassword: new FormControl('', [Validators.minLength(8), Validators.required]),
    confirmPassword: new FormControl('', [Validators.minLength(8), Validators.required]),
    email: new FormControl('', [Validators.required]),
  }, {validators: [match('newPassword', 'confirmPassword')]});


  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService,
              private reCaptchaV3Service: ReCaptchaV3Service,
              private snackBar: MatSnackBar) {
    this.activationResource='';
    this.activationType='';
    this.hasError = false;
  }

  ngOnInit(): void {}

  toChange() {
    if (this.reset.valid) {
      const renewPasswordRequest: RenewPasswordRequest = {
        newPassword: this.reset.value.newPassword as string,
        oldPassword: this.reset.value.oldPassword as string,
        email: this.reset.value.email as string

      }

      this.reCaptchaV3Service.execute('homepage')
        .subscribe((token) => {
          //console.log(token);
          //this.handleToken(token));
          
          this.authService.renewPassword(renewPasswordRequest,token).subscribe({
            next: (result) => {
              localStorage.setItem('expiredPassword', 'false');
              this.router.navigate(['/password-changed']);
            },
            error: (error) => {
              if (error instanceof HttpErrorResponse) {
                const errorCode = error.status;
    
                if (errorCode === 400) {
                    this.hasError = true;
                    this.snackBar.open('Cannot use previous password', 'Close', {
                      duration: 3000,
                      verticalPosition: 'bottom',
                      horizontalPosition: 'center',
                    });
                } else {
                  this.snackBar.open('E-mail or password incorrect', 'Close', {
                    duration: 3000,
                    verticalPosition: 'bottom',
                    horizontalPosition: 'center',
                  });
                }
              }
            },
          });

        });

      


    }
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}
