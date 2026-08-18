import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { tap } from 'rxjs';
import { API_URL } from './api';

export interface Session { token:string;usuario:string;nombres:string;apellidos:string;rol:string; }

@Injectable({providedIn:'root'})
export class AuthService {
  private readonly key='galcom_session';
  private readonly state=signal<Session|null>(this.read());
  readonly session=this.state.asReadonly();
  readonly authenticated=computed(()=>!!this.state()?.token);

  constructor(private readonly http:HttpClient){}

  login(credentials:{usuario:string;password:string}) {
    return this.http.post<Session>(`${API_URL}/usuarios/login`,credentials).pipe(tap(session=>{
      sessionStorage.setItem(this.key,JSON.stringify(session));
      this.state.set(session);
    }));
  }
  logout():void{sessionStorage.removeItem(this.key);this.state.set(null);}
  token():string{return this.state()?.token??'';}
  private read():Session|null{try{return JSON.parse(sessionStorage.getItem(this.key)??'null')}catch{return null}}
}
