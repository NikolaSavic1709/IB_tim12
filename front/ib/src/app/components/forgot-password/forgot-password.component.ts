import { HttpErrorResponse } from '@angular/common/http';
import { AfterContentInit, Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ResetService } from 'src/app/service/reset.service';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements AfterContentInit{
  hasError = false;

  forgot = new FormGroup({
    resource: new FormControl('', [Validators.required]),
    type: new FormControl('', [Validators.required]),
  });


  constructor(private router: Router,
    private resetService: ResetService,
    private reCaptchaV3Service: ReCaptchaV3Service) {
  }

  ngAfterContentInit(): void {
    this.hasError = false;
  }

  toReset() {
    if (this.forgot.valid && this.forgot.value.type && this.forgot.value.resource) {
      const type_n = this.forgot.value.type;
      const resource_n = this.forgot.value.resource;
      this.reCaptchaV3Service.execute('homepage')
      .subscribe((token) => {
        //console.log(token);
        //this.handleToken(token));
        this.resetService.sendResetCode(type_n, resource_n,token).subscribe({
          next: (result) => {
            this.resetService.type = this.forgot.value.type;
            this.resetService.resource = this.forgot.value.resource;
            this.router.navigate(['/reset-password']);
          },
          error: (error) => {
            if (error instanceof HttpErrorResponse) {
              this.hasError = true;
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
