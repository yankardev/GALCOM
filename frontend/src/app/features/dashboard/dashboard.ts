import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, computed, signal } from '@angular/core';
import { API_URL } from '../../core/api';

interface ReciboMini {
  id: number;
  numeroCorrelativo: string;
  tipo: string;
  socioNombre?: string;
  puestoNumero?: string;
  fecha: string;
  montoTotal: number;
  metodoPago?: string;
  estado: string;
}

interface DashboardData {
  socios: number;
  puestos: number;
  puestosOcupados: number;
  cuentasPendientes: number;
  porCobrar: number;
  recaudadoMes: number;
  egresosMes: number;
  ultimosRecibos: ReciboMini[];
}

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  readonly loading = signal(true);
  readonly error = signal('');
  readonly data = signal<DashboardData>({ socios:0, puestos:0, puestosOcupados:0, cuentasPendientes:0, porCobrar:0, recaudadoMes:0, egresosMes:0, ultimosRecibos:[] });
  readonly ocupacion = computed(() => this.data().puestos ? Math.round(this.data().puestosOcupados * 100 / this.data().puestos) : 0);
  readonly balance = computed(() => Number(this.data().recaudadoMes || 0) - Number(this.data().egresosMes || 0));
  readonly flujoMax = computed(() => Math.max(Number(this.data().recaudadoMes || 0), Number(this.data().egresosMes || 0), 1));

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<DashboardData>(`${API_URL}/dashboard/resumen`).subscribe({
      next: r => { this.data.set(r); this.loading.set(false); },
      error: () => { this.loading.set(false); this.error.set('No se pudo cargar el resumen. Verifique que el backend esté activo.'); }
    });
  }

  ancho(valor: number): number { return Math.round(Number(valor || 0) * 100 / this.flujoMax()); }
  responsable(r: ReciboMini): string { return r.socioNombre || (r.puestoNumero ? `Puesto ${r.puestoNumero}` : 'Movimiento general'); }
}
