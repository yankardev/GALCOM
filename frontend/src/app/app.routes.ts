import { Routes } from '@angular/router';
import { Login } from './features/login/login';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';
import { Shell } from './layout/shell';
import { Dashboard } from './features/dashboard/dashboard';
import { CrudPage, Field } from './features/crud/crud-page';
import { Cuentas } from './features/cuentas/cuentas';
import { Cobranza } from './features/cobranza/cobranza';
import { Ingresos } from './features/ingresos/ingresos';
import { Egresos } from './features/egresos/egresos';
import { Recibos } from './features/recibos/recibos';
import { Reportes } from './features/reportes/reportes';
import { Resumen } from './features/resumen/resumen';

const crud = (
  title: string,
  endpoint: string,
  fields: Field[],
  subtitle?: string,
  newLabel?: string,
) => ({ title, endpoint, fields, subtitle, newLabel });
const opt = (label: string, value: string | number | boolean) => ({ label, value });

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'resumen', component: Resumen, canActivate: [authGuard] },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: Dashboard },
      {
        path: 'socios',
        component: CrudPage,
        canActivate: [adminGuard],
        data: crud(
          'Socios',
          'socios',
          [
            {
              key: 'codigo',
              label: 'Código',
              generated: true,
              placeholder: 'Generado automáticamente',
            },
            {
              key: 'dni',
              label: 'DNI',
              placeholder: '8 dígitos',
              pattern: '^[0-9]{8}$',
              patternMessage: 'El DNI debe tener exactamente 8 dígitos.',
            },
            { key: 'nombres', label: 'Nombres', required: true },
            { key: 'apellidos', label: 'Apellidos', required: true },
            {
              key: 'telefono',
              label: 'Teléfono',
              placeholder: '9 dígitos',
              pattern: '^[0-9]{9}$',
              patternMessage: 'El teléfono debe tener exactamente 9 dígitos.',
            },
            { key: 'correo', label: 'Correo', type: 'email', tableHidden: true },
            {
              key: 'accion',
              label: 'N.º de acción',
              placeholder: 'Ej.: A-154',
              helper: 'Código de acción del socio. Campo opcional.',
            },
            {
              key: 'etapa',
              label: 'Etapa',
              type: 'select',
              required: true,
              valueType: 'number',
              placeholder: 'Seleccione una etapa...',
              options: [opt('Etapa 1', 1), opt('Etapa 2', 2), opt('Etapa 3', 3)],
            },
            {
              key: 'fechaNacimiento',
              label: 'Fecha de nacimiento',
              type: 'date',
              pastDate: true,
              tableHidden: true,
            },
            { key: 'estado', label: 'Estado', type: 'boolean', defaultValue: true },
          ],
          'Registro de socios, acciones y etapas de la galería',
          'Nuevo socio',
        ),
      },
      {
        path: 'giros',
        component: CrudPage,
        canActivate: [adminGuard],
        data: crud(
          'Giros comerciales',
          'giros',
          [
            { key: 'nombre', label: 'Nombre', required: true },
            { key: 'descripcion', label: 'Descripción' },
            { key: 'estado', label: 'Estado', type: 'boolean', defaultValue: true },
          ],
          'Catálogo de actividades comerciales disponibles',
        ),
      },
      {
        path: 'puestos',
        component: CrudPage,
        canActivate: [adminGuard],
        data: crud(
          'Puestos',
          'puestos',
          [
            { key: 'numero', label: 'Número', required: true ,  placeholder: 'Ej.: A-101'},
            { key: 'ubicacion', label: 'Ubicación', placeholder: 'Ej.: Primer piso - Ala A', },
            { key: 'inquilinoNombre', label: 'Inquilino' },
            { key: 'inquilinoDocumento', label: 'Documento inquilino' },
            {
              key: 'inquilinoTelefono',
              label: 'Teléfono inquilino',
              placeholder: '9 dígitos',
              pattern: '^[0-9]{9}$',
              patternMessage: 'El teléfono del inquilino debe tener exactamente 9 dígitos.',
            },
            { key: 'vigenciaInicio', label: 'Inicio vigencia', type: 'date' },
            { key: 'vigenciaFin', label: 'Fin vigencia', type: 'date' },
            {
              key: 'estado',
              label: 'Estado',
              type: 'select',
              required: true,
              defaultValue: 'DISPONIBLE',
              placeholder: 'Seleccione un estado...',
              options: [
                opt('Disponible', 'DISPONIBLE'),
                opt('Ocupado', 'OCUPADO'),
                opt('Mantenimiento', 'MANTENIMIENTO'),
                opt('Inactivo', 'INACTIVO'),
              ],
            },
            {
              key: 'socioId',
              label: 'Socio',
              type: 'select',
              source: 'socios',
              optionLabelKeys: ['codigo', 'nombres', 'apellidos'],
              valueType: 'number',
              allowEmpty: true,
              emptyLabel: 'Sin socio asociado',
            },
            {
              key: 'giroId',
              label: 'Giro comercial',
              type: 'select',
              source: 'giros',
              optionLabelKeys: ['nombre'],
              valueType: 'number',
              required: true,
              placeholder: 'Seleccione un giro...',
            },
          ],
          'Locales, inquilinos, vigencia y asociaciones comerciales','Nuevo puesto',
        ),
      },
      {
        path: 'servicios',
        component: CrudPage,
        canActivate: [adminGuard],
        data: crud(
          'Servicios',
          'servicios',
          [
            { key: 'nombre', label: 'Nombre', required: true },
            { key: 'descripcion', label: 'Descripción' },
            {
              key: 'recurrencia',
              label: 'Recurrencia',
              type: 'select',
              required: true,
              options: [
                opt('Mensual', 'MENSUAL'),
                opt('Trimestral', 'TRIMESTRAL'),
                opt('Anual', 'ANUAL'),
                opt('Eventual', 'EVENTUAL'),
                opt('Único', 'UNICO'),
              ],
            },
            { key: 'costo', label: 'Costo', type: 'number', required: true, min: 0 },
            {
              key: 'moneda',
              label: 'Moneda',
              type: 'select',
              required: true,
              options: [opt('Soles (PEN)', 'PEN'), opt('Dólares (USD)', 'USD')],
            },
            {
              key: 'cargoA',
              label: 'Cargo a',
              type: 'select',
              required: true,
              options: [opt('Puesto', 'PUESTO'), opt('Socio', 'SOCIO')],
            },
            {
              key: 'tipoCalculo',
              label: 'Tipo de cálculo',
              type: 'select',
              required: true,
              options: [opt('Fijo', 'FIJO'), opt('Por consumo', 'CONSUMO')],
            },
            { key: 'estado', label: 'Estado', type: 'boolean', defaultValue: true },
          ],
          'Conceptos cobrables, recurrencia, costo y destinatario', 'Nuevo servicio',
        ),
      },
      {
        path: 'bancos',
        component: CrudPage,
        canActivate: [adminGuard],
        data: crud(
          'Bancos y cuentas',
          'bancos',
          [
            { key: 'nombre', label: 'Banco', required: true },
            { key: 'numeroCuenta', label: 'Número de cuenta', required: true, placeholder: 'Ej.: 000-1234567', },
            {
              key: 'cci',
              label: 'CCI',
              placeholder: '20 dígitos',
              pattern: '^$|^[0-9]{20}$',
              patternMessage: 'El CCI debe tener 20 dígitos.',
            },
            {
              key: 'moneda',
              label: 'Moneda',
              type: 'select',
              required: true,
              options: [opt('Soles (PEN)', 'PEN'), opt('Dólares (USD)', 'USD')],
            },
            {
              key: 'tipoCuenta',
              label: 'Tipo de cuenta',
              type: 'select',
              allowEmpty: true,
              emptyLabel: 'Sin especificar',
              options: [
                opt('Cuenta corriente', 'CUENTA CORRIENTE'),
                opt('Cuenta de ahorros', 'CUENTA DE AHORROS'),
              ],
            },
            {
              key: 'saldoInicial',
              label: 'Saldo inicial',
              type: 'number',
              required: true,
              min: 0,
              defaultValue: 0,
            },
            { key: 'estado', label: 'Estado', type: 'boolean', defaultValue: true },
          ],
          'Cuentas bancarias disponibles para operaciones de caja','Nuevo banco',
        ),
      },
      { path: 'cuentas', component: Cuentas },
      { path: 'cobranza', component: Cobranza },
      { path: 'ingresos', component: Ingresos },
      { path: 'egresos', component: Egresos },
      { path: 'recibos', component: Recibos },
      { path: 'reportes', component: Reportes },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
