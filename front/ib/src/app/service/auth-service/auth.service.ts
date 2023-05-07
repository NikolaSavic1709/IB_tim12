import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { environment } from 'src/environments/environment';
import { LoginMFARequest, LoginRequest } from 'src/app/model/LoginRequest';
import { TokenResponse } from 'src/app/model/TokenResponse';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  hasError=new BehaviorSubject<boolean>(false);
  hasErrorObs=this.hasError.asObservable();
  
  public email:string|null|undefined='';
  public password:string|null|undefined='';

  private headers = new HttpHeaders({
    skip: 'true',
  });

  user$ = new BehaviorSubject(null);
  userState$ = this.user$.asObservable();

  constructor(private http: HttpClient, private router:Router) {
    this.user$.next(this.getRole());
  }

  setUser(): void {
    this.user$.next(this.getRole());
  }

  login(auth: LoginRequest): Observable<any> {
    return this.http.post(environment.apiHost + 'login', auth, {
      headers: this.headers, responseType: 'text'
    });
  }

  loginUserObs(login:LoginRequest):any{
    this.login(login).subscribe({
      next: (result) => {
        this.email = login.email;
        this.password = login.password;
        this.router.navigate(['/two-factor-auth']);
      },
      error: (error) => {
        if (error instanceof HttpErrorResponse) {
          this.hasError.next(true);
        }
      },
    });
  }

  loginMFA(auth: LoginMFARequest): any {
    this.http.post<TokenResponse>(environment.apiHost + 'loginMFA', auth, {
      headers: this.headers,
    }).subscribe({
      next: (result) => {
        console.log(result.accessToken);
        localStorage.setItem('user', JSON.stringify(result.accessToken));
        this.setUser();

        // if (this.getRole() == "ADMIN") {
        //   this.router.navigate(['/certificates']);
        // } else if (this.getRole() == "USER") {
        //   this.router.navigate(['/certificates']);
        // }
        this.router.navigate(['/certificates']);
      },
      error: (error) => {
        if (error instanceof HttpErrorResponse) {
          this.hasError.next(true);
        }
      },
    });
  }

  logout(): Observable<string> {
    return this.http.get(environment.apiHost + 'logout', {
      responseType: 'text',
    });
  }

  getRole(): any {
    if (this.isLoggedIn()) {
      const accessToken: any = localStorage.getItem('user');
      const helper = new JwtHelperService();
      const role = helper.decodeToken(accessToken).role;
      return role;
    }
    return null;
  }

  getId(): any {
    if (this.isLoggedIn()) {
      const accessToken: any = localStorage.getItem('user');
      const helper = new JwtHelperService();
      const id = helper.decodeToken(accessToken).id;
      return id;
    }
    return null;
  }

  getEmail(): string {
    if (this.isLoggedIn()) {
      const accessToken: string | null = localStorage.getItem('user');
      if (!accessToken)
        return "";
      const helper = new JwtHelperService();
      return helper.decodeToken(accessToken).sub;
    }

    return "";
  }

  isLoggedIn(): boolean {
    if (localStorage.getItem('user') != null) {
      return true;
    }
    return false;
  }

}
