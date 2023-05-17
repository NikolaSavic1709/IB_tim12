import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RegistrationService } from 'src/app/service/registration.service';

@Component({
  selector: 'app-account-activated',
  templateUrl: './account-activated.component.html',
  styleUrls: ['./account-activated.component.css']
})
export class AccountActivatedComponent {
  hasError: boolean;
  token: number|null|undefined;
  recaptchaToken: string|null|undefined;
  activationType: string|null|undefined;
  activationResource: string|null|undefined;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private registrationService: RegistrationService) {
    this.token = 0;
    this.recaptchaToken='';
    this.activationType='';
    this.activationResource= '';
    this.hasError = false;
  }

  ngOnInit() {
    // this.route.queryParams
    //   .subscribe(params => {
          // this.token = params['token'];
          // this.activationType=params['type'];
          // this.activationResource= params['resource'];

          this.token = this.registrationService.token;
          this.recaptchaToken = this.registrationService.recaptchaToken;
          this.activationResource = this.registrationService.activationResource;
          this.activationType = this.registrationService.activationType;

          if (this.token && this.activationResource && this.activationType && this.recaptchaToken)
          this.registrationService.activateUser(this.token,this.activationResource, this.activationType, this.recaptchaToken).subscribe({
            next: (result) => {
              this.hasError = false;
            },
            error: (error) => {
              if (error instanceof HttpErrorResponse) {
                this.hasError = true;
              }
            },
          });
      //   }
      // );
    
  }


  toLogin() {
    this.router.navigate(['/login']);
  }
}
