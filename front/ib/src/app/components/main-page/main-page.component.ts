import { Component, OnInit } from '@angular/core';
import { CertificateResponse } from '../../model/CertificateResponse';
import { CertificateServiceService } from '../../service/certificate-service.service';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import { MatSnackBar } from '@angular/material/snack-bar';
import { saveAs } from 'file-saver';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { RevokeDialogComponent } from 'src/app/dialog/revoke-dialog/revoke-dialog/revoke-dialog.component';


@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.css']
})

export class MainPageComponent implements OnInit{
  certificates: CertificateResponse[] = [];
  displayedColumns: string[] = ['select', 'Valid from', 'Valid to', 'Type', 'Subject'];
  dataSource = new MatTableDataSource<CertificateResponse>();
  selection = new SelectionModel<CertificateResponse>(true, []);

  constructor(private certificateService: CertificateServiceService, private snackBar: MatSnackBar, private matDialog: MatDialog) { }

  ngOnInit() {

    this.certificateService.getCertificates().subscribe({
      next: (res) => {
        this.certificates = res.results;
        this.dataSource = new MatTableDataSource<CertificateResponse>(this.certificates);
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
  download(){
    this.selection.selected.forEach(cert => {
      let serialNumber = cert.serialNumber;
      this.certificateService.downloadCertificate(serialNumber).subscribe({
        next: (res) => {
          const file = new Blob([res], { type: 'application/zip' });
          saveAs(file, serialNumber+'.zip');
          this.snackBar.open('Successfull download', 'Close', {
            duration: 3000,
            verticalPosition: 'bottom',
            horizontalPosition: 'center',
          });
        },
      });
    });
  }

  onFileSelected(event: any) {
    if (event!=null && event.target!=null)
    {
      const target= event.target as HTMLInputElement;
      if (target.files!=null && target.files.length == 1) {
        const file = target.files[0];
        console.log("pozvao servis");
        if (file.type == 'application/x-x509-ca-cert') {
          this.certificateService.uploadCertificate(file).subscribe({
            next: (res) => {
              
              this.snackBar.open('Valid', 'Close', {
                duration: 3000,
                verticalPosition: 'bottom',
                horizontalPosition: 'center',
              });
            },
            error:(error)=>{
              this.snackBar.open('Not valid', 'Close', {
                duration: 3000,
                verticalPosition: 'bottom',
                horizontalPosition: 'center',
              });
            }
          });
        }
        else {
          this.snackBar.open('File format not valid', 'Close', {
            duration: 3000,
            verticalPosition: 'bottom',
            horizontalPosition: 'center',
          });
        }
      } else {
        this.snackBar.open('Error', 'Close', {
          duration: 3000,
          verticalPosition: 'bottom',
          horizontalPosition: 'center',
        });
      }
    }

  }

  validateWithId() {
    this.selection.selected.forEach(cert => {
      let serialNumber = cert.serialNumber;
      this.certificateService.validateById(serialNumber).subscribe({
        next: (res) => {
              
          this.snackBar.open(res, 'Close', {
            duration: 3000,
            verticalPosition: 'bottom',
            horizontalPosition: 'center',
          });
        },
        error:(error)=>{
          this.snackBar.open('Not valid', 'Close', {
            duration: 3000,
            verticalPosition: 'bottom',
            horizontalPosition: 'center',
          });
        }
      });
    })
  }
  
  onUploadButtonClick() {
    const fileInputElement = document.getElementById('fileInput') as HTMLInputElement;
    fileInputElement.click();
  }

  openForm() {
    let selectedCertificates = this.selection.selected;
    let dialogRef: MatDialogRef<RevokeDialogComponent>;

    const openNextForm = (certIndex: number) => {
      if (certIndex < selectedCertificates.length) {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.width = "300px"; 
        dialogConfig.data = selectedCertificates[certIndex].serialNumber;
        
        dialogRef = this.matDialog.open(RevokeDialogComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(() => {
          openNextForm(certIndex + 1);
        });
      }
    };
    openNextForm(0);
  }
}
