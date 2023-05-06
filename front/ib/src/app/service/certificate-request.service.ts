import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { CertificatePage } from '../model/CertificateResponse';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { CertificateRequest } from '../model/CertificateRequest';
import { AuthService } from './auth-service/auth.service';
import { CreateCertificate } from '../model/CreateCertificate';

@Injectable({
  providedIn: 'root'
})
export class CertificateRequestService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getCertificates(): Observable<CertificateRequest[]> {
    return this.http.get<CertificateRequest[]>(environment.apiHost + "/api/request/all/" + this.authService.getId());
  }

  sendRequests(CreateCertificate : CreateCertificate): Observable<CertificateRequest> {
    return this.http.post<CertificateRequest>(environment.apiHost + "/api/request/create", CreateCertificate);
  }

  ngOnInit() {
    
  }
}
