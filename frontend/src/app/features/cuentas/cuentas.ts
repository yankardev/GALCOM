import { DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { API_URL } from '../../core/api';

interface Servicio {
  id: number;
  nombre: string;
  costo: number;
  cargoA: 'PUESTO' | 'SOCIO';
  tipoCalculo: 'FIJO' | 'CONSUMO';
  estado: boolean;
}
interface Socio {
  id: number;
  codigo: string;
  nombres: string;
  apellidos: string;
  etapa: number;
}
interface Puesto {
  id: number;
  numero: string;
  ubicacion: string;
  estado: string;
}
interface Cuenta {
  id: number;
  servicioNombre: string;
  socioNombre?: string;
  puestoNumero?: string;
  periodo: string;
  lecturaInicial?: number;
  lecturaFinal?: number;
  monto: number;
  fechaEmision: string;
  fechaVencimiento: string;
  estado: string;
}

@Component({
  selector: 'app-cuentas',
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './cuentas.html',
  styleUrl: './cuentas.css',
})
export class Cuentas implements OnInit {
  readonly tab = signal<'individual' | 'puestos' | 'socios'>('individual');
  readonly rows = signal<Cuenta[]>([]);
  readonly servicios = signal<Servicio[]>([]);
  readonly socios = signal<Socio[]>([]);
  readonly puestos = signal<Puesto[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly etapas = signal<number[]>([1, 2, 3]);
  query = '';
  estadoFiltro = 'TODOS';

  readonly individual = new FormGroup({
    servicioId: new FormControl<number | null>(null, Validators.required),
    destinoId: new FormControl<number | null>(null, Validators.required),
    periodo: new FormControl('', Validators.required),
    monto: new FormControl<number | null>(null, [Validators.min(0)]),
    lecturaInicial: new FormControl<number | null>(null, [Validators.min(0)]),
    lecturaFinal: new FormControl<number | null>(null, [Validators.min(0)]),
    fechaEmision: new FormControl('', Validators.required),
    fechaVencimiento: new FormControl('', Validators.required),
  });
  readonly masivoPuestos = new FormGroup({
    servicioId: new FormControl<number | null>(null, Validators.required),
    periodo: new FormControl('', Validators.required),
    monto: new FormControl<number | null>(null, [Validators.min(0)]),
    fechaEmision: new FormControl('', Validators.required),
    fechaVencimiento: new FormControl('', Validators.required),
  });
  readonly masivoSocios = new FormGroup({
    servicioId: new FormControl<number | null>(null, Validators.required),
    periodo: new FormControl('', Validators.required),
    monto: new FormControl<number | null>(null, [Validators.min(0)]),
    fechaEmision: new FormControl('', Validators.required),
    fechaVencimiento: new FormControl('', Validators.required),
    sociosUnicos: new FormControl(true),
  });

  constructor(private readonly http: HttpClient) {
    this.individual.controls.servicioId.valueChanges.subscribe(() =>
      this.actualizarServicioIndividual(),
    );
    this.individual.controls.fechaEmision.valueChanges.subscribe((v) =>
      this.sugerirFechas(this.individual, v),
    );
    this.masivoPuestos.controls.fechaEmision.valueChanges.subscribe((v) =>
      this.sugerirFechas(this.masivoPuestos, v),
    );
    this.masivoSocios.controls.fechaEmision.valueChanges.subscribe((v) =>
      this.sugerirFechas(this.masivoSocios, v),
    );
  }

  ngOnInit(): void {
    this.aplicarFechasIniciales();
    this.loading.set(true);
    forkJoin({
      cuentas: this.http.get<Cuenta[]>(`${API_URL}/cuentas/all`),
      servicios: this.http.get<Servicio[]>(`${API_URL}/servicios/all`),
      socios: this.http.get<Socio[]>(`${API_URL}/socios/all`),
      puestos: this.http.get<Puesto[]>(`${API_URL}/puestos/all`),
    }).subscribe({
      next: (r) => {
        this.rows.set(r.cuentas);
        this.servicios.set(r.servicios);
        this.socios.set(r.socios);
        this.puestos.set(r.puestos);
        this.loading.set(false);
      },
      error: (e) => this.fail(e),
    });
  }

  get filtered(): Cuenta[] {
    const q = this.query.toLowerCase().trim();
    return this.rows().filter(
      (x) =>
        (this.estadoFiltro === 'TODOS' || x.estado === this.estadoFiltro) &&
        (!q || JSON.stringify(x).toLowerCase().includes(q)),
    );
  }
  servicioSeleccionado(): Servicio | undefined {
    return this.servicios().find((s) => s.id === Number(this.individual.controls.servicioId.value));
  }
  serviciosPuesto(): Servicio[] {
    return this.servicios().filter(
      (s) => s.cargoA === 'PUESTO' && s.tipoCalculo === 'FIJO' && s.estado !== false,
    );
  }
  serviciosSocio(): Servicio[] {
    return this.servicios().filter(
      (s) => s.cargoA === 'SOCIO' && s.tipoCalculo === 'FIJO' && s.estado !== false,
    );
  }
  responsables(): Array<{ id: number; label: string }> {
    const s = this.servicioSeleccionado();
    if (s?.cargoA === 'SOCIO')
      return this.socios().map((x) => ({
        id: x.id,
        label: `${x.codigo} - ${x.nombres} ${x.apellidos}`,
      }));
    return this.puestos()
      .filter((x) => x.estado === 'OCUPADO')
      .map((x) => ({ id: x.id, label: `${x.numero} - ${x.ubicacion || ''}` }));
  }
  setTab(t: 'individual' | 'puestos' | 'socios'): void {
    this.tab.set(t);
    this.error.set('');
    this.message.set('');
  }
  toggleEtapa(etapa: number, checked: boolean): void {
    const n = checked ? [...this.etapas(), etapa] : this.etapas().filter((x) => x !== etapa);
    this.etapas.set([...new Set(n)].sort());
  }
  tieneEtapa(e: number): boolean {
    return this.etapas().includes(e);
  }

  guardarIndividual(): void {
    if (this.individual.invalid) {
      this.individual.markAllAsTouched();
      this.error.set('Complete los campos obligatorios de la cuenta.');
      return;
    }
    const s = this.servicioSeleccionado();
    if (!s) {
      this.error.set('Seleccione un servicio.');
      return;
    }
    const v = this.individual.getRawValue();
    if (v.fechaEmision && v.fechaVencimiento && v.fechaVencimiento < v.fechaEmision) {
      this.error.set('La fecha de vencimiento no puede ser anterior a la emisión.');
      return;
    }
    const body: any = {
      servicioId: Number(v.servicioId),
      periodo: v.periodo,
      monto: v.monto,
      fechaEmision: v.fechaEmision,
      fechaVencimiento: v.fechaVencimiento,
      estado: 'PENDIENTE',
    };
    if (s.cargoA === 'SOCIO') body.socioId = Number(v.destinoId);
    else body.puestoId = Number(v.destinoId);
    if (s.tipoCalculo === 'CONSUMO') {
      body.lecturaInicial = v.lecturaInicial;
      body.lecturaFinal = v.lecturaFinal;
    }
    this.post(`${API_URL}/cuentas`, body, 'Cuenta generada correctamente.', () =>
      this.resetIndividual(),
    );
  }

generarPuestos(): void {

  this.error.set('');
  this.message.set('');

  const controles = this.masivoPuestos.controls;

  // 1. Servicio obligatorio
  if (!controles.servicioId.value) {
    controles.servicioId.markAsTouched();

    this.error.set(
      'Seleccione un servicio fijo para generar las cuentas.'
    );

    return;
  }

  // 2. Período obligatorio
  if (!controles.periodo.value) {
    controles.periodo.markAsTouched();

    this.error.set(
      'Ingrese el período de las cuentas.'
    );

    return;
  }

  // 3. Fechas obligatorias
  if (
    !controles.fechaEmision.value ||
    !controles.fechaVencimiento.value
  ) {

    this.masivoPuestos.markAllAsTouched();

    this.error.set(
      'Complete la fecha de emisión y vencimiento.'
    );

    return;
  }

  // 4. Monto inválido
  if (controles.monto.invalid) {

    controles.monto.markAsTouched();

    this.error.set(
      'El monto no puede ser negativo.'
    );

    return;
  }

  const v = this.masivoPuestos.getRawValue();

  // 5. Validar fechas
  if (
    v.fechaEmision &&
    v.fechaVencimiento &&
    v.fechaVencimiento < v.fechaEmision
  ) {

    this.error.set(
      'La fecha de vencimiento no puede ser anterior a la emisión.'
    );

    return;
  }

  this.saving.set(true);

  this.http.post<Cuenta[]>(
    `${API_URL}/cuentas/generar-puestos`,
    v
  ).subscribe({

    next: (cuentasGeneradas) => {

      this.saving.set(false);

      // No se creó ninguna porque ya existían
      if (cuentasGeneradas.length === 0) {

        this.message.set(
          'No se generaron nuevas cuentas. Las obligaciones para este servicio y período ya existen.'
        );

        return;
      }

      // Sí se crearon cuentas nuevas
      this.message.set(
        `Se generaron ${cuentasGeneradas.length} cuentas correctamente para los puestos ocupados.`
      );

      this.resetMasivoPuestos();

      this.loadCuentas();
    },

    error: (e) => {

      this.saving.set(false);

      this.fail(e);
    }

  });
}
generarSocios(): void {

  this.error.set('');
  this.message.set('');

  const controles = this.masivoSocios.controls;

  // Servicio obligatorio
  if (!controles.servicioId.value) {
    controles.servicioId.markAsTouched();

    this.error.set(
      'Seleccione un servicio para generar las cuentas de los socios.'
    );

    return;
  }

  // Debe seleccionar al menos una etapa
  if (!this.etapas().length) {

    this.error.set(
      'Seleccione al menos una etapa.'
    );

    return;
  }

  // Período obligatorio
  if (!controles.periodo.value) {
    controles.periodo.markAsTouched();

    this.error.set(
      'Ingrese el período de las cuentas.'
    );

    return;
  }

  // Fechas obligatorias
  if (
    !controles.fechaEmision.value ||
    !controles.fechaVencimiento.value
  ) {

    this.masivoSocios.markAllAsTouched();

    this.error.set(
      'Complete la fecha de emisión y vencimiento.'
    );

    return;
  }

  // Monto inválido
  if (controles.monto.invalid) {

    controles.monto.markAsTouched();

    this.error.set(
      'El monto no puede ser negativo.'
    );

    return;
  }

  const v = this.masivoSocios.getRawValue();

  // Validación de fechas
  if (
    v.fechaEmision &&
    v.fechaVencimiento &&
    v.fechaVencimiento < v.fechaEmision
  ) {

    this.error.set(
      'La fecha de vencimiento no puede ser anterior a la emisión.'
    );

    return;
  }

  const body = {
    ...v,
    etapas: this.etapas()
  };

  this.saving.set(true);

  this.http.post<Cuenta[]>(
    `${API_URL}/cuentas/generar-socios`,
    body
  ).subscribe({

    next: (cuentasGeneradas) => {

      this.saving.set(false);

      // No se creó nada
      if (cuentasGeneradas.length === 0) {

        this.message.set(
          'No se generaron nuevas cuentas. Los socios seleccionados ya tienen esta obligación para el servicio y período indicados.'
        );

        return;
      }

      // Sí se crearon cuentas
      this.message.set(
        `Se generaron ${cuentasGeneradas.length} cuentas correctamente para los socios seleccionados.`
      );

      this.resetMasivoSocios();

      this.loadCuentas();
    },

    error: (e) => {

      this.saving.set(false);

      this.fail(e);
    }

  });
}

  estadoClase(e: string): string {
    return e === 'ABONADA'
      ? 'success'
      : e === 'PENDIENTE'
        ? 'warning'
        : e === 'EXONERADA'
          ? 'info'
          : 'danger';
  }

  private actualizarServicioIndividual(): void {
    this.individual.controls.destinoId.setValue(null, { emitEvent: false });
    const consumo = this.servicioSeleccionado()?.tipoCalculo === 'CONSUMO';
    for (const c of [
      this.individual.controls.lecturaInicial,
      this.individual.controls.lecturaFinal,
    ]) {
      if (consumo) c.addValidators(Validators.required);
      else c.removeValidators(Validators.required);
      c.updateValueAndValidity({ emitEvent: false });
    }
  }

  private aplicarFechasIniciales(): void {
    const hoy = new Date().toISOString().slice(0, 10);
    this.setFechas(this.individual, hoy);
    this.setFechas(this.masivoPuestos, hoy);
    this.setFechas(this.masivoSocios, hoy);
  }

  private sugerirFechas(form: any, fecha: string | null): void {
    if (fecha) this.setFechas(form, fecha, false);
  }
  private setFechas(form: any, fecha: string, forzar = true): void {
    const venc = this.sumarMes(fecha);
    const periodo = fecha.slice(0, 7);
    if (forzar || !form.controls.fechaEmision.value)
      form.controls.fechaEmision.setValue(fecha, { emitEvent: false });
    form.controls.fechaVencimiento.setValue(venc, { emitEvent: false });
    if (forzar || !form.controls.periodo.value || /^\d{4}-\d{2}$/.test(form.controls.periodo.value))
      form.controls.periodo.setValue(periodo, { emitEvent: false });
  }
  private sumarMes(fecha: string): string {
    const [y, m, d] = fecha.split('-').map(Number);
    let ny = y,
      nm = m + 1;
    if (nm === 13) {
      nm = 1;
      ny++;
    }
    const ultimo = new Date(ny, nm, 0).getDate();
    const nd = Math.min(d, ultimo);
    return `${ny}-${String(nm).padStart(2, '0')}-${String(nd).padStart(2, '0')}`;
  }

  private resetIndividual(): void {
    this.individual.reset({
      servicioId: null,
      destinoId: null,
      periodo: '',
      monto: null,
      lecturaInicial: null,
      lecturaFinal: null,
      fechaEmision: '',
      fechaVencimiento: '',
    });
    const h = new Date().toISOString().slice(0, 10);
    this.setFechas(this.individual, h);
  }
  private resetMasivoPuestos(): void {
    this.masivoPuestos.reset({
      servicioId: null,
      periodo: '',
      monto: null,
      fechaEmision: '',
      fechaVencimiento: '',
    });
    this.setFechas(this.masivoPuestos, new Date().toISOString().slice(0, 10));
  }
  private resetMasivoSocios(): void {
    this.masivoSocios.reset({
      servicioId: null,
      periodo: '',
      monto: null,
      fechaEmision: '',
      fechaVencimiento: '',
      sociosUnicos: true,
    });
    this.setFechas(this.masivoSocios, new Date().toISOString().slice(0, 10));
  }

  private post(url: string, body: any, msg: string, done: () => void): void {
    this.saving.set(true);
    this.error.set('');
    this.message.set('');
    this.http.post<any>(url, body).subscribe({
      next: () => {
        this.saving.set(false);
        this.message.set(msg);
        done();
        this.loadCuentas();
      },
      error: (e) => {
        this.saving.set(false);
        this.fail(e);
      },
    });
  }
  private loadCuentas(): void {
    this.http
      .get<Cuenta[]>(`${API_URL}/cuentas/all`)
      .subscribe({ next: (r) => this.rows.set(r), error: (e) => this.fail(e) });
  }
  private fail(e: HttpErrorResponse): void {
    this.loading.set(false);
    this.error.set(e.error?.message || 'No fue posible completar la operación.');
  }
}
