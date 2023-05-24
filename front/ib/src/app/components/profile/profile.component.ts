import { HttpErrorResponse } from '@angular/common/http';
import { Component, SecurityContext } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Profile, ProfileService } from 'src/app/service/profile.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent {
  hasError = false;
  error = '';
  submitted = false;

  profile: Profile = {
    name: '',
    surname: '',
    telephoneNumber: '',
    email: '',
    id: 0
  };

  allTextPattern = "[a-zA-Z][a-zA-Z]*";
  phoneNumberPattern = "[0-9 +]?[0-9]+[0-9 \\-]+";

  editForm = new FormGroup({
    email: new FormControl(''),
    name: new FormControl('', [Validators.pattern(this.allTextPattern), Validators.required]),
    surname: new FormControl('', [Validators.pattern(this.allTextPattern), Validators.required]),
    phoneNumber: new FormControl('', [Validators.pattern(this.phoneNumberPattern), Validators.minLength(6), Validators.maxLength(20), Validators.required]),
  });

  constructor(private sanitizer: DomSanitizer,
    private profileService: ProfileService,
    private router: Router,
    private snackBar:MatSnackBar) {
  }

  ngOnInit(): void {

    this.profileService.getProfileInfo().subscribe({
      next: (profile: Profile) => {
        console.log(profile);
        this.profile.email = profile.email;
        this.profile.name = profile.name;
        this.profile.surname = profile.surname;
        if (profile.telephoneNumber)
          this.profile.telephoneNumber = profile.telephoneNumber;
      },
      error: (error) => {
        if (error instanceof HttpErrorResponse) {
          this.hasError = true;
          this.error = 'Can not update profile info';
        }
      },
    });

  }


  sanitizeInput(input: string): string {
    const safeHtml: SafeHtml = this.sanitizer.sanitize(SecurityContext.HTML, input) ?? '';
    return safeHtml.toString();
  }

  editProfile() {
    if (this.editForm.valid) {
      const profile: ProfileUpdate = {
        name: this.sanitizeInput(this.editForm.value.name as string),
        surname: this.sanitizeInput(this.editForm.value.surname as string),
        telephoneNumber: this.sanitizeInput(this.editForm.value.phoneNumber as string),
      }


      this.profileService.editProfile(profile).subscribe({
        next: (result) => {
          this.snackBar.open('Profile updated', 'Close', {
            duration: 3000,
            verticalPosition: 'bottom',
            horizontalPosition: 'center',
          });
          this.router.navigate(['/profile']);
        },
        error: (error) => {
          if (error instanceof HttpErrorResponse) {
            this.hasError = true;
            this.error = 'Can not update profile info';
          }
        },
      });

    }
  }
}

export interface ProfileUpdate {
  name: string,
  surname: string,
  telephoneNumber: string
}