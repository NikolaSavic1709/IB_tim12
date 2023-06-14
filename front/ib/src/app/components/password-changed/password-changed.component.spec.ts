import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordChangedComponent } from './password-changed.component';

describe('PasswordChangedComponent', () => {
  let component: PasswordChangedComponent;
  let fixture: ComponentFixture<PasswordChangedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PasswordChangedComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordChangedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
