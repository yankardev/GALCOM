# GALCOM — Cobertura de requisitos funcionales

Esta matriz relaciona la Especificación de Requisitos con la implementación del proyecto.

| Requisito | Implementación principal | Estado |
|---|---|---|
| RF-01 | `POST /usuarios/login` + `AuthService` Angular | Implementado |
| RF-02 | `JWTAuthenticationFilter` + `authGuard` | Implementado |
| RF-03 | `Shell` muestra usuario/rol autenticado | Implementado |
| RF-04 | `AuthService.logout()` elimina sesión | Implementado |
| RF-05–RF-07 | CRUD Socios | Implementado |
| RF-08 | CRUD Giros | Implementado |
| RF-09–RF-11 | CRUD Puestos, socio/giro, inquilino y vigencia | Implementado |
| RF-12 | CRUD Bancos | Implementado |
| RF-13–RF-15 | CRUD Servicios, recurrencia, moneda, cargo y cálculo | Implementado |
| RF-16 | `POST /cuentas/generar-puestos` | Implementado |
| RF-17 | cálculo CONSUMO en `CuentaPorCobrarService` | Implementado |
| RF-18 | `POST /cuentas/generar-socios` con etapas y únicos | Implementado |
| RF-19–RF-20 | `GET /cobranza/socio/{id}` y `/puesto/{id}` | Implementado |
| RF-21 | selección ABONAR / EXONERAR en Cobranza | Implementado |
| RF-22 | total reactivo de cuentas seleccionadas | Implementado |
| RF-23 | `POST /cobranza/pagar`, transacción, detalle y recibo | Implementado |
| RF-24 | `POST /cobranza/canje` + movimiento bancario | Implementado |
| RF-25 | `POST /ingresos` + recibo automático | Implementado |
| RF-26 | ruta `/resumen` abierta en nueva ventana | Implementado |
| RF-27 | registro individual de egresos | Implementado |
| RF-28 | `POST /egresos/importar` para archivo CSV | Implementado |
| RF-29 | recibos de INGRESO por fecha + voucher | Implementado |
| RF-30 | egresos por mes + ver voucher + procesar/anular | Implementado |
| RF-31 | recibos BANCARIO por fecha + voucher | Implementado |
| RF-32 | reportes XLSX diarios, totales y mensuales | Implementado |
| RF-33 | XLSX de socios, no socios, egresos y bancos | Implementado |

## Reglas de negocio

- **RN-01:** Puesto puede asociarse a socio y giro.
- **RN-02:** Servicio se configura para PUESTO o SOCIO.
- **RN-03:** Cuenta puede quedar ABONADA o EXONERADA.
- **RN-04:** Recibos usan correlativo único `REC-000001`.
- **RN-05:** Consumo = diferencia positiva de lecturas × costo unitario.
- **RN-06:** Generación por etapas 1, 2, 3 y filtro de socios únicos.
- **RN-07:** Reportes diarios o mensuales según tipo.

## Arquitectura aplicada

```text
Angular 22
   ↓ HttpClient / JSON / Bearer JWT
Controller REST
   ↓
Service (reglas de negocio + @Transactional)
   ↓
JpaRepository / JPQL
   ↓
Entity JPA / Hibernate
   ↓
MySQL
```

Se conserva la organización trabajada en clase: Controller → Service → Repository, separando Model y Entity cuando corresponde.

## Requisitos no funcionales

| Requisito | Evidencia en GALCOM | Estado |
|---|---|---|
| RNF-01 | `JWTAuthenticationFilter` exige token Bearer en rutas protegidas | Implementado |
| RNF-02 | `WebSecurityConfig` restringe escrituras de catálogos a ADMINISTRADOR y los Services validan reglas | Implementado |
| RNF-03 | `AuthService.logout()` elimina `sessionStorage` y los guards bloquean rutas | Implementado |
| RNF-04 | Pago/canje/ingreso/egreso usan `@Transactional` para confirmar o revertir la operación completa | Implementado |
| RNF-05 | `secuencia_recibos` + bloqueo pesimista genera correlativos únicos dentro de la transacción | Implementado |
| RNF-06 | Reactive Forms, Bean Validation y `ApiExceptionHandler` muestran validaciones/mensajes | Implementado |
| RNF-07 | Buscadores y filtros en CRUD, cuentas, recibos y movimientos | Implementado |
| RNF-08 | Estados de carga, éxito y error en los flujos principales | Implementado |
| RNF-09 | Índices SQL en búsquedas frecuentes | Preparado; requiere prueba de rendimiento para acreditar ≤3 s |
| RNF-10 | Diseño web estándar/responsive | Requiere prueba final en Chrome, Edge y Firefox |
| RNF-11 | URL centralizada en `frontend/src/app/core/api.ts` | Implementado |
| RNF-12 | Angular → Controller → Service → Repository → Entity | Implementado |
| RNF-13 | Manejo de errores HTTP y backend con mensajes visibles | Implementado |
| RNF-14 | Tabla `auditoria_movimientos` registra usuario, fecha, entidad e importe en operaciones críticas | Implementado |
| RNF-15 | Formularios etiquetados y navegación con controles nativos | Preparado; requiere revisión final de teclado/lector |
| RNF-16 | Versiones declaradas y comandos de build documentados | Requiere ejecutar build limpio en el equipo de entrega |
