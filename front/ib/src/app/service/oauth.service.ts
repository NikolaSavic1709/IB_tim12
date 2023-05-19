import { SocialUser } from '@abacritt/angularx-social-login';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OauthService {

  constructor(private http: HttpClient) { }

  public loggedWithGoogle:boolean = false;
  private userState: BehaviorSubject<SocialUser | null> = new BehaviorSubject<SocialUser | null>(null);

  setUserState(user: SocialUser | null): void {
    this.userState.next(user);
  }

  getUserState(): Observable<SocialUser | null> {
    return this.userState.asObservable();
  }

  clearUserState(): void {
    this.userState.next(null);
  }

  private headers = new HttpHeaders({
    skip: 'true',
  });
  
  login(token:string): Observable<any> {
    let googleToken :GoogleTokenDTO = {
      value:token
    };
    return this.http.post(environment.apiHost + 'google/login', googleToken, {
      headers: this.headers
    });
  }
}

export interface GoogleTokenDTO{
  value:string;
}
