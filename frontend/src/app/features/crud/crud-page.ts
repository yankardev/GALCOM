import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { API_URL } from '../../core/api';

export interface SelectOption { label: string; value: string | number | boolean; }
export interface Field {
  key: string;
  label: string;
  type?: 'text' | 'email' | 'number' | 'date' | 'select' | 'boolean';
  required?: boolean;
  placeholder?: string;
  options?: SelectOption[];
  source?: string;
  optionLabelKeys?: string[];
  valueType?: 'number' | 'boolean' | 'string';
  generated?: boolean;
  tableHidden?: boolean;
  pattern?: string;
  patternMessage?: string;
  min?: number;
  max?: number;
  minLength?: number;
  maxLength?: number;
  helper?: string;
  defaultValue?: string | number | boolean | null;
  allowEmpty?: boolean;
  emptyLabel?: string;
  pastDate?: boolean;
}

export interface CrudConfig { title: string; subtitle?: string; endpoint: string; fields: Field[]; newLabel?: string; }

const pastDateValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (!control.value) return null;
  const selected = new Date(`${control.value}T00:00:00`);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return selected < today ? null : { pastDate: true };
};

@Component({
  selector:'app-crud-page',
  imports:[ReactiveFormsModule],
  templateUrl:'./crud-page.html',
  styleUrl:'./crud-page.css'
})
export class CrudPage implements OnInit {
  readonly rows = signal<any[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly message = signal('');
  readonly error = signal('');
  readonly lookups = signal<Record<string, any[]>>({});
  readonly form = new FormGroup<Record<string,FormControl<any>>>({});
  readonly config = inject(ActivatedRoute).snapshot.data as CrudConfig;
  readonly modalOpen = signal(false);
  editingId: number | null = null;
  query = '';

  constructor(private readonly http: HttpClient) {
    for (const f of this.config.fields) {
      const validations: ValidatorFn[] = [];
      if (f.required && !f.generated) validations.push(Validators.required);
      if (f.type === 'email') validations.push(Validators.email);
      if (f.pattern) validations.push(Validators.pattern(f.pattern));
      if (f.min !== undefined) validations.push(Validators.min(f.min));
      if (f.max !== undefined) validations.push(Validators.max(f.max));
      if (f.minLength !== undefined) validations.push(Validators.minLength(f.minLength));
      if (f.maxLength !== undefined) validations.push(Validators.maxLength(f.maxLength));
      if (f.pastDate) validations.push(pastDateValidator);
      this.form.addControl(f.key, new FormControl(this.initialValue(f), validations));
    }
  }

  ngOnInit(): void { this.loadLookups(); this.load(); }

  get filtered(): any[] {
    const q = this.query.trim().toLowerCase();
    if (!q) return this.rows();
    return this.rows().filter(row => this.config.fields.some(f => this.display(row, f).toLowerCase().includes(q)));
  }

  get tableFields(): Field[] { return this.config.fields.filter(f => !f.tableHidden); }
  get formFields(): Field[] { return this.config.fields.filter(f => !f.generated || this.editingId !== null); }

  load(): void {
    this.loading.set(true);
    this.http.get<any[]>(`${API_URL}/${this.config.endpoint}/all`).subscribe({
      next:r => { this.rows.set(r); this.loading.set(false); },
      error:e => this.fail(e)
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.resetForCreate();
    this.message.set('');
    this.error.set('');
    this.modalOpen.set(true);
  }

  edit(row:any): void {
    this.editingId = row.id;
    for (const f of this.config.fields) this.form.controls[f.key].setValue(row[f.key] ?? this.initialValue(f));
    this.message.set('');
    this.error.set('');
    this.modalOpen.set(true);
  }

  cancel(): void {
    this.editingId = null;
    this.resetForCreate();
    this.error.set('');
    this.modalOpen.set(false);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    this.message.set('');
    const body:any = {...this.form.getRawValue()};
    for (const f of this.config.fields) {
      if (f.generated) { delete body[f.key]; continue; }
      if ((f.type === 'number' || f.valueType === 'number') && body[f.key] !== '' && body[f.key] !== null && body[f.key] !== undefined) body[f.key] = Number(body[f.key]);
      if (f.type === 'boolean' || f.valueType === 'boolean') body[f.key] = body[f.key] === true || body[f.key] === 'true';
      if (body[f.key] === '') body[f.key] = null;
    }

    if (!this.validateBusinessRules(body)) {
      this.saving.set(false);
      return;
    }

    const request = this.editingId
      ? this.http.put(`${API_URL}/${this.config.endpoint}/${this.editingId}`, body)
      : this.http.post(`${API_URL}/${this.config.endpoint}`, body);

    request.subscribe({
      next:(saved:any)=>{
        const wasEditing=this.editingId!==null;
        this.saving.set(false);
        const generatedCode=!wasEditing&&saved?.codigo?` Código asignado: ${saved.codigo}.`:'';
        this.message.set((wasEditing?'Registro actualizado correctamente.':'Registro creado correctamente.')+generatedCode);
        this.editingId=null;
        this.resetForCreate();
        this.modalOpen.set(false);
        this.load();
      },
      error:e=>{this.saving.set(false);this.fail(e);}
    });
  }

  remove(row:any): void {
    const etiqueta = row.nombre ?? row.codigo ?? row.numero ?? row.id;
    if (!confirm(`¿Confirma eliminar el registro ${etiqueta}?`)) return;
    this.http.delete(`${API_URL}/${this.config.endpoint}/${row.id}`).subscribe({
      next:()=>{this.message.set('Registro eliminado correctamente.');this.load();}, error:e=>this.fail(e)
    });
  }

  options(field: Field): SelectOption[] {
    if (field.options) return field.options;
    if (!field.source) return [];
    return (this.lookups()[field.source] ?? []).map(item => ({
      value: item.id,
      label: this.lookupLabel(item, field.optionLabelKeys ?? ['nombre'])
    }));
  }

  display(row:any, f:Field): string {
    const value = row[f.key];
    if (f.type === 'boolean') return value === true ? 'Activo' : value === false ? 'Inactivo' : '—';
    if (f.source && value != null) {
      const item=(this.lookups()[f.source]??[]).find(x=>String(x.id)===String(value));
      if(item) return this.lookupLabel(item,f.optionLabelKeys??['nombre']);
    }
    if (f.options) return f.options.find(o=>String(o.value)===String(value))?.label ?? String(value ?? '—');
    return value === null || value === undefined || value === '' ? '—' : String(value);
  }

  validationMessage(f: Field): string {
    const c = this.form.controls[f.key];
    if (c.hasError('required')) return `${f.label} es obligatorio.`;
    if (c.hasError('email')) return 'Ingrese un correo válido.';
    if (c.hasError('pattern')) return f.patternMessage || `${f.label} tiene un formato inválido.`;
    if (c.hasError('min')) return `${f.label} no puede ser menor que ${f.min}.`;
    if (c.hasError('max')) return `${f.label} no puede ser mayor que ${f.max}.`;
    if (c.hasError('minlength')) return `${f.label} es demasiado corto.`;
    if (c.hasError('maxlength')) return `${f.label} excede la longitud permitida.`;
    if (c.hasError('pastDate')) return 'La fecha de nacimiento debe ser anterior a hoy.';
    return 'Revise el valor ingresado.';
  }

  isStatusField(f: Field): boolean { return f.type === 'boolean' || f.key.toLowerCase() === 'estado'; }

  badgeClass(row:any,f:Field):string {
    const value=String(row[f.key]??'').toUpperCase();
    if(row[f.key]===true || ['ACTIVO','PAGADO','ABONADA','EMITIDO','DISPONIBLE'].includes(value)) return 'success';
    if(['PENDIENTE','OCUPADO','MANTENIMIENTO'].includes(value)) return 'warning';
    if(row[f.key]===false || ['INACTIVO','VENCIDO','ANULADO','ANULADA'].includes(value)) return 'danger';
    return 'neutral';
  }

  private initialValue(f: Field): any {
    if (f.defaultValue !== undefined) return f.defaultValue;
    if (f.type === 'boolean') return true;
    if (f.type === 'select') return null;
    return '';
  }

  private resetForCreate(): void {
    const values: Record<string, any> = {};
    for (const f of this.config.fields) values[f.key] = this.initialValue(f);
    this.form.reset(values);
  }

  private validateBusinessRules(body:any): boolean {
    if (this.config.endpoint === 'puestos' && body.vigenciaInicio && body.vigenciaFin && body.vigenciaFin < body.vigenciaInicio) {
      this.error.set('La fecha fin de vigencia no puede ser anterior a la fecha de inicio.');
      return false;
    }
    return true;
  }

  private loadLookups(): void {
    const sources=[...new Set(this.config.fields.map(f=>f.source).filter((x):x is string=>!!x))];
    if(!sources.length) return;
    const requests:Record<string,Observable<any[]>>={};
    for(const source of sources) requests[source]=this.http.get<any[]>(`${API_URL}/${source}/all`);
    forkJoin(requests).subscribe({next:r=>this.lookups.set(r),error:()=>this.error.set('No fue posible cargar los datos auxiliares.')});
  }

  private lookupLabel(item:any, keys:string[]):string {
    return keys.map(k=>item[k]).filter(v=>v!==null&&v!==undefined&&v!=='').join(' ') || String(item.id);
  }

  private fail(e:HttpErrorResponse):void {
    this.loading.set(false);
    this.error.set(e.error?.message || e.error?.detail || 'No fue posible completar la operación. Verifique los datos y el servidor.');
  }
}
