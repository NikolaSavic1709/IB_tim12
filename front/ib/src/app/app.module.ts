import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule} from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatToolbarModule} from '@angular/material/toolbar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatOptionModule } from '@angular/material/core';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule} from '@angular/material/select';
import { HttpClientModule } from '@angular/common/http';
import {MatTableModule} from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox'
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MainPageComponent } from './components/main-page/main-page.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RequestPageComponent } from './components/request-page/request-page.component';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from 'src/app/auth/auth.interceptor';
import { RequestDialogComponent } from './dialog/request-dialog/request-dialog/request-dialog.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { AccountActivatedComponent } from './components/account-activated/account-activated.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { PasswordChangedComponent } from './components/password-changed/password-changed.component';
import { VerifyAccountComponent } from './components/verify-account/verify-account.component';
import { EmailForForgotPasswordComponent } from './components/email-for-forgot-password/email-for-forgot-password.component';
import { TwoFactorAuthComponent } from './components/two-factor-auth/two-factor-auth.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RevokeDialogComponent } from './dialog/revoke-dialog/revoke-dialog/revoke-dialog.component';
import { RenewPasswordComponent } from './components/renew-password/renew-password.component';

@NgModule({
  declarations: [
    AppComponent,
    MainPageComponent,
    RequestPageComponent,
    RequestDialogComponent,
    LoginComponent,
    RegisterComponent,
    AccountActivatedComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    PasswordChangedComponent,
    VerifyAccountComponent,
    EmailForForgotPasswordComponent,
    TwoFactorAuthComponent,
    RevokeDialogComponent,
    RenewPasswordComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    MatInputModule,
    MatTableModule,
    MatCheckboxModule,
    MatSnackBarModule,
    HttpClientModule,
    MatDialogModule,
    MatOptionModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatRadioModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
