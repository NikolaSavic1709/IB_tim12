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

  sendResetCode(type: string, resource:string): Observable<any> {
    return this.http.post(environment.apiHost + "forgotPassword",{
      activationType: type,
      activationResource: resource,
    }, 
      {headers: this.headers, responseType: 'text'})
  }

  changePasswordWithResetCode(resetPasswordDTO: ResetPasswordDTO): Observable<any> {
    return this.http.post(environment.apiHost + "resetPassword", resetPasswordDTO,
      {"headers": this.headers, responseType: 'text'})
  }
}

export interface ResetPasswordDTO{
  "code": number,
  "newPassword": string,
  "activationType":string,
  "activationResource":string,
}
