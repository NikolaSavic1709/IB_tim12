import { Component } from '@angular/core';
import { CertificateRequest } from '../../model/CertificateRequest';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import { CertificateRequestService } from '../../service/certificate-request.service';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { RequestDialogComponent } from '../../dialog/request-dialog/request-dialog/request-dialog.component';

@Component({
  selector: 'app-request-page',
  templateUrl: './request-page.component.html',
  styleUrls: ['./request-page.component.css']
})
export class RequestPageComponent {

    certificates: CertificateRequest[] = [];
    displayedColumns: string[] = ['select', 'serial number', 'Issuer', 'Valid from', 'Valid to', 'Type', 'Subject', 'Status'];
    dataSource = new MatTableDataSource<CertificateRequest>(this.certificates);
    selection = new SelectionModel<CertificateRequest>(true, []);
  
    constructor(private certificateRequestService: CertificateRequestService, private matDialog: MatDialog) { }

    ngOnInit() {
  
      this.certificateRequestService.getCertificates().subscribe({
        next: (res) => {
          this.certificates = res;
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
  }
  
