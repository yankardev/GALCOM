# Pruebas guiadas de GALCOM

## Preparación

1. Ejecutar `database/GALCOM_DB_v1.sql` en MySQL.
2. Configurar `DB_USERNAME` y `DB_PASSWORD` para el backend.
3. Iniciar Spring Boot en `9090`.
4. En `frontend`: `npm install` y luego `npm start`.
5. Abrir `http://localhost:4200`.

Credenciales de prueba:
- Administrador: `admin / admin123`
- Cajero: `cajero / cajero123`

## 1. Seguridad
- Iniciar sesión con administrador.
- Cerrar sesión y verificar que desaparezca la credencial local.
- Intentar abrir `/dashboard` sin sesión y comprobar redirección al login.
- Entrar como cajero y comprobar que no puede modificar catálogos administrativos.

## 2. Socios
Registrar un socio de prueba:
- DNI: `74125896`
- Nombres: `Roberto`
- Apellidos: `Salazar Medina`
- Teléfono: `987456123`
- Correo: `roberto.salazar@correo.pe`
- N.º de acción: `A-125`
- Etapa: `2`
- Fecha de nacimiento: `1989-03-14`

Comprobar:
- No solicita código al crear.
- Genera `SOC-XXX` automáticamente.
- El teléfono aparece en el listado.
- `123` como teléfono es rechazado.
- DNI duplicado muestra mensaje específico.
- El placeholder de Etapa no puede guardarse.
- Una fecha de nacimiento futura es rechazada.
- Editar conserva el código.

## 3. Giros y puestos
- Crear un giro y verificar que aparece en el combo de Puestos.
- Crear un puesto asociando giro y, opcionalmente, socio.
- Probar vigencia fin anterior a inicio: debe rechazarse.
- Teléfono de inquilino, si se usa, debe tener 9 dígitos.

## 4. Servicios
- Crear servicio FIJO para PUESTO.
- Crear servicio CONSUMO para PUESTO.
- Crear servicio FIJO para SOCIO.
- Verificar recurrencia, moneda, destinatario y tipo de cálculo.

## 5. Cuentas por cobrar
- Crear cuenta individual FIJA y confirmar monto.
- Crear cuenta CONSUMO con lectura inicial/final y comprobar diferencia positiva × costo unitario.
- Cambiar emisión y comprobar que vencimiento se sugiere a +1 mes; modificarlo manualmente para verificar que sigue editable.
- Generar por puestos ocupados y repetir la misma operación para confirmar control de duplicados.
- Generar por socios filtrando etapas y socios únicos.

## 6. Cobranza
- Buscar por puesto y por socio.
- Marcar cuentas ABONAR/EXONERAR y verificar total.
- Procesar pago y comprobar recibo correlativo.
- Realizar canje bancario de una cuenta de socio.
- Abrir resumen en nueva ventana.

## 7. Ingresos externos
- Registrar depositante, categoría, concepto, monto y fecha.
- Verificar recibo automático.
- Probar ingreso con banco y en efectivo.

## 8. Egresos
Registrar:
- Tipo: Factura
- Número: `F001-00245`
- Proveedor: `Electro Norte SAC`
- Subtotal: `450.00`
- Impuesto: `81.00`
- Motivo: `Reparación eléctrica`

Comprobar:
- Total se muestra automáticamente como `531.00`.
- No se puede editar el total manualmente.
- Procesar genera comprobante.
- Anular actualiza el estado.
- Importar `database/egresos_ejemplo.csv`.

## 9. Recibos
- Filtrar por fecha y por tipo INGRESO, EGRESO y BANCARIO.
- Abrir voucher de un recibo.
- Imprimir.

## 10. Reportes
- Descargar MOVIMIENTOS_DIARIOS por fecha.
- Descargar TOTALES y MENSUAL por mes.
- Descargar SOCIOS, NO_SOCIOS, EGRESOS y BANCOS.

## Validación técnica final

```powershell
# backend
cd backend
.\mvnw.cmd test

# frontend
cd ..\frontend
npm install
npm run build
```

La validación de RNF-09 (<= 3 s), RNF-10 (Chrome/Edge/Firefox), RNF-15 (teclado/lector) y RNF-16 (build limpio) debe ejecutarse en el equipo de entrega.
