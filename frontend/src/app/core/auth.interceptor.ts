import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).token();
  const authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  return next(token ? request.clone({ setHeaders: { Authorization: authorization } }) : request);
};
