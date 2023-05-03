import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RequestPageComponent } from './request-page.component';

describe('RequestPageComponent', () => {
  let component: RequestPageComponent;
  let fixture: ComponentFixture<RequestPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RequestPageComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RequestPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
