import { AfterContentInit, Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { match } from '../register/register.component';
import { ActivatedRoute, Router } from '@angular/router';
import { ResetPasswordDTO, ResetService } from 'src/app/service/reset.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements AfterContentInit{
  hasError: boolean;
  error: string;
  token: number;
  activationType: string | null | undefined;
  activationResource: string | null | undefined;
  hidePassword = true;
  hideConfirmPassword = true;

  reset = new FormGroup({
    token: new FormControl('', [Validators.minLength(6), Validators.maxLength(6), Validators.required]),
    password: new FormControl('', [Validators.minLength(8), Validators.required]),
    confirmPassword: new FormControl('', [Validators.required]),
  }, { validators: [match('password', 'confirmPassword')] });


  constructor(private router: Router,
    private route: ActivatedRoute,
    private resetService: ResetService,
    private reCaptchaV3Service: ReCaptchaV3Service) {
    this.token = 0;
    this.activationResource = '';
    this.activationType = '';
    this.hasError = false;
    this.error = '';
  }

  ngOnInit(): void {
    // this.route.queryParams
    //   .subscribe(params => {
    //       // this.token = params['token'];
    //       this.activationType = params['type'];
    //       this.activationResource = params['resource'];
    //     }
    //   );
    this.activationType = this.resetService.type;
    this.activationResource = this.resetService.resource;
  }

  ngAfterContentInit(): void {
    this.hasError = false;
    this.error = '';
  }

  toChange() {
    let token = Number(this.reset.value.token)
    if (this.reset.valid && !isNaN(token) && this.activationType && this.activationResource) {
      const resetPasswordDTO: ResetPasswordDTO = {
        code: token,
        newPassword: this.reset.value.password as string,
        activationType: this.activationType,
        activationResource: this.activationResource,

      }

      this.reCaptchaV3Service.execute('homepage')
        .subscribe((token) => {
          //console.log(token);
          //this.handleToken(token));

          this.resetService.changePasswordWithResetCode(resetPasswordDTO, token).subscribe({
            next: (result) => {
              this.router.navigate(['/password-changed']);
            },
            error: (error) => {
              if (error instanceof HttpErrorResponse) {
                this.hasError = true;
                const errorCode = error.status;

                if (errorCode === 403) {
                  this.hasError = true;
                  this.error = 'You tried more than three times!';
                } else if (errorCode === 400) {
                  this.hasError = true;
                  this.error = 'Reset code expired!';
                } else {
                  this.hasError = true;
                  this.error = 'Wrong reset code!';       
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
