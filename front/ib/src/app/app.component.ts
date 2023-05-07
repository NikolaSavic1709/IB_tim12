import { Component } from '@angular/core';
import { AuthService } from './service/auth-service/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ib';

  constructor(private authService: AuthService) {}

  isAuthenticated() {
    return this.authService.isLoggedIn();
  }
  
  logout() {
    localStorage.removeItem("user");
    this.authService.logout();
  }
}
