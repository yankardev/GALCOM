# GALCOM — Galería Comercial

Sistema de Gestión Administrativa y de Caja desarrollado con **Spring Boot + Spring Data JPA + Spring Security/JWT + MySQL + Angular 22**.

## Estructura

```text
GALCOM/
├── backend/        Spring Boot REST
├── frontend/       Angular 22
├── database/       Script MySQL y CSV de ejemplo
└── documentacion/  Cobertura, pruebas y observaciones corregidas
```

## Requisitos locales

- Java 21+ (el proyecto se ha usado también con Java 24)
- MySQL 8+
- Node compatible con Angular 22 (en el equipo de pruebas usar Node 22.22.3+ o Node 24 compatible)
- npm

## Base de datos

Abrir MySQL Workbench y ejecutar completamente:

```text
database/GALCOM_DB_FINAL.sql
```

La base creada es `galcom_db`.

## Backend

La configuración usa variables de entorno para no publicar contraseñas:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/galcom_db}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```
Cada integrante debe configurar localmente DB_USERNAME y DB_PASSWORD
según su instalación de MySQL. La contraseña no se almacena en el repositorio.
En Windows puede definirlas antes de ejecutar o configurarlas en IntelliJ.

Puerto:

```text
http://localhost:9090
```

## Frontend

```powershell
cd frontend
npm install
npm start
```

Abrir:

```text
http://localhost:4200
```

## Usuarios demo

```text
Administrador
usuario: admin
contraseña: admin123

Cajero
usuario: cajero
contraseña: cajero123
```

## Mejoras aplicadas

- Código de socio automático y no editable.
- DNI/telefonía/fechas/combos con validaciones más claras.
- Modales y tablas CRUD con buscador y estados.
- Vencimiento de cuentas sugerido a +1 mes y editable.
- Egresos con total calculado desde subtotal + impuesto y recalculado en backend.
- JWT, guards/interceptor, autorización backend por rol.
- Cobranza, canje bancario, recibos correlativos, ingresos, egresos, CSV, reportes XLSX y dashboard.

Revisar:
- `documentacion/COBERTURA_REQUISITOS.md`
- `documentacion/OBSERVACIONES_CORREGIDAS.md`
- `documentacion/PRUEBAS_GUIADAS.md`
