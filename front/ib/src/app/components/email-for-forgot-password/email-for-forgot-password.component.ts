import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-email-for-forgot-password',
  templateUrl: './email-for-forgot-password.component.html',
  styleUrls: ['./email-for-forgot-password.component.css']
})
export class EmailForForgotPasswordComponent {
  constructor(private router: Router) {
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}
