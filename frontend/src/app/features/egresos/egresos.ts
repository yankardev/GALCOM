import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { API_URL } from '../../core/api';

interface Banco {
  id: number;
  nombre: string;
  numeroCuenta: string;
}
interface Egreso {
  id: number;
  tipoDocumento?: string;
  numeroDocumento?: string;
  proveedor: string;
  fecha: string;
  subtotal: number;
  impuesto: number;
  montoTotal: number;
  documentoAsociado?: string;
  motivo: string;
  archivoOrigen?: string;
  estado: string;
  bancoNombre?: string;
  reciboId?: number;
  numeroRecibo?: string;
}
interface ReciboDetalle {
  cuentaId: number;
  servicio: string;
  periodo: string;
  montoAplicado: number;
}

interface Recibo {
  id: number;
  numeroCorrelativo: string;
  tipo: string;
  usuario: string;
  socioNombre?: string;
  puestoNumero?: string;
  fecha: string;
  montoTotal: number;
  metodoPago?: string;
  estado: string;
  detalles: ReciboDetalle[];
}
@Component({
  selector: 'app-egresos',
imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  templateUrl: './egresos.html',
  styleUrl: './egresos.css',
})
export class Egresos implements OnInit {
  readonly bancos = signal<Banco[]>([]);
  readonly rows = signal<Egreso[]>([]);
  readonly saving = signal(false);
  readonly uploading = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly reciboSeleccionado = signal<Recibo | null>(null);
mes = (() => {
  const hoy = new Date();

  const anio = hoy.getFullYear();

  const mes = String(
    hoy.getMonth() + 1
  ).padStart(2, '0');

  return `${anio}-${mes}`;
})();
  archivo: File | null = null;
readonly form = new FormGroup({
  tipoDocumento: new FormControl('FACTURA',Validators.required),
  numeroDocumento: new FormControl('',Validators.required),
  proveedor: new FormControl('',Validators.required),
  fecha: new FormControl(new Date().toISOString().slice(0, 10),Validators.required),
  // Cantidad que escribe el usuario
  montoIngresado: new FormControl<number | null>(null,[Validators.required,Validators.min(0.01)]),
  // Indica cómo debe tratarse el IGV
  tratamientoIgv: new FormControl<string | null>(null,Validators.required),
  // Valores calculados automáticamente
  subtotal: new FormControl<number>({ value: 0, disabled: true }),
  impuesto: new FormControl<number>({ value: 0, disabled: true }),
  montoTotal: new FormControl<number>({ value: 0, disabled: true }),
  documentoAsociado: new FormControl(''),
  motivo: new FormControl( '',Validators.required),
  bancoId: new FormControl<number | null>(null),
});

constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<Banco[]>(`${API_URL}/bancos/all`).subscribe((r) => this.bancos.set(r));
    this.load();
  }

save(): void {
  // Calculamos subtotal, IGV y total antes de guardar
  this.recalcularTotal();
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    this.error.set('Revise los campos obligatorios y los importes del egreso.');
    return;
  }
  const raw = this.form.getRawValue();
  if (Number(raw.montoTotal || 0) <= 0) {
    this.error.set(
      'El monto total del egreso debe ser mayor que cero.'
    );
    return;
  }
  // Solo enviamos al backend los campos que Egreso ya conoce
  const payload = {
    tipoDocumento: raw.tipoDocumento,
    numeroDocumento: raw.numeroDocumento,
    proveedor: raw.proveedor,
    fecha: raw.fecha,
    subtotal: raw.subtotal,
    impuesto: raw.impuesto,
    montoTotal: raw.montoTotal,
    documentoAsociado: raw.documentoAsociado,
    motivo: raw.motivo,
    bancoId: raw.bancoId,
  };
  this.saving.set(true);
  this.error.set('');
  this.message.set('');
  this.http.post<Egreso>(
    `${API_URL}/egresos`,
    payload
  ).subscribe({
    next: (r) => {
      this.saving.set(false);
      this.message.set(
        `Egreso registrado por S/ ${Number(r.montoTotal).toFixed(2)}. Queda pendiente de procesar.`
      );
      this.resetForm();
      this.load();
    },
    error: (e) => {
      this.saving.set(false);
      this.fail(e);
    },
  });
}
load(): void {

  const p = new URLSearchParams();

  if (this.mes) {

    const [anio, mes] =
      this.mes.split('-').map(Number);

    // Primer día del mes
    const inicio =
      `${this.mes}-01`;

    // Último día del mes
    const ultimoDia =
      new Date(anio, mes, 0).getDate();

    const fin =
      `${this.mes}-${String(ultimoDia).padStart(2, '0')}`;

    p.set('inicio', inicio);
    p.set('fin', fin);
  }

  this.http
    .get<Egreso[]>(
      `${API_URL}/egresos/all${p.toString() ? '?' + p.toString() : ''}`
    )
    .subscribe({

      next: (r) => {
        this.rows.set(r);
      },

      error: (e) => {
        this.fail(e);
      },

    });
}
  procesar(x: Egreso): void {
    if (!confirm(`¿Procesar el egreso de ${x.proveedor} por S/ ${x.montoTotal}?`)) return;
    this.http.put<Egreso>(`${API_URL}/egresos/${x.id}/procesar`, {}).subscribe({
      next: (r) => {
        this.message.set(`Egreso procesado. Comprobante ${r.numeroRecibo}`);
        this.load();
      },
      error: (e) => this.fail(e),
    });
  }
  anular(x: Egreso): void {
    if (!confirm('¿Confirma anular este egreso?')) return;
    this.http.put<Egreso>(`${API_URL}/egresos/${x.id}/anular`, {}).subscribe({
      next: () => {
        this.message.set('Egreso anulado.');
        this.load();
      },
      error: (e) => this.fail(e),
    });
  }
verVoucher(x: Egreso): void {

  if (!x.reciboId) {
    this.error.set('Este egreso todavía no tiene un comprobante generado.');
    return;
  }

  this.http
    .get<Recibo>(`${API_URL}/recibos/${x.reciboId}`)
    .subscribe({
      next: (r) => {
        this.reciboSeleccionado.set(r);
      },
      error: (e) => {
        this.fail(e);
      },
    });
}

cerrarVoucher(): void {
  this.reciboSeleccionado.set(null);
}

imprimirVoucher(): void {
  window.print();
}

  seleccionarArchivo(event: Event): void {
    this.archivo = (event.target as HTMLInputElement).files?.[0] || null;
  }
  importar(): void {
    if (!this.archivo) {
      this.error.set('Seleccione un archivo CSV.');
      return;
    }
    this.uploading.set(true);
    this.error.set('');
    const fd = new FormData();
    fd.append('archivo', this.archivo);
    this.http.post<Egreso[]>(`${API_URL}/egresos/importar`, fd).subscribe({
      next: (r) => {
        this.uploading.set(false);
        this.message.set(`Se importaron ${r.length} egresos.`);
        this.archivo = null;
        this.load();
      },
      error: (e) => {
        this.uploading.set(false);
        this.fail(e);
      },
    });
  }
  clase(e: string): string {
    return e === 'PROCESADO' ? 'success' : e === 'REGISTRADO' ? 'warning' : 'danger';
  }

 recalcularTotal(): void {

  const monto = Number(
    this.form.controls.montoIngresado.value || 0
  );

  const tratamiento =
    this.form.controls.tratamientoIgv.value;

  const IGV = 0.18;

  let subtotal = 0;
  let impuesto = 0;
  let total = 0;

  if (monto <= 0 || !tratamiento) {

    this.form.controls.subtotal.setValue(
      0,
      { emitEvent: false }
    );

    this.form.controls.impuesto.setValue(
      0,
      { emitEvent: false }
    );

    this.form.controls.montoTotal.setValue(
      0,
      { emitEvent: false }
    );

    return;
  }

  switch (tratamiento) {

    case 'INCLUIDO':

      total = monto;
      subtotal = monto / (1 + IGV);
      impuesto = total - subtotal;

      break;

    case 'ADICIONAL':

      subtotal = monto;
      impuesto = monto * IGV;
      total = subtotal + impuesto;

      break;

    case 'NO_APLICA':

      subtotal = monto;
      impuesto = 0;
      total = monto;

      break;
  }

  subtotal = Math.round(subtotal * 100) / 100;
  impuesto = Math.round(impuesto * 100) / 100;
  total = Math.round(total * 100) / 100;

  this.form.controls.subtotal.setValue(
    subtotal,
    { emitEvent: false }
  );

  this.form.controls.impuesto.setValue(
    impuesto,
    { emitEvent: false }
  );

  this.form.controls.montoTotal.setValue(
    total,
    { emitEvent: false }
  );

}

private resetForm(): void {
  this.form.reset({
    tipoDocumento: 'FACTURA',
    numeroDocumento: '',
    proveedor: '',
    fecha: new Date().toISOString().slice(0, 10),
    // Nuevos campos
    montoIngresado: null,
    tratamientoIgv: null,
    // Valores calculados
    subtotal: 0,
    impuesto: 0,
    montoTotal: 0,
    documentoAsociado: '',
    motivo: '',
    bancoId: null,
  });
}

  private fail(e: HttpErrorResponse): void {
    this.error.set(e.error?.message || 'No fue posible completar la operación.');
  }
}
