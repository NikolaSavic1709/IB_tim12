import { Component } from '@angular/core';
import { CertificateRequest } from '../model/CertificateRequest';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import { CertificateRequestService } from '../service/certificate-request.service';

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
  
    constructor(private certificateRequestService: CertificateRequestService) { }
  
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
  }
  
