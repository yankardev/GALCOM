import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { API_URL } from '../../core/api';

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
  selector: 'app-recibos',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './recibos.html',
  styleUrl: './recibos.css',
})
export class Recibos implements OnInit {
  readonly rows = signal<Recibo[]>([]);
  readonly seleccionado = signal<Recibo | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  fecha = new Date().toISOString().slice(0, 10);
  tipo = 'TODOS';
  constructor(private readonly http: HttpClient) {}
  ngOnInit(): void {
    this.load();
  }
  load(): void {
    this.loading.set(true);
    this.error.set('');
    const p = new URLSearchParams({ fecha: this.fecha });
    if (this.tipo !== 'TODOS') p.set('tipo', this.tipo);
    this.http.get<Recibo[]>(`${API_URL}/recibos?${p}`).subscribe({
      next: (r) => {
        this.rows.set(r);
        this.loading.set(false);
      },
      error: (e) => this.fail(e),
    });
  }
  ver(r: Recibo): void {
    this.http
      .get<Recibo>(`${API_URL}/recibos/${r.id}`)
      .subscribe({ next: (x) => this.seleccionado.set(x), error: (e) => this.fail(e) });
  }
  cerrar(): void {
    this.seleccionado.set(null);
  }
  imprimir(): void {
    window.print();
  }
  responsable(r: Recibo): string {
    return r.socioNombre || (r.puestoNumero ? `Puesto ${r.puestoNumero}` : 'Movimiento general');
  }
  clase(t: string): string {
    return t === 'EGRESO' ? 'danger' : t === 'BANCARIO' ? 'info' : 'success';
  }
  private fail(e: HttpErrorResponse): void {
    this.loading.set(false);
    this.error.set(e.error?.message || 'No se pudieron consultar los recibos.');
  }
}
