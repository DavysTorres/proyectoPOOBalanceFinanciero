-- Esquema de referencia MySQL 8.0.16 o posterior (CHECK se aplica desde esa version).
-- Ejecutar en una base vacia seleccionada por el usuario. No elimina ni modifica tablas existentes.
CREATE TABLE usuarios (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(150) NOT NULL,
 username VARCHAR(80) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 rol VARCHAR(20) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 creado_en DATETIME(6) NOT NULL,
 CHECK (rol IN ('ADMINISTRADOR','JEFE','CAJERO')),
 CHECK (CHAR_LENGTH(TRIM(nombre))>0),
 CHECK (CHAR_LENGTH(TRIM(username))>0),
 CHECK (CHAR_LENGTH(TRIM(password_hash))>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE productos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(150) NOT NULL,
 precio_venta DECIMAL(19,4) NOT NULL,
 costo_compra DECIMAL(19,4) NOT NULL,
 stock INT NOT NULL DEFAULT 0,
 stock_minimo INT NOT NULL DEFAULT 0,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 CHECK (precio_venta>0 AND costo_compra>0),
 CHECK (stock>=0 AND stock_minimo>=0),
 CHECK (CHAR_LENGTH(TRIM(nombre))>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE ventas (
 id INT AUTO_INCREMENT PRIMARY KEY,
 cajero_id INT NOT NULL,
 fecha_hora DATETIME(6) NOT NULL,
 total DECIMAL(19,4) NOT NULL,
 FOREIGN KEY (cajero_id) REFERENCES usuarios(id),
 CHECK (total>=0),
 INDEX idx_ventas_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE detalle_venta (
 id INT AUTO_INCREMENT PRIMARY KEY,
 venta_id INT NOT NULL,
 producto_id INT NOT NULL,
 cantidad INT NOT NULL,
 precio_unitario DECIMAL(19,4) NOT NULL,
 costo_unitario DECIMAL(19,4) NOT NULL,
 FOREIGN KEY (venta_id) REFERENCES ventas(id),
 FOREIGN KEY (producto_id) REFERENCES productos(id),
 UNIQUE (venta_id,producto_id),
 CHECK (cantidad>0 AND precio_unitario>0 AND costo_unitario>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE compras (
 id INT AUTO_INCREMENT PRIMARY KEY,
 usuario_id INT NOT NULL,
 proveedor VARCHAR(150) NOT NULL,
 fecha_hora DATETIME(6) NOT NULL,
 total DECIMAL(19,4) NOT NULL,
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
 CHECK (total>0),
 CHECK (CHAR_LENGTH(TRIM(proveedor))>0),
 INDEX idx_compras_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE detalle_compra (
 id INT AUTO_INCREMENT PRIMARY KEY,
 compra_id INT NOT NULL,
 producto_id INT NOT NULL,
 cantidad INT NOT NULL,
 costo_unitario DECIMAL(19,4) NOT NULL,
 FOREIGN KEY (compra_id) REFERENCES compras(id),
 FOREIGN KEY (producto_id) REFERENCES productos(id),
 CHECK (cantidad>0 AND costo_unitario>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE gastos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 usuario_id INT NOT NULL,
 concepto VARCHAR(200) NOT NULL,
 monto DECIMAL(19,4) NOT NULL,
 fecha DATE NOT NULL,
 tipo VARCHAR(10) NOT NULL,
 dias_operacion INT NULL,
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
 CHECK (monto>0),
 CHECK (CHAR_LENGTH(TRIM(concepto))>0),
 CHECK ((tipo='FIJO' AND dias_operacion IS NOT NULL AND dias_operacion BETWEEN 1 AND 31)
        OR (tipo='VARIABLE' AND dias_operacion IS NULL)),
 INDEX idx_gastos_fecha (fecha,tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE costos_fijos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 concepto VARCHAR(200) NOT NULL,
 monto_mensual DECIMAL(19,4) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 CHECK (monto_mensual>0),
 CHECK (CHAR_LENGTH(TRIM(concepto))>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE cierres_diarios (
 id INT AUTO_INCREMENT PRIMARY KEY,
 fecha DATE NOT NULL UNIQUE,
 usuario_id INT NOT NULL,
 ingresos DECIMAL(19,4) NOT NULL,
 costos_variables DECIMAL(19,4) NOT NULL,
 costos_fijos DECIMAL(19,4) NOT NULL,
 resultado DECIMAL(19,4) NOT NULL,
 punto_equilibrio DECIMAL(19,4) NOT NULL,
 efectivo_sistema DECIMAL(19,4) NOT NULL,
 efectivo_contado DECIMAL(19,4) NOT NULL,
 diferencia DECIMAL(19,4) NOT NULL,
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
 CHECK (ingresos>=0 AND costos_variables>=0 AND costos_fijos>=0 AND punto_equilibrio>=0),
 CHECK (efectivo_contado>=0),
 CHECK (diferencia=efectivo_contado-efectivo_sistema)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE historial_cambios (
 id INT AUTO_INCREMENT PRIMARY KEY,
 venta_id INT NOT NULL,
 usuario_id INT NOT NULL,
 fecha_hora DATETIME(6) NOT NULL,
 valor_anterior DECIMAL(19,4) NOT NULL,
 valor_nuevo DECIMAL(19,4) NOT NULL,
 motivo VARCHAR(500) NOT NULL,
 FOREIGN KEY (venta_id) REFERENCES ventas(id),
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
 CHECK (valor_anterior>=0 AND valor_nuevo>=0),
 CHECK (CHAR_LENGTH(TRIM(motivo))>0),
 INDEX idx_historial_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE configuracion (
 clave VARCHAR(80) PRIMARY KEY,
 valor VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
