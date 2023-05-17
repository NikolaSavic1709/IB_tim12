import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RecaptchaService {

  constructor(private http: HttpClient) { }

  private verifyUrl = 'https://www.google.com/recaptcha/api/siteverify';

  verifyToken(token: string) : Observable<any>{

    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded')
      .set('Access-Control-Allow-Origin', 'http://localhost:4200')
      .set('Access-Control-Allow-Methods', '*');

    const body = new HttpParams()
      .set('secret', environment.reCaptchaV3SiteKey) //secret key to add but not secure
      .set('response', token);

    return this.http.post<RecaptchaResponse>(this.verifyUrl, body.toString(), { headers })


  }
  
}


export interface RecaptchaResponse {
  success : boolean,
  challenge_ts : string,
  hostname : string;
  score : number;
  action : string;
}