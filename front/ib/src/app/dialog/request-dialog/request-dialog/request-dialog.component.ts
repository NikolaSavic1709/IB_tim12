import { Component, Inject, SecurityContext } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { CertificateRequestService } from 'src/app/service/certificate-request.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CreateCertificate } from 'src/app/model/CreateCertificate';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';


@Component({
  selector: 'app-request-dialog',
  templateUrl: './request-dialog.component.html',
  styleUrls: ['./request-dialog.component.css']
})
export class RequestDialogComponent {
  admin = false;
  
  certificateForm = new FormGroup({
    algorithm: new FormControl('', [Validators.required, Validators.minLength(2)]),
    issuer: new FormControl('', [Validators.required, Validators.minLength(5)]),
    certificateType: new FormControl('', [Validators.required, Validators.minLength(1)])
  });

  constructor(private sanitizer: DomSanitizer, public dialogRef: MatDialogRef<RequestDialogComponent>, public requestService: CertificateRequestService, public authService: AuthService) {}

  closeDialog() {
    this.dialogRef.close();
  }

  ngOnInit() : void {
    if (this.authService.getRole() == "ADMIN") {
    this.admin = true;
    }

    this.certificateForm.get('certificateType')?.valueChanges.subscribe(certificateType => {
      if (certificateType === 'ROOT') {
        this.certificateForm.get('issuer')?.disable(); // Disable issuer control
        this.certificateForm.get('issuer')?.setValue(null);
      } else {
        this.certificateForm.get('issuer')?.enable(); // Enable issuer control
      }
    });
  }

  isFormValid(): boolean {
    return this.certificateForm.valid;
  }

  sanitizeInput(input: string): string {
    const safeHtml: SafeHtml = this.sanitizer.sanitize(SecurityContext.HTML, input) ?? '';
    return safeHtml.toString();
  }
  
  sendRequest() {

    const email = this.authService.getEmail();
      if (!email || !this.validateEmail(email)) {
      // Handle invalid email address
      return;
    }

    let request: CreateCertificate = {
      signatureAlgorithm: this.sanitizeInput(this.certificateForm.value.algorithm as string),
      issuer: this.sanitizeInput(this.certificateForm.value.issuer as string),
      type: this.sanitizeInput(this.certificateForm.value.certificateType as string),
      email: email
    }

    if (request.issuer == "") {
      request.issuer = null;
    }
    this.requestService.sendRequest(request).subscribe({
      next: (res) => {
        this.dialogRef.close();
      }
    })}

    cancel() {
      this.dialogRef.close();
    }

  validateEmail(email: string): boolean {
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return pattern.test(email);
  }
}
