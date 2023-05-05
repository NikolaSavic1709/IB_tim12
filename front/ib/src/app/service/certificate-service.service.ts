import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { CertificatePage } from '../model/CertificateResponse';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CertificateServiceService {

  constructor(private http: HttpClient) { }

  getCertificates(): Observable<CertificatePage> {
    return this.http.get<CertificatePage>(environment.apiHost + "/api/certificate");
  }

  ngOnInit() {
    
  }
}
