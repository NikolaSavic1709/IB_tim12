import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ResetService } from 'src/app/service/reset.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  hasError = false;

  forgot = new FormGroup({
    resource: new FormControl('', [Validators.required]),
    type: new FormControl('', [Validators.required]),
  });


  constructor(private router: Router,
    private resetService: ResetService) {
  }

  toReset() {
    if (this.forgot.valid && this.forgot.value.type && this.forgot.value.resource) {

      this.resetService.sendResetCode(this.forgot.value.type, this.forgot.value.resource).subscribe({
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

    }
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}