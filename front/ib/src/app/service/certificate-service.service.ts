import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { CertificatePage } from '../model/CertificateResponse';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CertificateServiceService {

  constructor(private http: HttpClient) { }

  getCertificates(): Observable<CertificatePage> {
    return this.http.get<CertificatePage>(environment.apiHost + "certificate");
  }

  downloadCertificate(serialNumber:string):Observable<any>{
    return this.http.get('http://localhost:8080/api/certificate/file/'+serialNumber, { responseType: 'blob' });

  }
  validateById(serialNumber:string):Observable<any>{

    return this.http.get('http://localhost:8080/api/certificate/validity/' + serialNumber);
  }

  uploadCertificate(file:File):Observable<any>{

    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post('http://localhost:8080/api/certificate/validity/file', formData);
  }

  revokeCertificate(serialNumber:string, revocationReason:string, token:string):Observable<any>{
    const recaptchaHeaders = new HttpHeaders({
      recaptcha:token
    });
    return this.http.post('http://localhost:8080/api/certificate/revoke/' +  serialNumber, revocationReason, {
      headers: recaptchaHeaders,
    });
  }

  ngOnInit() {
    
  }
}
