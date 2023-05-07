import { Component, Inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { CertificateRequestService } from 'src/app/service/certificate-request.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CreateCertificate } from 'src/app/model/CreateCertificate';


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

  constructor(public dialogRef: MatDialogRef<RequestDialogComponent>, public requestService: CertificateRequestService, public authService: AuthService) {}

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

  sendRequest() {
    let request: CreateCertificate = {
      signatureAlgorithm: this.certificateForm.value.algorithm as string,
      issuer: this.certificateForm.value.issuer as string,
      type: this.certificateForm.value.certificateType as string,
      email: this.authService.getEmail()
    }


    this.requestService.sendRequest(request).subscribe({
      next: (res) => {
        this.dialogRef.close();
      }
    })}

    cancel() {
      this.dialogRef.close();
    }
}
