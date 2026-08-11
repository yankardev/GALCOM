-- ============================================================================
-- GALCOM - Galería Comercial
-- Base de datos inicial v1
-- Basada en la Especificación de Requisitos del Sistema de Gestión
-- Administrativa y de Caja y adaptada al proyecto del curso DAW I.
-- ============================================================================

DROP DATABASE IF EXISTS galcom_db;
CREATE DATABASE galcom_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE galcom_db;

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
    etapa TINYINT,
    fecha_nacimiento DATE,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CHECK (etapa IS NULL OR etapa IN (1,2,3))
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
    FOREIGN KEY (id_socio) REFERENCES socios(id_socio) ON DELETE SET NULL,
    FOREIGN KEY (id_giro) REFERENCES giros(id_giro),
    CHECK (estado IN ('DISPONIBLE','OCUPADO','MANTENIMIENTO','INACTIVO')),
    CHECK (vigencia_fin IS NULL OR vigencia_inicio IS NULL OR vigencia_fin >= vigencia_inicio)
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
    CHECK (recurrencia IN ('MENSUAL','TRIMESTRAL','ANUAL','EVENTUAL','UNICO')),
    CHECK (moneda IN ('PEN','USD')),
    CHECK (cargo_a IN ('PUESTO','SOCIO')),
    CHECK (tipo_calculo IN ('FIJO','CONSUMO')),
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
    FOREIGN KEY (id_servicio) REFERENCES servicios(id_servicio),
    FOREIGN KEY (id_socio) REFERENCES socios(id_socio),
    FOREIGN KEY (id_puesto) REFERENCES puestos(id_puesto),
    CHECK ((id_socio IS NOT NULL AND id_puesto IS NULL) OR
           (id_socio IS NULL AND id_puesto IS NOT NULL)),
    CHECK (estado IN ('PENDIENTE','ABONADA','EXONERADA','ANULADA')),
    CHECK (monto >= 0)
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
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_socio) REFERENCES socios(id_socio),
    FOREIGN KEY (id_puesto) REFERENCES puestos(id_puesto),
    CHECK (tipo IN ('INGRESO','EGRESO','BANCARIO')),
    CHECK (metodo_pago IS NULL OR metodo_pago IN ('EFECTIVO','TRANSFERENCIA','YAPE_PLIN','TARJETA')),
    CHECK (estado IN ('EMITIDO','ANULADO')),
    CHECK (monto_total >= 0)
) ENGINE=InnoDB;

CREATE TABLE detalle_recibo (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_recibo BIGINT NOT NULL,
    id_cuenta BIGINT NOT NULL,
    monto_aplicado DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo) ON DELETE CASCADE,
    FOREIGN KEY (id_cuenta) REFERENCES cuentas_por_cobrar(id_cuenta),
    UNIQUE (id_recibo, id_cuenta),
    CHECK (monto_aplicado >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 4. INGRESOS, EGRESOS Y BANCO
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
    FOREIGN KEY (id_banco) REFERENCES bancos(id_banco),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo),
    CHECK (monto > 0)
) ENGINE=InnoDB;

CREATE TABLE egresos (
    id_egreso BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(30),
    numero_documento VARCHAR(50),
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
    FOREIGN KEY (id_banco) REFERENCES bancos(id_banco),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo),
    CHECK (estado IN ('REGISTRADO','PROCESADO','ANULADO')),
    CHECK (monto_total >= 0)
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
    FOREIGN KEY (id_banco) REFERENCES bancos(id_banco),
    FOREIGN KEY (id_cuenta) REFERENCES cuentas_por_cobrar(id_cuenta),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_recibo) REFERENCES recibos(id_recibo),
    CHECK (tipo IN ('CANJE','DEPOSITO','RETIRO','TRANSFERENCIA')),
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
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ============================================================================
-- 6. ÍNDICES
-- ============================================================================

CREATE INDEX idx_socios_nombre ON socios(apellidos, nombres);
CREATE INDEX idx_puestos_socio ON puestos(id_socio);
CREATE INDEX idx_cuentas_estado_vencimiento ON cuentas_por_cobrar(estado, fecha_vencimiento);
CREATE INDEX idx_cuentas_socio ON cuentas_por_cobrar(id_socio);
CREATE INDEX idx_cuentas_puesto ON cuentas_por_cobrar(id_puesto);
CREATE INDEX idx_recibos_fecha ON recibos(fecha);
CREATE INDEX idx_ingresos_fecha ON ingresos_externos(fecha);
CREATE INDEX idx_egresos_fecha ON egresos(fecha);
CREATE INDEX idx_movimientos_banco_fecha ON movimientos_bancarios(id_banco, fecha_deposito);

-- ============================================================================
-- 7. DATOS INICIALES
-- Contraseña del usuario admin: admin123
-- El valor almacenado es BCrypt, no texto plano.
-- ============================================================================

INSERT INTO roles (nombre) VALUES ('ADMINISTRADOR'), ('CAJERO');

INSERT INTO usuarios (usuario, password, nombres, apellidos, correo, id_rol)
VALUES (
    'admin',
    '$2y$10$6dOswmVns6qwRkqHk26TyOZyRlfggfgW.i6DB64AomyPFbVKXpymK',
    'Administrador',
    'GALCOM',
    'admin@galcom.pe',
    1
);

INSERT INTO giros (nombre, descripcion) VALUES
('Ropa y Calzado', 'Venta de prendas de vestir y calzado'),
('Alimentos', 'Venta y preparación de alimentos'),
('Electrónica', 'Venta y reparación de equipos electrónicos'),
('Ferretería', 'Venta de herramientas y materiales'),
('Cosméticos', 'Productos de belleza y cuidado personal'),
('Juguetería', 'Venta de juguetes y artículos recreativos');

INSERT INTO socios (codigo, dni, nombres, apellidos, telefono, correo, accion, etapa, fecha_nacimiento) VALUES
('SOC-001','12345678','María','García López','987654321','maria@correo.pe','A-001',1,'1988-04-15'),
('SOC-002','23456789','Carlos','Mendoza Torres','976543210','carlos@correo.pe','A-002',1,'1985-09-22'),
('SOC-003','34567890','Rosa','Huanca Quispe','965432109','rosa@correo.pe','A-003',2,'1990-01-10'),
('SOC-004','45678901','Luis','Sánchez Pérez','954321098','luis@correo.pe','A-004',2,'1982-12-03'),
('SOC-005','56789012','Ana','Flores Ríos','943210987','ana@correo.pe','A-005',3,'1994-06-17'),
('SOC-006','67890123','Jorge','Vargas Cárdenas','932109876','jorge@correo.pe','A-006',3,'1987-08-29');

INSERT INTO puestos (numero, ubicacion, estado, id_socio, id_giro) VALUES
('A-101','Primer nivel - Ala A','OCUPADO',1,1),
('B-205','Segundo nivel - Ala B','OCUPADO',2,2),
('C-312','Tercer nivel - Ala C','OCUPADO',3,3),
('A-203','Segundo nivel - Ala A','OCUPADO',4,4),
('D-108','Primer nivel - Ala D','OCUPADO',5,5),
('B-401','Cuarto nivel - Ala B','OCUPADO',6,6),
('C-115','Primer nivel - Ala C','DISPONIBLE',NULL,1);

INSERT INTO bancos (nombre, numero_cuenta, cci, moneda, tipo_cuenta, saldo_inicial) VALUES
('BCP','191-2345678-0-12','00219100234567801234','PEN','CUENTA CORRIENTE',125430.50),
('Interbank','898-3008765432','00389800300876543210','PEN','CUENTA DE AHORROS',48200.00),
('BBVA','0011-0175-0200789456','01117500020078945612','PEN','CUENTA CORRIENTE',32100.75);

INSERT INTO servicios (nombre, descripcion, recurrencia, costo, moneda, cargo_a, tipo_calculo) VALUES
('Cuota Mensual de Puesto','Derecho de uso y mantenimiento del puesto comercial','MENSUAL',250.00,'PEN','PUESTO','FIJO'),
('Servicio de Agua','Consumo mensual de agua potable','MENSUAL',3.50,'PEN','PUESTO','CONSUMO'),
('Servicio de Luz','Consumo mensual de energía eléctrica','MENSUAL',0.80,'PEN','PUESTO','CONSUMO'),
('Limpieza y Mantenimiento','Limpieza de áreas comunes','MENSUAL',45.00,'PEN','PUESTO','FIJO'),
('Cuota Extraordinaria','Aportes extraordinarios para mejoras de la galería','EVENTUAL',120.00,'PEN','SOCIO','FIJO');

-- Ejemplos de cuentas por cobrar para pruebas posteriores.
INSERT INTO cuentas_por_cobrar
(id_servicio, id_puesto, periodo, monto, fecha_emision, fecha_vencimiento, estado)
VALUES
(1,3,'2026-07',250.00,'2026-07-01','2026-07-31','PENDIENTE'),
(1,1,'2026-08',250.00,'2026-08-01','2026-08-31','PENDIENTE'),
(1,2,'2026-08',250.00,'2026-08-01','2026-08-31','PENDIENTE'),
(4,4,'2026-08',45.00,'2026-08-01','2026-08-31','PENDIENTE');
