import { Component } from '@angular/core';
import { AuthService } from './service/auth-service/auth.service';
import { environment } from 'src/environments/environment';
import { Router } from '@angular/router';
import { SocialAuthService } from '@abacritt/angularx-social-login';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ib';

  constructor(private authService: AuthService,
    private socialAuthService:SocialAuthService,
    private router:Router) {}

  isAuthenticated() {
    return this.authService.isLoggedIn();
  }
  
  logout() {
    localStorage.removeItem("user");
    this.socialAuthService.signOut(true);
    this.router.navigate(['/login']);

    //this.authService.logout();

    // this.authService.logout().subscribe({
    //   next: (result) => {
    //     localStorage.removeItem("user");
    //   },
    //   error: (error) => {
    //     console.log(error);
    //     localStorage.removeItem("user");
    //   },
    // });
  }
}
