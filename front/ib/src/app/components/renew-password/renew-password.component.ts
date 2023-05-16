import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { match } from '../register/register.component';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { RenewPasswordRequest } from 'src/app/model/RenewPasswordRequest';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

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
      this.authService.renewPassword(renewPasswordRequest).subscribe({
        next: (result) => {
          localStorage.setItem('expiredPassword', 'false');
          this.router.navigate(['/password-changed']);
        },
        error: (error) => {
          if (error instanceof HttpErrorResponse) {
            this.snackBar.open('E-mail or password incorrect', 'Close', {
              duration: 3000,
              verticalPosition: 'bottom',
              horizontalPosition: 'center',
            });
          }
        },
      });


    }
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}
