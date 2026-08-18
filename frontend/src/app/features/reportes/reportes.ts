import { HttpClient } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API_URL } from '../../core/api';

interface ReportOption { value:string; label:string; description:string; period:'day'|'month'|'none'; }

@Component({selector:'app-reportes',imports:[FormsModule],templateUrl:'./reportes.html',styleUrl:'./reportes.css'})
export class Reportes {
  readonly downloading=signal(false);readonly error=signal('');
  readonly options:ReportOption[]=[
    {value:'MOVIMIENTOS_DIARIOS',label:'Movimientos diarios',description:'Recibos y egresos registrados en una fecha.',period:'day'},
    {value:'TOTALES',label:'Totales del mes',description:'Totales consolidados de ingresos, egresos y saldo del período.',period:'month'},
    {value:'MENSUAL',label:'Resumen mensual',description:'Ingresos, egresos, saldo y detalle de comprobantes del mes.',period:'month'},
    {value:'SOCIOS',label:'Socios',description:'Relación general de socios registrados.',period:'none'},
    {value:'NO_SOCIOS',label:'No socios / puestos libres',description:'Puestos sin socio asociado y sus datos.',period:'none'},
    {value:'EGRESOS',label:'Egresos',description:'Comprobantes de egreso correspondientes al mes.',period:'month'},
    {value:'BANCOS',label:'Bancos',description:'Cuentas bancarias configuradas en GALCOM.',period:'none'}
  ];
  tipo='MOVIMIENTOS_DIARIOS';fecha=new Date().toISOString().slice(0,10);mes=new Date().toISOString().slice(0,7);
  constructor(private readonly http:HttpClient){}
  option():ReportOption{return this.options.find(x=>x.value===this.tipo)!;}
  descargar():void{this.downloading.set(true);this.error.set('');const p=new URLSearchParams({tipo:this.tipo});if(this.option().period==='day')p.set('fecha',this.fecha);if(this.option().period==='month')p.set('mes',this.mes);this.http.get(`${API_URL}/reportes/xlsx?${p}`,{responseType:'blob',observe:'response'}).subscribe({next:r=>{this.downloading.set(false);const blob=r.body!;const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download=`GALCOM_${this.tipo}.xlsx`;a.click();URL.revokeObjectURL(a.href);},error:()=>{this.downloading.set(false);this.error.set('No se pudo generar el reporte. Verifique el servidor y vuelva a intentar.')}});}
}
