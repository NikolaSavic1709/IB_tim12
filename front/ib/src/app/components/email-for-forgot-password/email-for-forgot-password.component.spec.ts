import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailForForgotPasswordComponent } from './email-for-forgot-password.component';

describe('EmailForForgotPasswordComponent', () => {
  let component: EmailForForgotPasswordComponent;
  let fixture: ComponentFixture<EmailForForgotPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmailForForgotPasswordComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmailForForgotPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
