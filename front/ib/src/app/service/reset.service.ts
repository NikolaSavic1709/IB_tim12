import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { Message } from './registration.service';

@Injectable({
  providedIn: 'root'
})
export class ResetService {

  public type:string|null|undefined='';
  public resource:string|null|undefined='';
  public token:number|null|undefined=0;

  private headers = new HttpHeaders({
    'Content-Type': 'application/json',
    skip: 'true',
  });

  constructor(private http: HttpClient) {
  }

  sendResetCode(type: string, resource:string, token:string): Observable<any> {
    const recaptchaHeaders = new HttpHeaders({
      skip: 'true', recaptcha:token
    });

    const forgotPassword :ForgotPasswordDTO={
      activationType: type,
      activationResource: resource
    };

    return this.http.post(environment.apiHost + "forgotPassword",forgotPassword, 
      {headers: recaptchaHeaders, responseType: 'text'})
  }

  changePasswordWithResetCode(resetPasswordDTO: ResetPasswordDTO,token:string): Observable<any> {
    const recaptchaHeaders = new HttpHeaders({
      skip: 'true', recaptcha:token
    });

    return this.http.post(environment.apiHost + "resetPassword", resetPasswordDTO,
      {"headers": recaptchaHeaders, responseType: 'text'})
  }
}

export interface ResetPasswordDTO{
  "code": number,
  "newPassword": string,
  "activationType":string,
  "activationResource":string,
}

export interface ForgotPasswordDTO{
  "activationType":string,
  "activationResource":string,
}
