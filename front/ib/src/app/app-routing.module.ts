import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainPageComponent } from './components/main-page/main-page.component';
import { RequestPageComponent } from './components/request-page/request-page.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { LoginGuard } from './guard/login.guard';
import { NotLoggedInGuard } from './guard/not-logged-in.guard';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { VerifyAccountComponent } from './components/verify-account/verify-account.component';
import { AccountActivatedComponent } from './components/account-activated/account-activated.component';
import { PasswordChangedComponent } from './components/password-changed/password-changed.component';
import { EmailForForgotPasswordComponent } from './components/email-for-forgot-password/email-for-forgot-password.component';
import { TwoFactorAuthComponent } from './components/two-factor-auth/two-factor-auth.component';

const routes: Routes = [
  { path: 'certificates', component: MainPageComponent,
  // canActivate: [NotLoggedInGuard]
 },
  { path: 'requests', component: RequestPageComponent,
  canActivate: [NotLoggedInGuard] },
  { path: 'login', component: LoginComponent,
  canActivate: [LoginGuard] },
  { path: 'register', component: RegisterComponent,
  canActivate: [LoginGuard] },
  {
    path: 'forgot-password',
    canActivate: [LoginGuard],
    component: ForgotPasswordComponent
  },
  {
    path: 'email-for-forgot-password',
    canActivate: [LoginGuard],
    component: EmailForForgotPasswordComponent
  },
  {
    path: 'reset-password',
    canActivate: [LoginGuard],
    component: ResetPasswordComponent
  },
  {
    path: 'password-changed',
    canActivate: [LoginGuard],
    component: PasswordChangedComponent
  },
  {
    path: 'verify-account',
    canActivate: [LoginGuard],
    component: VerifyAccountComponent
  },
  {
    path: 'account-activated',
    canActivate: [LoginGuard],
    component: AccountActivatedComponent
  },
  {
    path: 'two-factor-auth',
    canActivate: [LoginGuard],
    component: TwoFactorAuthComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
