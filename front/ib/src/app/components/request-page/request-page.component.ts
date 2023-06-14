import { Component } from '@angular/core';
import { CertificateRequest } from '../../model/CertificateRequest';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import { CertificateRequestService } from '../../service/certificate-request.service';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { RequestDialogComponent } from '../../dialog/request-dialog/request-dialog/request-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RejectDialogComponent } from 'src/app/dialog/reject-dialog/reject-dialog.component';

@Component({
  selector: 'app-request-page',
  templateUrl: './request-page.component.html',
  styleUrls: ['./request-page.component.css']
})
export class RequestPageComponent {

    certificates: CertificateRequest[] = [];
    displayedColumns: string[] = ['select', 'serial number', 'Issuer', 'Valid from', 'Valid to', 'Type', 'Subject', 'Status'];
    dataSource = new MatTableDataSource<CertificateRequest>();
    selection = new SelectionModel<CertificateRequest>(true, []);
  
    constructor(private snackBar: MatSnackBar, private certificateRequestService: CertificateRequestService, private matDialog: MatDialog) { }

    ngOnInit() {
  
      this.certificateRequestService.getCertificates().subscribe({
        next: (res) => {
          this.certificates = res;
          this.dataSource.data = this.certificates;
        },
      });
    }
  
    isAllSelected() {
      const numSelected = this.selection.selected.length;
      const numRows = this.dataSource.data.length;
      return numSelected === numRows;
    }
  
    /** Selects all rows if they are not all selected; otherwise clear selection. */
    masterToggle() {
      this.isAllSelected() ?
          this.selection.clear() :
          this.dataSource.data.forEach(row => this.selection.select(row));
    }

    openForm() {
      const dialogConfig = new MatDialogConfig();
        dialogConfig.width = "300px"; 
        const modalDialog = this.matDialog.open(RequestDialogComponent, dialogConfig);
    }

    acceptRequests() {
      this.selection.selected.forEach(cert => {
        let serialNumber = cert.serialNumber;
        this.certificateRequestService.acceptRequest(serialNumber).subscribe({
          next: (res) => {
            this.snackBar.open('Successfully accepted and created', 'Close', {
              duration: 3000,
              verticalPosition: 'bottom',
              horizontalPosition: 'center',
            });
          },
        });
      });
    }

    rejectRequests() {
      let selectedCertificates = this.selection.selected;
      let dialogRef: MatDialogRef<RejectDialogComponent>;
  
      const openNextForm = (certIndex: number) => {
        if (certIndex < selectedCertificates.length) {
          const dialogConfig = new MatDialogConfig();
          dialogConfig.width = "300px"; 
          dialogConfig.data = selectedCertificates[certIndex].serialNumber;
          
          dialogRef = this.matDialog.open(RejectDialogComponent, dialogConfig);
          dialogRef.afterClosed().subscribe(() => {
            openNextForm(certIndex + 1);
          });
        }
      };
      openNextForm(0);
    }
  }
  
