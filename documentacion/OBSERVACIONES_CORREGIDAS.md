# GALCOM — Observaciones corregidas

Esta versión incorpora las observaciones funcionales y de usabilidad detectadas durante las pruebas manuales.

## Socios
- El `id` de MySQL sigue siendo `AUTO_INCREMENT` y nunca se solicita al usuario.
- El código de negocio `SOC-XXX` se genera en el backend después de insertar el socio y no se pide al crear.
- Al editar, el código se conserva y se muestra como solo lectura.
- DNI: si se ingresa, debe tener 8 dígitos y no puede repetirse.
- Teléfono: si se ingresa, debe tener exactamente 9 dígitos; ahora también aparece en el listado.
- Correo: validación de formato.
- `Acción` se presenta como **N.º de acción (participación)**, con ayuda contextual y como dato opcional, porque la especificación no define una regla adicional de formato/unicidad.
- Etapa: obligatoria y restringida a 1, 2 o 3; el texto de guía del combo no puede seleccionarse.
- Fecha de nacimiento: no admite hoy ni fechas futuras.
- Estado: Activo por defecto, con opciones Activo/Inactivo reales.
- Mensaje específico para DNI duplicado y manejo más claro de errores de integridad.

## Combos y formularios
- Los textos `Seleccione...` son placeholders deshabilitados, no valores válidos.
- Los campos opcionales usan opciones explícitas como `Sin socio asociado`, `Sin especificar` o `Efectivo / sin banco`.
- Se agregaron mensajes de validación más específicos y etiquetas accesibles en acciones y búsquedas.

## Puestos
- Teléfono de inquilino: si se ingresa, debe tener 9 dígitos.
- La fecha fin de vigencia no puede ser anterior al inicio, validado en frontend y backend.
- Estado por defecto: `DISPONIBLE`.
- Socio opcional con opción explícita `Sin socio asociado`; giro sigue siendo obligatorio.

## Cuentas por cobrar
- La fecha de emisión se inicializa con la fecha actual.
- GALCOM propone automáticamente un vencimiento **un mes después** de la emisión; el usuario puede modificarlo.
- El período se sugiere en formato `AAAA-MM` y el backend valida ese formato.
- Los servicios por consumo hacen obligatorias las lecturas inicial y final.
- Los títulos de los combos no se pueden seleccionar como datos.

## Egresos
- Número de documento, proveedor, fecha y motivo se validan antes del envío.
- El usuario ingresa un único **monto del comprobante** y selecciona el tratamiento del IGV: `INCLUIDO`, `ADICIONAL` o `NO_APLICA`.
- El frontend calcula automáticamente subtotal, impuesto y monto total según la opción elegida y los muestra como valores calculados.
- El backend vuelve a calcular el monto total a partir de subtotal e impuesto para evitar inconsistencias o manipulación desde el frontend.
- Se evita registrar el mismo comprobante para el mismo proveedor mediante validación de aplicación y restricción única en la base de datos.
- Se conserva la importación CSV; si aparece un registro inválido o duplicado, la importación se cancela y la transacción se revierte.

## Diseño
- Se conserva la identidad azul GALCOM con variantes sobrias.
- CRUD con tabla, búsqueda, badges, modal de creación/edición y acciones compactas.
- Dashboard real conectado al backend.
- Layout responsive, ayudas de campo y mensajes visibles de éxito/error.
