import {HttpErrorResponse} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {FormGroup, FormControl, Validators, ValidatorFn, AbstractControl} from '@angular/forms';
import {Router} from '@angular/router';
import { RegistrationService } from 'src/app/service/registration.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  hidePassword = true;
  hideConfirmPassword = true;
  hasError = false;
  submitted = false;

  allTextPattern = "[a-zA-Z][a-zA-Z]*";
  phoneNumberPattern = "[0-9 +]?[0-9]+[0-9 \\-]+";

  signupForm = new FormGroup({
    name: new FormControl('', [Validators.pattern(this.allTextPattern), Validators.required]),
    surname: new FormControl('', [Validators.pattern(this.allTextPattern), Validators.required]),
    phoneNumber: new FormControl('', [Validators.pattern(this.phoneNumberPattern), Validators.minLength(6), Validators.maxLength(20), Validators.required]),
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', [Validators.minLength(8), Validators.required]),
    confirmPassword: new FormControl('', [Validators.required]),
    twoFactor: new FormControl('', [Validators.required]),
  }, {validators: [match('password', 'confirmPassword')]});

  constructor(private router: Router,
              private registrationService: RegistrationService) {
  }

  ngOnInit(): void {
    this.registrationService.hasErrorObs.subscribe((value)=>{
      this.hasError=value;
    })
  }

  signup() {
    console.log(this.signupForm.value.twoFactor);
    this.submitted = true;
    
    if (this.signupForm.valid) {
      const registration: Registration = {
        name: this.signupForm.value.name,
        surname: this.signupForm.value.surname,
        telephoneNumber: this.signupForm.value.phoneNumber,
        email: this.signupForm.value.email,
        password: this.signupForm.value.password,
        userActivationType: this.signupForm.value.twoFactor
      }
  
      this.registrationService.registerUserObs(registration);
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

export interface Registration {
  name?: string | null,
  surname?: string | null,
  telephoneNumber?: string | null,
  email?: string | null,
  password?: string | null,
  userActivationType?: string|null
}
