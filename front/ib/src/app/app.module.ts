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
import { RecaptchaModule,
  RECAPTCHA_SETTINGS,
  RecaptchaSettings,
  RecaptchaFormsModule,
  RECAPTCHA_V3_SITE_KEY,
  RecaptchaV3Module } from "ng-recaptcha";
import { environment } from 'src/environments/environment';
import { RejectDialogComponent } from './dialog/reject-dialog/reject-dialog.component';
import { GoogleInitOptions, GoogleLoginProvider, SocialAuthServiceConfig } from '@abacritt/angularx-social-login';
import {GoogleSigninButtonModule} from "@abacritt/angularx-social-login";
import { ProfileComponent } from './components/profile/profile.component';
import { FormsModule } from '@angular/forms';
const googleLoginOptions: GoogleInitOptions = {
  oneTapEnabled: false, // default is true
  scopes: 'email profile https://www.googleapis.com/auth/user.phonenumbers.read'
};

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
    RenewPasswordComponent,
    RejectDialogComponent,
    ProfileComponent
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
    MatRadioModule,
    RecaptchaModule,
    RecaptchaFormsModule,
    RecaptchaV3Module,
    GoogleSigninButtonModule,
    FormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    { provide: RECAPTCHA_V3_SITE_KEY, useValue: environment.reCaptchaV3SiteKey },
    {
      provide: 'SocialAuthServiceConfig',
      useValue: {
        autoLogin: false,
        providers: [
          {
            id: GoogleLoginProvider.PROVIDER_ID,
            provider: new GoogleLoginProvider(
              '62679338211-4omk3gb5srm5i4lfpa3vscg73qah9evv.apps.googleusercontent.com',googleLoginOptions
            ),
          },
        ],
      } as SocialAuthServiceConfig,
    }
    // {
    //   provide: RECAPTCHA_SETTINGS,
    //   useValue: {
    //     siteKey: environment.reCaptchaV2SiteKey
    //   } as RecaptchaSettings
    // }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
