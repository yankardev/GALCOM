import { DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { API_URL } from '../../core/api';

interface Banco{ id:number;nombre:string;numeroCuenta:string; }
interface Ingreso{ id:number;depositante:string;categoria:string;concepto:string;monto:number;fecha:string;observaciones?:string;bancoNombre?:string;numeroRecibo?:string; }

@Component({selector:'app-ingresos',imports:[ReactiveFormsModule,DecimalPipe],templateUrl:'./ingresos.html',styleUrl:'./ingresos.css'})
export class Ingresos implements OnInit{
  readonly bancos=signal<Banco[]>([]);readonly rows=signal<Ingreso[]>([]);readonly saving=signal(false);readonly error=signal('');readonly message=signal('');
  inicio='';fin='';
  readonly form=new FormGroup({depositante:new FormControl('',Validators.required),categoria:new FormControl('',Validators.required),concepto:new FormControl('',Validators.required),monto:new FormControl<number|null>(null,[Validators.required,Validators.min(.01)]),fecha:new FormControl(new Date().toISOString().slice(0,10),Validators.required),observaciones:new FormControl(''),bancoId:new FormControl<number|null>(null)});
  constructor(private readonly http:HttpClient){}
  ngOnInit():void{this.http.get<Banco[]>(`${API_URL}/bancos/all`).subscribe(r=>this.bancos.set(r));this.load();}
  save():void{if(this.form.invalid){this.form.markAllAsTouched();return;}this.saving.set(true);this.error.set('');this.http.post<Ingreso>(`${API_URL}/ingresos`,this.form.getRawValue()).subscribe({next:r=>{this.saving.set(false);this.message.set(`Ingreso registrado. Recibo ${r.numeroRecibo}`);this.form.reset({fecha:new Date().toISOString().slice(0,10)});this.load()},error:e=>{this.saving.set(false);this.fail(e)}});}
  load():void{const p=new URLSearchParams();if(this.inicio)p.set('inicio',this.inicio);if(this.fin)p.set('fin',this.fin);this.http.get<Ingreso[]>(`${API_URL}/ingresos/all${p.toString()?'?'+p:''}`).subscribe({next:r=>this.rows.set(r),error:e=>this.fail(e)});}
  private fail(e:HttpErrorResponse):void{this.error.set(e.error?.message||'No fue posible completar la operación.');}
}
