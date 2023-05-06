import { Component, Inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { CertificateRequestService } from 'src/app/service/certificate-request.service';

@Component({
  selector: 'app-request-dialog',
  templateUrl: './request-dialog.component.html',
  styleUrls: ['./request-dialog.component.css']
})
export class RequestDialogComponent {
  admin = false;
  constructor(public dialogRef: MatDialogRef<RequestDialogComponent>, public requestService: CertificateRequestService, public authService: AuthService) {}

  closeDialog() {
    this.dialogRef.close();
  }

  ngOnInit() {
    if (this.authService.getRole() == "ADMIN") {
    this.admin = true;
    }
  }
}
