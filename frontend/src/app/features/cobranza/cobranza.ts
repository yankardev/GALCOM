import { DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { API_URL } from '../../core/api';

interface Socio{ id:number;codigo:string;nombres:string;apellidos:string; }
interface Puesto{ id:number;numero:string;ubicacion:string;estado:string; }
interface Banco{ id:number;nombre:string;numeroCuenta:string; }
interface Cuenta{ id:number;servicioNombre:string;periodo:string;monto:number;fechaVencimiento:string;estado:string;socioNombre?:string;puestoNumero?:string; }
interface Recibo{ id:number;numeroCorrelativo:string;tipo:string;fecha:string;montoTotal:number;metodoPago:string;estado:string;detalles:Array<{cuentaId:number;servicio:string;periodo:string;montoAplicado:number}>; }
interface PagoResponse{recibo?:Recibo;totalPagado:number;cuentasAbonadas:number[];cuentasExoneradas:number[];}

@Component({selector:'app-cobranza',imports:[ReactiveFormsModule,DecimalPipe],templateUrl:'./cobranza.html',styleUrl:'./cobranza.css'})
export class Cobranza implements OnInit{
  readonly socios=signal<Socio[]>([]);readonly puestos=signal<Puesto[]>([]);readonly bancos=signal<Banco[]>([]);readonly cuentas=signal<Cuenta[]>([]);
  readonly seleccionAbono=signal<number[]>([]);readonly seleccionExon=signal<number[]>([]);readonly loading=signal(false);readonly processing=signal(false);readonly error=signal('');readonly message=signal('');readonly recibo=signal<Recibo|null>(null);readonly canjeCuenta=signal<Cuenta|null>(null);
  tipo:'SOCIO'|'PUESTO'='PUESTO';responsableId:number|null=null;
  readonly pagoForm=new FormGroup({metodoPago:new FormControl('EFECTIVO',Validators.required),bancoId:new FormControl<number|null>(null),fechaDeposito:new FormControl(''),numeroOperacion:new FormControl(''),observaciones:new FormControl('')});
  readonly canjeForm=new FormGroup({bancoId:new FormControl<number|null>(null,Validators.required),fechaDeposito:new FormControl(new Date().toISOString().slice(0,10),Validators.required),numeroOperacion:new FormControl(''),observaciones:new FormControl('')});
  readonly total=computed(()=>this.cuentas().filter(c=>this.seleccionAbono().includes(c.id)).reduce((s,c)=>s+Number(c.monto),0));

  constructor(private readonly http:HttpClient){}
  ngOnInit():void{forkJoin({socios:this.http.get<Socio[]>(`${API_URL}/socios/all`),puestos:this.http.get<Puesto[]>(`${API_URL}/puestos/all`),bancos:this.http.get<Banco[]>(`${API_URL}/bancos/all`)}).subscribe({next:r=>{this.socios.set(r.socios);this.puestos.set(r.puestos);this.bancos.set(r.bancos)},error:e=>this.fail(e)});}
  responsables():Array<{id:number;label:string}>{return this.tipo==='SOCIO'?this.socios().map(x=>({id:x.id,label:`${x.codigo} · ${x.nombres} ${x.apellidos}`})):this.puestos().map(x=>({id:x.id,label:`${x.numero} · ${x.ubicacion||''}`}));}
  cambiarTipo(t:'SOCIO'|'PUESTO'):void{this.tipo=t;this.responsableId=null;this.cuentas.set([]);this.limpiarSeleccion();}
  consultar():void{if(!this.responsableId){this.error.set('Seleccione un socio o puesto.');return;}this.loading.set(true);this.error.set('');const path=this.tipo==='SOCIO'?`socio/${this.responsableId}`:`puesto/${this.responsableId}`;this.http.get<Cuenta[]>(`${API_URL}/cobranza/${path}`).subscribe({next:r=>{this.cuentas.set(r);this.loading.set(false);this.limpiarSeleccion()},error:e=>this.fail(e)});}
  pendientes():Cuenta[]{return this.cuentas().filter(c=>c.estado==='PENDIENTE');}
  toggleAbono(id:number,checked:boolean):void{this.seleccionAbono.set(checked?[...this.seleccionAbono(),id]:this.seleccionAbono().filter(x=>x!==id));if(checked)this.seleccionExon.set(this.seleccionExon().filter(x=>x!==id));}
  toggleExon(id:number,checked:boolean):void{this.seleccionExon.set(checked?[...this.seleccionExon(),id]:this.seleccionExon().filter(x=>x!==id));if(checked)this.seleccionAbono.set(this.seleccionAbono().filter(x=>x!==id));}
  abono(id:number):boolean{return this.seleccionAbono().includes(id)} exon(id:number):boolean{return this.seleccionExon().includes(id)}
  procesar():void{if(!this.seleccionAbono().length&&!this.seleccionExon().length){this.error.set('Marque al menos una cuenta como abonada o exonerada.');return;}if(this.seleccionAbono().length&&this.pagoForm.invalid){this.pagoForm.markAllAsTouched();return;}this.processing.set(true);this.error.set('');this.message.set('');const v=this.pagoForm.getRawValue();const body={cuentasAbonar:this.seleccionAbono(),cuentasExonerar:this.seleccionExon(),metodoPago:v.metodoPago,bancoId:v.bancoId,fechaDeposito:v.fechaDeposito||null,numeroOperacion:v.numeroOperacion,observaciones:v.observaciones};this.http.post<PagoResponse>(`${API_URL}/cobranza/pagar`,body).subscribe({next:r=>{this.processing.set(false);this.message.set(r.recibo?`Pago procesado. Recibo ${r.recibo.numeroCorrelativo}`:'Cuentas exoneradas correctamente.');this.recibo.set(r.recibo||null);this.consultar()},error:e=>{this.processing.set(false);this.fail(e)}});}
  abrirCanje(cuenta:Cuenta):void{if(this.tipo!=='SOCIO')return;this.canjeCuenta.set(cuenta);this.canjeForm.reset({fechaDeposito:new Date().toISOString().slice(0,10)});}
  cerrarCanje():void{this.canjeCuenta.set(null);}
  procesarCanje():void{const cuenta=this.canjeCuenta();if(!cuenta)return;if(this.canjeForm.invalid){this.canjeForm.markAllAsTouched();return;}this.processing.set(true);this.http.post<Recibo>(`${API_URL}/cobranza/canje`,{cuentaId:cuenta.id,...this.canjeForm.getRawValue()}).subscribe({next:r=>{this.processing.set(false);this.canjeCuenta.set(null);this.recibo.set(r);this.message.set(`Canje procesado. Recibo ${r.numeroCorrelativo}`);this.consultar()},error:e=>{this.processing.set(false);this.fail(e)}});}
  abrirResumen():void{if(!this.responsableId)return;window.open(`/resumen?tipo=${this.tipo}&id=${this.responsableId}`,'_blank','width=1100,height=760');}
  cerrarRecibo():void{this.recibo.set(null)}
  imprimirRecibo():void{window.print()}
  private limpiarSeleccion():void{this.seleccionAbono.set([]);this.seleccionExon.set([])}
  private fail(e:HttpErrorResponse):void{this.loading.set(false);this.error.set(e.error?.message||'No fue posible completar la operación.');}
}
