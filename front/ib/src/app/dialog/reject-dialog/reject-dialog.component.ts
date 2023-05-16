import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from 'src/app/service/auth-service/auth.service';
import { CertificateRequestService } from 'src/app/service/certificate-request.service';
import { CertificateServiceService } from 'src/app/service/certificate-service.service';

@Component({
  selector: 'app-reject-dialog',
  templateUrl: './reject-dialog.component.html',
  styleUrls: ['./reject-dialog.component.css']
})
export class RejectDialogComponent {
  rejectionForm = new FormGroup({
    rejectionReason: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  constructor(public dialogRef: MatDialogRef<RejectDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: string, public requestService: CertificateRequestService, public authService: AuthService, private snackBar: MatSnackBar) {}

  closeDialog() {
    this.dialogRef.close();
  }

  ngOnInit() : void {}


  isFormValid(): boolean {
    return this.rejectionForm.valid;
  }

  rejectRequest() {
    this.requestService.rejectRequest(this.data, this.rejectionForm.value.rejectionReason as string).subscribe({
      next: (res) => {
        this.dialogRef.close();
        this.snackBar.open('Successfully rejected!', 'Close', {
          duration: 3000,
          verticalPosition: 'bottom',
          horizontalPosition: 'center',
        });
      },
      error:(error)=>{
        this.snackBar.open('Cannot be rejected', 'Close', {
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
