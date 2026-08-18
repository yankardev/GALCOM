-- ============================================================================
-- GALCOM - Galería Comercial
-- BASE DE DATOS FINAL DE DEMOSTRACIÓN
-- Compatible con el proyecto Angular + Spring Boot de GALCOM.
--
-- OBJETIVO:
--   - Que todos los integrantes del equipo trabajen con los mismos datos.
--   - Contener información suficiente para demostrar RF-01 a RF-33.
--   - Poder ejecutarse directamente en MySQL Workbench.
--
-- CREDENCIALES DEMO:
--   Administrador: admin  / admin123
--   Cajero:        cajero / cajero123
--
-- IMPORTANTE:
--   Este script ELIMINA galcom_db si ya existe y la crea nuevamente.
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS galcom_db;
CREATE DATABASE galcom_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE galcom_db;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 1. SEGURIDAD
-- ============================================================================

CREATE TABLE roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(120) UNIQUE,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_rol BIGINT NOT NULL,
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
) ENGINE=InnoDB;

-- ============================================================================
-- 2. CATÁLOGOS ADMINISTRATIVOS
-- ============================================================================

CREATE TABLE socios (
    id_socio BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    dni CHAR(8) UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(120),
    accion VARCHAR(50),
    etapa TINYINT NOT NULL,
    fecha_nacimiento DATE,
    estado TINYINT(1) NOT NULL DEFAULT 1,

    CONSTRAINT chk_socio_etapa
        CHECK (etapa IN (1,2,3)),
    CONSTRAINT chk_socio_telefono
        CHECK (telefono IS NULL OR CHAR_LENGTH(telefono) = 9)
) ENGINE=InnoDB;

CREATE TABLE giros (
    id_giro BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE puestos (
    id_puesto BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    ubicacion VARCHAR(150),
    inquilino_nombre VARCHAR(150),
    inquilino_documento VARCHAR(20),
    inquilino_telefono VARCHAR(20),
    vigencia_inicio DATE,
    vigencia_fin DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    id_socio BIGINT NULL,
    id_giro BIGINT NOT NULL,

    CONSTRAINT fk_puesto_socio
        FOREIGN KEY (id_socio) REFERENCES socios(id_socio) ON DELETE SET NULL,
    CONSTRAINT fk_puesto_giro
        FOREIGN KEY (id_giro) REFERENCES giros(id_giro),

    CONSTRAINT chk_puesto_estado
        CHECK (estado IN ('DISPONIBLE','OCUPADO','MANTENIMIENTO','INACTIVO')),
    CONSTRAINT chk_puesto_vigencia
        CHECK (
            vigencia_fin IS NULL
            OR vigencia_inicio IS NULL
            OR vigencia_fin >= vigencia_inicio
        )
) ENGINE=InnoDB;

CREATE TABLE bancos (
    id_banco BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    numero_cuenta VARCHAR(50) NOT NULL UNIQUE,
    cci VARCHAR(30) UNIQUE,
    moneda CHAR(3) NOT NULL DEFAULT 'PEN',
    tipo_cuenta VARCHAR(30),
    saldo_inicial DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado TINYINT(1) NOT NULL DEFAULT 1,

    CONSTRAINT chk_banco_moneda
        CHECK (moneda IN ('PEN','USD'))
) ENGINE=InnoDB;

CREATE TABLE servicios (
    id_servicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    recurrencia VARCHAR(20) NOT NULL,
    costo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    moneda CHAR(3) NOT NULL DEFAULT 'PEN',
    cargo_a VARCHAR(10) NOT NULL,
    tipo_calculo VARCHAR(10) NOT NULL DEFAULT 'FIJO',
    estado TINYINT(1) NOT NULL DEFAULT 1,

    CONSTRAINT chk_servicio_recurrencia
        CHECK (recurrencia IN ('MENSUAL','TRIMESTRAL','ANUAL','EVENTUAL','UNICO')),
    CONSTRAINT chk_servicio_moneda
        CHECK (moneda IN ('PEN','USD')),
    CONSTRAINT chk_servicio_cargo
        CHECK (cargo_a IN ('PUESTO','SOCIO')),
    CONSTRAINT chk_servicio_calculo
        CHECK (tipo_calculo IN ('FIJO','CONSUMO')),
    CONSTRAINT chk_servicio_costo
        CHECK (costo >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 3. CUENTAS POR COBRAR Y COBRANZA
-- ============================================================================

CREATE TABLE cuentas_por_cobrar (
    id_cuenta BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_servicio BIGINT NOT NULL,
    id_socio BIGINT NULL,
    id_puesto BIGINT NULL,
    periodo VARCHAR(20) NOT NULL,
    lectura_inicial DECIMAL(12,2),
    lectura_final DECIMAL(12,2),
    costo_unitario DECIMAL(12,2),
    monto DECIMAL(12,2) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_cuenta_servicio
        FOREIGN KEY (id_servicio) REFERENCES servicios(id_servicio),
    CONSTRAINT fk_cuenta_socio
        FOREIGN KEY (id_socio) REFERENCES socios(id_socio),
    CONSTRAINT fk_cuenta_puesto
        FOREIGN KEY (id_puesto) REFERENCES puestos(id_puesto),

    CONSTRAINT chk_cuenta_destino
        CHECK (
            (id_socio IS NOT NULL AND id_puesto IS NULL)
            OR
            (id_socio IS NULL AND id_puesto IS NOT NULL)
        ),
    CONSTRAINT chk_cuenta_estado
        CHECK (estado IN ('PENDIENTE','ABONADA','EXONERADA','ANULADA')),
    CONSTRAINT chk_cuenta_monto
        CHECK (monto >= 0),

    CONSTRAINT uq_cuenta_puesto_periodo
        UNIQUE (id_servicio, id_puesto, periodo),
    CONSTRAINT uq_cuenta_socio_periodo
        UNIQUE (id_servicio, id_socio, periodo)
) ENGINE=InnoDB;

CREATE TABLE recibos (
    id_recibo BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_correlativo VARCHAR(30) NOT NULL UNIQUE,
    tipo VARCHAR(15) NOT NULL DEFAULT 'INGRESO',
    id_usuario BIGINT NOT NULL,
    id_socio BIGINT NULL,
    id_puesto BIGINT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_total DECIMAL(12,2) NOT NULL,
    metodo_pago VARCHAR(20),
    estado VARCHAR(15) NOT NULL DEFAULT 'EMITIDO',

    CONSTRAINT fk_recibo_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_recibo_socio
        FOREIGN KEY (id_socio) REFERENCES socios(id_socio),
    CONSTRAINT fk_recibo_puesto
        FOREIGN KEY (id_puesto) REFERENCES puestos(id_puesto),

    CONSTRAINT chk_recibo_tipo
        CHECK (tipo IN ('INGRESO','EGRESO','BANCARIO')),
    CONSTRAINT chk_recibo_metodo
        CHECK (
            metodo_pago IS NULL
            OR metodo_pago IN ('EFECTIVO','TRANSFERENCIA','YAPE_PLIN','TARJETA')
        ),
    CONSTRAINT chk_recibo_estado
        CHECK (estado IN ('EMITIDO','ANULADO')),
    CONSTRAINT chk_recibo_monto
        CHECK (monto_total >= 0)
) ENGINE=InnoDB;

-- Usada por Spring Boot para obtener correlativos REC-000001, REC-000002, etc.
CREATE TABLE secuencia_recibos (
    id TINYINT PRIMARY KEY,
    ultimo BIGINT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE detalle_recibo (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_recibo BIGINT NOT NULL,
    id_cuenta BIGINT NOT NULL,
    monto_aplicado DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_detalle_recibo
        FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_cuenta
        FOREIGN KEY (id_cuenta) REFERENCES cuentas_por_cobrar(id_cuenta),

    CONSTRAINT uq_detalle_recibo_cuenta
        UNIQUE (id_recibo, id_cuenta),
    CONSTRAINT chk_detalle_monto
        CHECK (monto_aplicado >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 4. INGRESOS, EGRESOS Y MOVIMIENTOS BANCARIOS
-- ============================================================================

CREATE TABLE ingresos_externos (
    id_ingreso BIGINT AUTO_INCREMENT PRIMARY KEY,
    depositante VARCHAR(150) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    concepto VARCHAR(200) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    fecha DATE NOT NULL,
    observaciones VARCHAR(255),
    id_banco BIGINT NULL,
    id_usuario BIGINT NOT NULL,
    id_recibo BIGINT NULL,

    CONSTRAINT fk_ingreso_banco
        FOREIGN KEY (id_banco) REFERENCES bancos(id_banco),
    CONSTRAINT fk_ingreso_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_ingreso_recibo
        FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo),

    CONSTRAINT chk_ingreso_monto
        CHECK (monto > 0)
) ENGINE=InnoDB;

CREATE TABLE egresos (
    id_egreso BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    proveedor VARCHAR(150) NOT NULL,
    fecha DATE NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    impuesto DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    monto_total DECIMAL(12,2) NOT NULL,
    documento_asociado VARCHAR(100),
    motivo VARCHAR(255) NOT NULL,
    archivo_origen VARCHAR(255),
    estado VARCHAR(15) NOT NULL DEFAULT 'REGISTRADO',
    id_banco BIGINT NULL,
    id_usuario BIGINT NOT NULL,
    id_recibo BIGINT NULL,

    CONSTRAINT fk_egreso_banco
        FOREIGN KEY (id_banco) REFERENCES bancos(id_banco),
    CONSTRAINT fk_egreso_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_egreso_recibo
        FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo),

    -- Corrección: evita registrar dos veces el mismo comprobante/proveedor.
    CONSTRAINT uq_egreso_documento_proveedor
        UNIQUE (tipo_documento, numero_documento, proveedor),

    CONSTRAINT chk_egreso_estado
        CHECK (estado IN ('REGISTRADO','PROCESADO','ANULADO')),
    CONSTRAINT chk_egreso_subtotal
        CHECK (subtotal >= 0),
    CONSTRAINT chk_egreso_impuesto
        CHECK (impuesto >= 0),
    CONSTRAINT chk_egreso_total
        CHECK (monto_total > 0)
) ENGINE=InnoDB;

CREATE TABLE movimientos_bancarios (
    id_movimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_banco BIGINT NOT NULL,
    id_cuenta BIGINT NULL,
    id_usuario BIGINT NOT NULL,
    id_recibo BIGINT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_deposito DATE NOT NULL,
    numero_operacion VARCHAR(80),
    monto DECIMAL(12,2) NOT NULL,
    observaciones VARCHAR(255),

    CONSTRAINT fk_movimiento_banco
        FOREIGN KEY (id_banco) REFERENCES bancos(id_banco),
    CONSTRAINT fk_movimiento_cuenta
        FOREIGN KEY (id_cuenta) REFERENCES cuentas_por_cobrar(id_cuenta),
    CONSTRAINT fk_movimiento_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_movimiento_recibo
        FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo),

    CONSTRAINT chk_movimiento_tipo
        CHECK (tipo IN ('CANJE','DEPOSITO','RETIRO','TRANSFERENCIA')),
    CONSTRAINT chk_movimiento_monto
        CHECK (monto > 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 5. AUDITORÍA
-- ============================================================================

CREATE TABLE auditoria_movimientos (
    id_auditoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(50) NOT NULL,
    entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT,
    importe DECIMAL(12,2),
    detalle VARCHAR(255),

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ============================================================================
-- 6. ÍNDICES DE CONSULTA
-- ============================================================================

CREATE INDEX idx_socios_nombre
    ON socios(apellidos, nombres);

CREATE INDEX idx_puestos_socio
    ON puestos(id_socio);

CREATE INDEX idx_cuentas_estado_vencimiento
    ON cuentas_por_cobrar(estado, fecha_vencimiento);

CREATE INDEX idx_cuentas_socio
    ON cuentas_por_cobrar(id_socio);

CREATE INDEX idx_cuentas_puesto
    ON cuentas_por_cobrar(id_puesto);

CREATE INDEX idx_recibos_fecha
    ON recibos(fecha);

CREATE INDEX idx_ingresos_fecha
    ON ingresos_externos(fecha);

CREATE INDEX idx_egresos_fecha
    ON egresos(fecha);

CREATE INDEX idx_movimientos_banco_fecha
    ON movimientos_bancarios(id_banco, fecha_deposito);

-- ============================================================================
-- 7. DATOS DE SEGURIDAD
-- ============================================================================

INSERT INTO roles (id_rol, nombre) VALUES
(1, 'ADMINISTRADOR'),
(2, 'CAJERO');

-- BCrypt:
-- admin  / admin123
-- cajero / cajero123
INSERT INTO usuarios
(id_usuario, usuario, password, nombres, apellidos, correo, estado, id_rol)
VALUES
(
    1,
    'admin',
    '$2y$10$6dOswmVns6qwRkqHk26TyOZyRlfggfgW.i6DB64AomyPFbVKXpymK',
    'Administrador',
    'GALCOM',
    'admin@galcom.pe',
    1,
    1
),
(
    2,
    'cajero',
    '$2y$10$iczC3WMReyeiwWZ2NcKkFOByvCASe3HLrBeeQz4PEG0.X3uECGB/W',
    'Operador',
    'Caja',
    'caja@galcom.pe',
    1,
    2
);

-- ============================================================================
-- 8. DATOS MAESTROS DE DEMOSTRACIÓN
-- ============================================================================

INSERT INTO giros (id_giro, nombre, descripcion, estado) VALUES
(1, 'Ropa y Calzado',        'Venta de prendas de vestir y calzado', 1),
(2, 'Alimentos',             'Venta y preparación de alimentos', 1),
(3, 'Electrónica',           'Venta y reparación de equipos electrónicos', 1),
(4, 'Ferretería',            'Venta de herramientas y materiales', 1),
(5, 'Cosméticos',            'Productos de belleza y cuidado personal', 1),
(6, 'Juguetería',            'Venta de juguetes y artículos recreativos', 1),
(7, 'Librería y Papelería',  'Venta de libros, útiles e impresiones', 1);

-- Diez socios para probar listado, búsqueda, edición, etapas y generación masiva.
-- SOC-007 y SOC-010 repiten deliberadamente nombres/apellidos para probar
-- la opción "Socios únicos" de RF-18.
-- La columna accion NO es autoincremental ni UNIQUE porque el requisito no lo exige.
INSERT INTO socios
(id_socio, codigo, dni, nombres, apellidos, telefono, correo, accion, etapa, fecha_nacimiento, estado)
VALUES
(1,  'SOC-001', '70123451', 'María',     'López Rodríguez',       '987654321', 'maria.lopez@galcom.pe',       'A-001', 1, '1988-04-15', 1),
(2,  'SOC-002', '70123452', 'Carlos',    'Mendoza Torres',        '976543210', 'carlos.mendoza@galcom.pe',    'A-002', 1, '1985-09-22', 1),
(3,  'SOC-003', '70123453', 'Rosa',      'Huanca Quispe',         '965432109', 'rosa.huanca@galcom.pe',       'A-003', 2, '1990-01-10', 1),
(4,  'SOC-004', '70123454', 'Luis',      'Sánchez Pérez',         '954321098', 'luis.sanchez@galcom.pe',      'A-004', 2, '1982-12-03', 1),
(5,  'SOC-005', '70123455', 'Ana',       'Flores Ríos',           '943210987', 'ana.flores@galcom.pe',        'A-005', 3, '1994-06-17', 1),
(6,  'SOC-006', '70123456', 'Yancarlos', 'Calderon Espinola',     '916284243', 'yancarlos.calderon@galcom.pe','A-006', 2, '1990-05-22', 1),
(7,  'SOC-007', '70123457', 'Pamela',    'Ascoy Benites',         '915985215', 'pamela.ascoy@galcom.pe',      'A-007', 1, '1990-08-08', 1),
(8,  'SOC-008', '70123458', 'Jennyfer',  'Benites Rodríguez',     '951852456', 'jennyfer.benites@galcom.pe',  'A-008', 3, '1992-07-20', 1),
(9,  'SOC-009', '70123459', 'Ricardo',   'Torres Salazar',        '912345678', 'ricardo.torres@galcom.pe',    'A-009', 2, '1989-10-11', 1),
(10, 'SOC-010', '70123460', 'Pamela',    'Ascoy Benites',         '923456789', 'pamela.ascoy2@galcom.pe',     'A-010', 1, '1991-03-14', 1);

INSERT INTO bancos
(id_banco, nombre, numero_cuenta, cci, moneda, tipo_cuenta, saldo_inicial, estado)
VALUES
(1, 'BCP',        '191-2345678-0-12',    '00219100234567801234', 'PEN', 'CUENTA CORRIENTE', 125430.50, 1),
(2, 'Interbank',  '898-3008765432',       '00389800300876543210', 'PEN', 'CUENTA DE AHORROS',  48200.00, 1),
(3, 'BBVA',       '0011-0175-0200789456', '01117500020078945612', 'PEN', 'CUENTA CORRIENTE',  32100.75, 1),
(4, 'Scotiabank', '000-1234567',          '00900000123456789012', 'PEN', 'CUENTA CORRIENTE',  15000.00, 1);

INSERT INTO servicios
(id_servicio, nombre, descripcion, recurrencia, costo, moneda, cargo_a, tipo_calculo, estado)
VALUES
(1, 'Cuota Mensual de Puesto',   'Derecho de uso y mantenimiento del puesto comercial', 'MENSUAL', 250.00, 'PEN', 'PUESTO', 'FIJO',    1),
(2, 'Servicio de Agua',          'Consumo mensual de agua potable',                      'MENSUAL',   3.50, 'PEN', 'PUESTO', 'CONSUMO', 1),
(3, 'Servicio de Luz',           'Consumo mensual de energía eléctrica',                 'MENSUAL',   0.80, 'PEN', 'PUESTO', 'CONSUMO', 1),
(4, 'Limpieza y Mantenimiento',  'Limpieza de áreas comunes',                            'MENSUAL',  45.00, 'PEN', 'PUESTO', 'FIJO',    1),
(5, 'Cuota Extraordinaria',      'Aporte extraordinario para mejoras de la galería',     'EVENTUAL',120.00, 'PEN', 'SOCIO',  'FIJO',    1),
(6, 'Seguridad mensual',         'Servicio de vigilancia y seguridad de áreas comunes',  'MENSUAL',  65.00, 'PEN', 'PUESTO', 'FIJO',    1),
(7, 'Consumo de gas',            'Consumo mensual de gas del puesto',                    'MENSUAL',   4.50, 'PEN', 'PUESTO', 'CONSUMO', 1);

-- Ocho puestos ocupados, uno disponible y uno en mantenimiento.
-- El campo inquilino_documento permanece VARCHAR(20), ya que el requisito
-- indica "documento" y no exclusivamente DNI.
INSERT INTO puestos
(id_puesto, numero, ubicacion, inquilino_nombre, inquilino_documento, inquilino_telefono,
 vigencia_inicio, vigencia_fin, estado, id_socio, id_giro)
VALUES
(1,  'A-101', 'Primer piso - Ala A',  'Luis López Rodríguez',    '45879631', '951852701', '2026-01-01', '2026-12-31', 'OCUPADO',       1,    1),
(2,  'A-102', 'Primer piso - Ala A',  'Carlos Mendoza Torres',   '45879632', '951852702', '2026-02-01', '2027-01-31', 'OCUPADO',       2,    2),
(3,  'B-201', 'Segundo piso - Ala B', 'Rosa Huanca Quispe',      '45879633', '951852703', '2026-03-01', '2027-02-28', 'OCUPADO',       3,    3),
(4,  'B-202', 'Segundo piso - Ala B', 'Luis Sánchez Pérez',      '45879634', '951852704', '2026-04-01', '2027-03-31', 'OCUPADO',       4,    4),
(5,  'C-301', 'Tercer piso - Ala C',  'Ana Flores Ríos',         '45879635', '951852705', '2026-05-01', '2027-04-30', 'OCUPADO',       5,    5),
(6,  'C-302', 'Tercer piso - Ala C',  'José Ramírez Castillo',   '45879636', '951852706', '2026-06-01', '2027-05-31', 'OCUPADO',       NULL, 6),
(7,  'D-101', 'Primer piso - Ala D',  NULL,                       NULL,       NULL,        NULL,         NULL,         'DISPONIBLE',     NULL, 1),
(8,  'E-120', 'Segundo piso - Ala E', 'Pamela Ascoy Benites',    '45879638', '951852708', '2026-08-01', '2027-07-31', 'OCUPADO',       7,    7),
(9,  'E-210', 'Segundo piso - Ala E', 'Yancarlos Calderon',      '45879639', '951852709', '2026-08-01', '2027-07-31', 'OCUPADO',       6,    3),
(10, 'F-110', 'Primer piso - Ala F',  NULL,                       NULL,       NULL,        NULL,         NULL,         'MANTENIMIENTO',  NULL, 4);

-- ============================================================================
-- 9. CUENTAS POR COBRAR DE DEMOSTRACIÓN
--
-- Se dejan periodos posteriores (por ejemplo 2026-09) disponibles para que
-- el profesor pueda probar generación masiva sin chocar con duplicados.
-- ============================================================================

INSERT INTO cuentas_por_cobrar
(id_cuenta, id_servicio, id_socio, id_puesto, periodo,
 lectura_inicial, lectura_final, costo_unitario, monto,
 fecha_emision, fecha_vencimiento, estado, version)
VALUES
-- Cuentas de puestos
(1,  1, NULL, 1, '2026-08', NULL,   NULL,   250.00, 250.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),
(2,  1, NULL, 2, '2026-08', NULL,   NULL,   250.00, 250.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),
(3,  1, NULL, 3, '2026-08', NULL,   NULL,   250.00, 250.00, '2026-08-01', '2026-09-01', 'EXONERADA', 1),
(4,  4, NULL, 4, '2026-08', NULL,   NULL,    45.00,  45.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),
(5,  6, NULL, 1, '2026-08', NULL,   NULL,    65.00,  65.00, '2026-08-01', '2026-09-01', 'ABONADA',   1),
(6,  6, NULL, 2, '2026-08', NULL,   NULL,    65.00,  65.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),
(7,  6, NULL, 3, '2026-08', NULL,   NULL,    65.00,  65.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),

-- Consumo positivo: (125 - 100) * 4.50 = 112.50
(8,  7, NULL, 8, '2026-08', 100.00, 125.00,   4.50, 112.50, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),

-- Consumo negativo: se almacena monto 0 según RN-05
(9,  7, NULL, 9, '2026-08', 150.00, 140.00,   4.50,   0.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),

-- Cuentas de socios
(10, 5, 1,    NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),
(11, 5, 2,    NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'ABONADA',   1),
(12, 5, 6,    NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'ABONADA',   1),
(13, 5, 7,    NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'ABONADA',   1),
(14, 5, 8,    NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'EXONERADA', 1),
(15, 5, 9,    NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0),
(16, 5, 10,   NULL, '2026-08', NULL, NULL, 120.00, 120.00, '2026-08-01', '2026-09-01', 'PENDIENTE', 0);

-- ============================================================================
-- 10. RECIBOS, PAGOS Y CANJES DE DEMOSTRACIÓN
-- ============================================================================

INSERT INTO recibos
(id_recibo, numero_correlativo, tipo, id_usuario, id_socio, id_puesto,
 fecha, monto_total, metodo_pago, estado)
VALUES
(1, 'REC-000001', 'INGRESO',  1, 6,    NULL, '2026-08-15 10:20:00', 120.00, 'YAPE_PLIN',      'EMITIDO'),
(2, 'REC-000002', 'BANCARIO', 1, 7,    NULL, '2026-08-16 09:15:00', 120.00, 'TRANSFERENCIA',  'EMITIDO'),
(3, 'REC-000003', 'INGRESO',  1, NULL, 1,    '2026-08-16 11:30:00',  65.00, 'EFECTIVO',       'EMITIDO'),
(4, 'REC-000004', 'INGRESO',  1, NULL, NULL, '2026-08-16 15:10:00', 250.00, 'TRANSFERENCIA',  'EMITIDO'),
(5, 'REC-000005', 'EGRESO',   1, NULL, NULL, '2026-08-16 17:00:00', 590.00, 'TRANSFERENCIA',  'EMITIDO'),
(6, 'REC-000006', 'INGRESO',  1, NULL, NULL, '2026-08-17 09:40:00', 350.00, 'EFECTIVO',       'EMITIDO'),
(7, 'REC-000007', 'EGRESO',   1, NULL, NULL, '2026-08-17 13:20:00', 120.00, 'EFECTIVO',       'EMITIDO'),
(8, 'REC-000008', 'BANCARIO', 1, 2,    NULL, '2026-08-18 08:30:00', 120.00, 'TRANSFERENCIA',  'EMITIDO');

INSERT INTO secuencia_recibos (id, ultimo)
VALUES (1, 8);

INSERT INTO detalle_recibo
(id_detalle, id_recibo, id_cuenta, monto_aplicado)
VALUES
(1, 1, 12, 120.00),
(2, 2, 13, 120.00),
(3, 3, 5,   65.00),
(4, 8, 11, 120.00);

-- ============================================================================
-- 11. INGRESOS EXTERNOS
-- ============================================================================

INSERT INTO ingresos_externos
(id_ingreso, depositante, categoria, concepto, monto, fecha, observaciones,
 id_banco, id_usuario, id_recibo)
VALUES
(1, 'Juan Pérez', 'ALQUILER',
    'Alquiler temporal de espacio publicitario',
    250.00, '2026-08-16',
    'Ingreso externo de demostración para RF-25',
    2, 1, 4),

(2, 'Asociación de Comerciantes', 'APORTE',
    'Aporte para actividad institucional',
    350.00, '2026-08-17',
    'Aporte extraordinario para actividades de la galería',
    NULL, 1, 6);

-- ============================================================================
-- 12. EGRESOS
-- Incluye PROCESADO, REGISTRADO y ANULADO para demostrar RF-27, RF-28 y RF-30.
-- ============================================================================

INSERT INTO egresos
(id_egreso, tipo_documento, numero_documento, proveedor, fecha,
 subtotal, impuesto, monto_total, documento_asociado, motivo,
 archivo_origen, estado, id_banco, id_usuario, id_recibo)
VALUES
(1, 'FACTURA', 'F001-000101',
    'Servicios Eléctricos del Norte SAC',
    '2026-08-16', 500.00, 90.00, 590.00,
    'OS-2026-101',
    'Mantenimiento del sistema eléctrico',
    NULL, 'PROCESADO', 1, 1, 5),

(2, 'BOLETA', 'B001-000102',
    'Comercial López Rodríguez EIRL',
    '2026-08-17', 120.00, 0.00, 120.00,
    'OC-2026-102',
    'Compra de materiales de limpieza',
    NULL, 'PROCESADO', NULL, 1, 7),

(3, 'FACTURA', 'F001-000103',
    'Seguridad Integral Trujillo SAC',
    '2026-08-18', 300.00, 54.00, 354.00,
    'OS-2026-103',
    'Servicio de vigilancia',
    NULL, 'REGISTRADO', NULL, 1, NULL),

(4, 'RECIBO', 'R001-000104',
    'Mantenimiento Calderón SAC',
    '2026-08-18', 200.00, 36.00, 236.00,
    'OS-2026-104',
    'Mantenimiento de equipos informáticos',
    'egresos_demo.csv', 'ANULADO', NULL, 1, NULL);

-- ============================================================================
-- 13. MOVIMIENTOS BANCARIOS
-- ============================================================================

INSERT INTO movimientos_bancarios
(id_movimiento, id_banco, id_cuenta, id_usuario, id_recibo,
 tipo, fecha_deposito, numero_operacion, monto, observaciones)
VALUES
(1, 2, NULL, 1, 4,
    'DEPOSITO', '2026-08-16', 'OP-EXT-001', 250.00,
    'Ingreso externo por alquiler de espacio'),

(2, 1, NULL, 1, 5,
    'RETIRO', '2026-08-16', 'OP-EGR-001', 590.00,
    'Pago de mantenimiento eléctrico'),

(3, 1, 13, 1, 2,
    'CANJE', '2026-08-16', '987654321', 120.00,
    'Canje bancario de cuota extraordinaria - Pamela Ascoy'),

(4, 3, 11, 1, 8,
    'CANJE', '2026-08-18', '458963214', 120.00,
    'Canje bancario de cuota extraordinaria - Carlos Mendoza');

-- ============================================================================
-- 14. AUDITORÍA DE DEMOSTRACIÓN
-- ============================================================================

INSERT INTO auditoria_movimientos
(id_auditoria, id_usuario, fecha, accion, entidad, entidad_id, importe, detalle)
VALUES
(1, 1, '2026-08-15 10:20:00', 'ABONAR',   'CUENTA',          12, 120.00, 'Cuenta abonada en REC-000001'),
(2, 1, '2026-08-15 10:20:01', 'PAGO',     'RECIBO',           1, 120.00, 'Pago procesado REC-000001'),
(3, 1, '2026-08-16 09:15:00', 'CANJE',    'CUENTA',          13, 120.00, 'Canje bancario REC-000002'),
(4, 1, '2026-08-16 11:30:00', 'ABONAR',   'CUENTA',           5,  65.00, 'Cuenta abonada en REC-000003'),
(5, 1, '2026-08-16 15:10:00', 'REGISTRAR','INGRESO_EXTERNO',  1, 250.00, 'Alquiler temporal de espacio publicitario'),
(6, 1, '2026-08-16 17:00:00', 'PROCESAR', 'EGRESO',            1, 590.00, 'REC-000005'),
(7, 1, '2026-08-17 09:40:00', 'REGISTRAR','INGRESO_EXTERNO',  2, 350.00, 'Aporte para actividad institucional'),
(8, 1, '2026-08-17 13:20:00', 'PROCESAR', 'EGRESO',            2, 120.00, 'REC-000007'),
(9, 1, '2026-08-18 08:30:00', 'CANJE',    'CUENTA',           11, 120.00, 'Canje bancario REC-000008');

-- ============================================================================
-- 15. VALIDACIÓN RÁPIDA
-- Si el script terminó correctamente, estas consultas muestran los datos demo.
-- ============================================================================

SELECT 'GALCOM_DB creada correctamente' AS resultado;

SELECT
    (SELECT COUNT(*) FROM usuarios)             AS usuarios,
    (SELECT COUNT(*) FROM socios)               AS socios,
    (SELECT COUNT(*) FROM puestos)              AS puestos,
    (SELECT COUNT(*) FROM giros)                AS giros,
    (SELECT COUNT(*) FROM servicios)            AS servicios,
    (SELECT COUNT(*) FROM bancos)               AS bancos,
    (SELECT COUNT(*) FROM cuentas_por_cobrar)   AS cuentas,
    (SELECT COUNT(*) FROM recibos)              AS recibos,
    (SELECT COUNT(*) FROM ingresos_externos)    AS ingresos_externos,
    (SELECT COUNT(*) FROM egresos)              AS egresos,
    (SELECT COUNT(*) FROM movimientos_bancarios) AS movimientos_bancarios;

SELECT
    'admin' AS usuario,
    'admin123' AS password_demo,
    'ADMINISTRADOR' AS rol
UNION ALL
SELECT
    'cajero',
    'cajero123',
    'CAJERO';

-- ============================================================================
-- FIN DEL SCRIPT
-- Para volver a empezar desde cero, ejecute nuevamente todo el archivo.
-- ============================================================================
