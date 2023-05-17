import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RegistrationService } from 'src/app/service/registration.service';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-verify-account',
  templateUrl: './verify-account.component.html',
  styleUrls: ['./verify-account.component.css']
})
export class VerifyAccountComponent {

  hasError: boolean;
  token: number;
  activationType: string|null|undefined;
  activationResource: string|null|undefined;
  hidePassword = true;
  hideConfirmPassword = true;

  verify = new FormGroup({
    token: new FormControl('', [Validators.minLength(6), Validators.maxLength(6), Validators.required]),
  });

  constructor(private router: Router,
    private reCaptchaV3Service: ReCaptchaV3Service,
    private registrationService:RegistrationService,
    private route: ActivatedRoute) {
    this.token = 0;
    this.activationResource = '';
    this.activationType = '';
    this.hasError = false;
  }

  ngOnInit(): void {
    // this.route.queryParams
    //   .subscribe(params => {
    //     // this.token = params['token'];
    //     this.activationType = params['type'];
    //     this.activationResource = params['resource'];
    //   }
    //   );
    this.activationResource = this.registrationService.activationResource;
    this.activationType = this.registrationService.activationType;
  }

  toVerify() {
    let token = Number(this.verify.value.token)
    if (this.verify.valid && !isNaN(token)) {
      this.reCaptchaV3Service.execute('homepage')
        .subscribe((recaptchaToken) => {
          //console.log(token);
          //this.handleToken(token));

          const activationDTO: any = {
            code: token,
            activationType: this.activationType,
            activationResource: this.activationResource,
          }
          this.registrationService.token = token;
          this.registrationService.recaptchaToken = recaptchaToken;
          this.router.navigate(['/account-activated']);

        });
      

    }
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}
