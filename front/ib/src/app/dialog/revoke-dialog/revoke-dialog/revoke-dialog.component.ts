import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CertificateServiceService } from 'src/app/service/certificate-service.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-revoke-dialog',
  templateUrl: './revoke-dialog.component.html',
  styleUrls: ['./revoke-dialog.component.css']
})
export class RevokeDialogComponent {

  revocationForm = new FormGroup({
    revocationReason: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  constructor(public dialogRef: MatDialogRef<RevokeDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: string, public certificateService: CertificateServiceService, public authService: AuthService, private snackBar: MatSnackBar) {}

  closeDialog() {
    this.dialogRef.close();
  }

  ngOnInit() : void {}


  isFormValid(): boolean {
    return this.revocationForm.valid;
  }

  sendRequest() {
    this.certificateService.revokeCertificate(this.data, this.revocationForm.value.revocationReason as string).subscribe({
      next: (res) => {
        this.dialogRef.close();
        this.snackBar.open('Successfully revoked!', 'Close', {
          duration: 3000,
          verticalPosition: 'bottom',
          horizontalPosition: 'center',
        });
      },
      error:(error)=>{
        this.snackBar.open('Cannot be revoked', 'Close', {
          duration: 3000,
          verticalPosition: 'bottom',
          horizontalPosition: 'center',
        });
        this.cancel();
      }
    })
  }

  cancel() {
      this.dialogRef.close();
  }
}
