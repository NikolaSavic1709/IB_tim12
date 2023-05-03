import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainPageComponent } from './main-page/main-page.component';
import { RequestPageComponent } from './request-page/request-page.component';

const routes: Routes = [
  { path: 'certificates', component: MainPageComponent },
  { path: 'requests', component: RequestPageComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
