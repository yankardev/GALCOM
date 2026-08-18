import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { API_URL } from '../../core/api';

interface Cuenta{ id:number;servicioNombre:string;periodo:string;monto:number;fechaVencimiento:string;estado:string; }
interface Recibo{ id:number;numeroCorrelativo:string;tipo:string;fecha:string;montoTotal:number;estado:string; }
interface ResumenData{tipo:string;id:number;nombre:string;totalPendiente:number;cuentas:Cuenta[];recibos:Recibo[];}

@Component({selector:'app-resumen',imports:[DecimalPipe,DatePipe],templateUrl:'./resumen.html',styleUrl:'./resumen.css'})
export class Resumen implements OnInit{
  readonly data=signal<ResumenData|null>(null);readonly error=signal('');readonly loading=signal(true);
  constructor(private readonly route:ActivatedRoute,private readonly http:HttpClient){}
  ngOnInit():void{const tipo=this.route.snapshot.queryParamMap.get('tipo')||'';const id=Number(this.route.snapshot.queryParamMap.get('id'));if(!tipo||!id){this.error.set('Faltan parámetros para mostrar el resumen.');this.loading.set(false);return;}this.http.get<ResumenData>(`${API_URL}/cobranza/resumen?tipo=${tipo}&id=${id}`).subscribe({next:r=>{this.data.set(r);this.loading.set(false)},error:()=>{this.error.set('No se pudo cargar el resumen.');this.loading.set(false)}});}
  imprimir():void{window.print()}
}
