import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../service/auth-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class NotRenewPasswordGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}


  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
      if (!this.authService.isPasswordExpired()) {
        this.router.navigate(['/renew-password']);
        return false;
      }
    return true;
  }
  
}
