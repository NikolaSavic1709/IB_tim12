import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainPageComponent } from './components/main-page/main-page.component';
import { RequestPageComponent } from './components/request-page/request-page.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { LoginGuard } from './guard/login.guard';
import { NotLoggedInGuard } from './guard/not-logged-in.guard';

const routes: Routes = [
  { path: 'certificates', component: MainPageComponent,
  canActivate: [NotLoggedInGuard] },
  { path: 'requests', component: RequestPageComponent,
  canActivate: [NotLoggedInGuard] },
  { path: 'login', component: LoginComponent,
  canActivate: [LoginGuard] },
  { path: 'register', component: RegisterComponent,
  canActivate: [LoginGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
