import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor() {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const accessToken: any = localStorage.getItem('user');
    const decodedItem = JSON.parse(accessToken);

    if (req.headers.get('skip')) {
    return next.handle(req);
    }

    if (accessToken) {
      const cloned = req.clone({
        setHeaders: { Authorization: "Bearer " + decodedItem},
      });

      return next.handle(cloned);
    } else {
      return next.handle(req);
    }
  }
}
