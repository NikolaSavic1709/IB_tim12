import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { CertificatePage } from '../model/CertificateResponse';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CertificateRequest } from '../model/CertificateRequest';
import { AuthService } from './auth-service/auth.service';
import { CreateCertificate } from '../model/CreateCertificate';

@Injectable({
  providedIn: 'root'
})
export class CertificateRequestService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getCertificates(): Observable<CertificateRequest[]> {
    return this.http.get<CertificateRequest[]>(environment.apiHost + "request/all/" + this.authService.getId());
  }

  sendRequest(CreateCertificate : CreateCertificate, token:string): Observable<CertificateRequest> {
    const recaptchaHeaders = new HttpHeaders({
      recaptcha:token
    });
    return this.http.post<CertificateRequest>(environment.apiHost + "request/create", CreateCertificate, {
      headers: recaptchaHeaders,
    });
  }

  ngOnInit() {
    
  }
}
