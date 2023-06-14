import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { Registration } from '../components/register/register.component';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  public activationType:string|null|undefined='';
  public activationResource:string|null|undefined='';
  public token:number|null|undefined=0;
  public recaptchaToken:string|null|undefined='';

  private hasError=new BehaviorSubject<boolean>(false);
  hasErrorObs=this.hasError.asObservable();

  private error=new BehaviorSubject<string>('');
  errorObs=this.error.asObservable();

  private headers = new HttpHeaders({
    skip: 'true',
  });

  constructor(private http: HttpClient, private router:Router) {
  }
  registerUserObs(registration:Registration, token:string):any{
    this.registerUser(registration,token).subscribe({
      next: (result) => {
        if (registration.userActivationType=="email"){
          this.activationResource = registration.email;
          this.activationType = "email";
        } else{
          this.activationResource = registration.telephoneNumber;
          this.activationType = "sms";
        }
        this.router.navigate(['/verify-account']);
      },
      error: (error) => {
        if (error instanceof HttpErrorResponse) {
          this.hasError.next(true);
          this.error.next('Email already exists');
        }
      },
    });
  }
  registerUser(registration: Registration, token:string): Observable<Registration> {
    const recaptchaHeaders = new HttpHeaders({
      skip: 'true', recaptcha:token
    });
    return this.http.post<any>(environment.apiHost + "register",
      {
        name: registration.name,
        surname: registration.surname,
        telephoneNumber: registration.telephoneNumber,
        email: registration.email,
        password: registration.password,
        userActivationType: registration.userActivationType
      }, {"headers": recaptchaHeaders})
  }

  activateUser(token:number,resource:string,type:string, recaptchaToken:string): Observable<any> {
    const recaptchaHeaders = new HttpHeaders({
      skip: 'true', recaptcha:recaptchaToken
    });

    return this.http.post(environment.apiHost + "activate",{
      activationType:type,
      activationResource:resource,
      activationCode:token
    }, {"headers": recaptchaHeaders, responseType: 'text'})
  }

  
}

export interface Message{
  "message": string
}
