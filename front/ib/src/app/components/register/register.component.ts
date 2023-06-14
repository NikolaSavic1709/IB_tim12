import {HttpErrorResponse} from '@angular/common/http';
import {AfterContentInit, Component, OnInit, SecurityContext} from '@angular/core';
import {FormGroup, FormControl, Validators, ValidatorFn, AbstractControl, ValidationErrors} from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import {Router} from '@angular/router';
import { RegistrationService } from 'src/app/service/registration.service';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements AfterContentInit{
  hidePassword = true;
  hideConfirmPassword = true;
  hasError = false;
  error = '';
  submitted = false;

  allTextPattern = "[a-zA-Z][a-zA-Z]*";
  phoneNumberPattern = "[0-9 +]?[0-9]+[0-9 \\-]+";

  signupForm = new FormGroup({
    name: new FormControl('', [Validators.pattern(this.allTextPattern), Validators.required]),
    surname: new FormControl('', [Validators.pattern(this.allTextPattern), Validators.required]),
    phoneNumber: new FormControl('', [Validators.pattern(this.phoneNumberPattern), Validators.minLength(6), Validators.maxLength(20), Validators.required]),
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', [Validators.minLength(8), Validators.required, passwordStrengthValidator]),
    confirmPassword: new FormControl('', [Validators.required]),
    twoFactor: new FormControl('', [Validators.required]),
  }, {validators: [match('password', 'confirmPassword')]});

  constructor(private sanitizer: DomSanitizer,
              private router: Router,
              private registrationService: RegistrationService,
              private reCaptchaV3Service: ReCaptchaV3Service) {
  }

  ngOnInit(): void {
    this.registrationService.hasErrorObs.subscribe((value)=>{
      this.hasError=value;
    });

    this.registrationService.errorObs.subscribe((value)=>{
      this.error=value;
    });
  }

  ngAfterContentInit(): void {
    this.hasError = false;
    this.error = '';
  }

  sanitizeInput(input: string): string {
    const safeHtml: SafeHtml = this.sanitizer.sanitize(SecurityContext.HTML, input) ?? '';
    return safeHtml.toString();
  }

  signup() {
    console.log(this.signupForm.value.twoFactor);
    this.submitted = true;
    
    if (this.signupForm.valid) {
      const registration: Registration = {
        name: this.sanitizeInput(this.signupForm.value.name as string),
        surname: this.sanitizeInput(this.signupForm.value.surname as string),
        telephoneNumber: this.sanitizeInput(this.signupForm.value.phoneNumber as string),
        email: this.sanitizeInput(this.signupForm.value.email as string),
        password: this.sanitizeInput(this.signupForm.value.password as string),
        userActivationType: this.signupForm.value.twoFactor
      }
  
      this.reCaptchaV3Service.execute('homepage')
        .subscribe((token) => {
          //console.log(token);
          //this.handleToken(token));
          this.registrationService.registerUserObs(registration,token);
        });
      
    }
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}

export function match(controlName: string, checkControlName: string): ValidatorFn {
  return (controls: AbstractControl) => {
    const control = controls.get(controlName);
    const checkControl = controls.get(checkControlName);

    if (checkControl?.errors && !checkControl.errors['matching']) {
      return null;
    }

    if (control?.value !== checkControl?.value) {
      controls.get(checkControlName)?.setErrors({matching: true});
      return {matching: true};
    } else {
      return null;
    }
  };
}

export function passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
  const value: string = control.value;
  const hasNumber = /[0-9]/.test(value);
  const hasUpper = /[A-Z]/.test(value);
  const hasLower = /[a-z]/.test(value);
  const hasSpecial = /[$@!%*?&]/.test(value);

  if (!value || value.length < 8 || !hasNumber || !hasUpper || !hasLower || !hasSpecial) {
    return { strength: true };
  }

  return null;
}

export interface Registration {
  name?: string | null,
  surname?: string | null,
  telephoneNumber?: string | null,
  email?: string | null,
  password?: string | null,
  userActivationType?: string|null
}


