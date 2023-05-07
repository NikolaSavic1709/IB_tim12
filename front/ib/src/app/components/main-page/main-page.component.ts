import { Component, OnInit } from '@angular/core';
import { CertificateResponse } from '../../model/CertificateResponse';
import { CertificateServiceService } from '../../service/certificate-service.service';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';


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

  constructor(private certificateService: CertificateServiceService) { }

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
}
