import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-password-changed',
  templateUrl: './password-changed.component.html',
  styleUrls: ['./password-changed.component.css']
})
export class PasswordChangedComponent {
  constructor(private router: Router) {}

  toLogin(){
    this.router.navigate(['/login']);
  }
}
