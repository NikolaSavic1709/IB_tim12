import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OauthService {

  constructor(private http: HttpClient) { }

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
