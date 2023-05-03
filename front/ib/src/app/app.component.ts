import { Component } from '@angular/core';
import { AuthService } from './service/auth-service/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ib';
  authenticated = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.authenticated = this.authService.isLoggedIn();
  }
  
  logout() {
    localStorage.removeItem("user");
    this.authenticated = false;
  }
}
