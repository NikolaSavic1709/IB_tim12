import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ProfileUpdate } from '../components/profile/profile.component';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthService } from './auth-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  constructor(private http: HttpClient, private router:Router, private authService:AuthService) {
  }

  editProfile(profile: ProfileUpdate): Observable<any> {
    const id = this.authService.getId();
    return this.http.put<any>(environment.apiHost + "user/update/"+id,
      profile)
  }

  getProfileInfo():Observable<Profile>{
    const id = this.authService.getId();
    return this.http.get<Profile>(environment.apiHost + 'user/' + id);
  }
}

export interface Profile{
  name:string,
  surname:string,
  telephoneNumber:string,
  id:number,
  email:string
}