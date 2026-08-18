# Ajustes de interfaz y Socios

- Código de socio generado automáticamente por backend con formato `SOC-XXX` a partir del ID autogenerado por MySQL.
- El formulario de alta ya no solicita el código.
- Al editar, el código se muestra como solo lectura y no se modifica desde Angular.
- `Acción` se presenta como `N.º de acción` y permanece opcional.
- Los mantenimientos genéricos ahora usan tabla + buscador + botón de alta + formulario modal.
- Se mejoraron badges, hover, acciones y adaptación responsive.
- Se corrigió el tipado de `forkJoin` para Angular/TypeScript.

## Prueba recomendada
1. Crear socio sin código; el backend debe devolver `SOC-XXX`.
2. Editar el socio; el código debe aparecer de solo lectura y conservarse.
3. Verificar búsqueda por código/DNI/nombre y confirmación de eliminación.
4. Revisar Giros, Puestos, Servicios y Bancos: todos usan el mismo patrón moderno de tabla + modal.
