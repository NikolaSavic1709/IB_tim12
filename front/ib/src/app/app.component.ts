import { Component } from '@angular/core';
import { AuthService } from './service/auth-service/auth.service';
import { Router } from '@angular/router';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { OauthService } from './service/oauth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ib';

  constructor(private authService: AuthService,
    private oauthService: OauthService,
    private socialAuthService:SocialAuthService,
    private router:Router) {}

  isAuthenticated() {
    return this.authService.isLoggedIn();
  }
  
  logout() {
    localStorage.removeItem("user");
    this.oauthService.clearUserState();
    if (this.oauthService.loggedWithGoogle){
      this.socialAuthService.signOut(true);
      this.oauthService.loggedWithGoogle=false;
    }
    
    this.router.navigate(['/login']);
  }
}
