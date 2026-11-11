-- DATOS DE DEMOSTACION: ejecutar solamente en un esquema vacio con esquema.sql ya cargado.
-- Usuarios: demo_admin, demo_jefe, demo_cajero. Clave de demostracion: DemoPoo2026!
-- Los hashes se generan con el mismo servicio de autenticacion del proyecto.
START TRANSACTION;
INSERT INTO usuarios(nombre,username,password_hash,rol,activo,creado_en) VALUES
('Admin ejemplo','demo_admin','pbkdf2-sha256$600000$euGngOTXShW7AJwX8SBBRw==$IR0soQZAO0nkdj0WSigLa7Qkdi2B6uhCDrVFVc3cUSY=','ADMINISTRADOR',TRUE,'2026-11-05 08:00:00'),
('Jefe ejemplo','demo_jefe','pbkdf2-sha256$600000$LQNAKO19eFcR09B2NgYrOA==$seOimvb2FLe4CVkQwMKEDQsIniPleDnwfN6CTmAp29g=','JEFE',TRUE,'2026-11-05 08:00:00'),
('Cajero ejemplo','demo_cajero','pbkdf2-sha256$600000$lS6Rusbf/ZS/0xoyzG/aCA==$w5ad4hOC3IMWuFJC5ZfHHi+SvoON1z2nca8JlLmjB8c=','CAJERO',TRUE,'2026-11-05 08:00:00');
SET @jefe=(SELECT id FROM usuarios WHERE username='demo_jefe');
SET @cajero=(SELECT id FROM usuarios WHERE username='demo_cajero');
INSERT INTO productos(nombre,precio_venta,costo_compra,stock,stock_minimo,activo) VALUES
('Cafe ejemplo',10000,6000,50,5,TRUE),
('Pan ejemplo',2000,1000,50,5,TRUE),
('Leche ejemplo',5000,3500,50,5,TRUE),
('Arroz ejemplo',4500,3000,50,5,TRUE),
('Huevos ejemplo',18000,14000,50,5,TRUE),
('Azucar ejemplo',4000,2700,50,5,TRUE),
('Sal ejemplo',1500,800,50,5,TRUE),
('Aceite ejemplo',12000,9000,50,5,TRUE),
('Chocolate ejemplo',7000,4500,50,5,TRUE),
('Queso ejemplo',15000,11000,50,5,TRUE),
('Galletas ejemplo',3000,1800,50,5,TRUE),
('Jugo ejemplo',4000,2300,50,5,TRUE),
('Agua ejemplo',2500,1100,50,5,TRUE),
('Atun ejemplo',8000,5500,50,5,TRUE),
('Harina ejemplo',3500,2200,50,5,TRUE);
INSERT INTO costos_fijos(concepto,monto_mensual,activo) VALUES
('Arriendo ejemplo',11700000,TRUE),('Servicios ejemplo',900000,TRUE),('Internet ejemplo',300000,TRUE);
INSERT INTO configuracion(clave,valor) VALUES ('nombre','Tienda de ejemplo'),('dias_operacion','30'),
('hora_apertura','08:00'),('hora_cierre','18:00'),('umbral_amarillo','70'),('umbral_margen_bajo','15');
SET @cafe=(SELECT id FROM productos WHERE nombre='Cafe ejemplo');
SET @pan=(SELECT id FROM productos WHERE nombre='Pan ejemplo');
INSERT INTO ventas(cajero_id,fecha_hora,total) VALUES (@cajero,'2026-11-05 10:00:00',30000);
SET @venta=LAST_INSERT_ID();
INSERT INTO detalle_venta(venta_id,producto_id,cantidad,precio_unitario,costo_unitario) VALUES (@venta,@cafe,3,10000,6000);
UPDATE productos SET stock=stock-3 WHERE id=@cafe;
INSERT INTO ventas(cajero_id,fecha_hora,total) VALUES (@cajero,'2026-11-06 10:00:00',4000);
SET @venta=LAST_INSERT_ID();
INSERT INTO detalle_venta(venta_id,producto_id,cantidad,precio_unitario,costo_unitario) VALUES (@venta,@pan,2,2000,1000);
UPDATE productos SET stock=stock-2 WHERE id=@pan;
INSERT INTO compras(usuario_id,proveedor,fecha_hora,total) VALUES (@jefe,'Proveedor ejemplo','2026-11-06 11:00:00',30000);
SET @compra=LAST_INSERT_ID();
INSERT INTO detalle_compra(compra_id,producto_id,cantidad,costo_unitario) VALUES (@compra,@cafe,5,6000);
UPDATE productos SET stock=stock+5 WHERE id=@cafe;
INSERT INTO gastos(usuario_id,concepto,monto,fecha,tipo,dias_operacion) VALUES
(@jefe,'Transporte ejemplo',2000,'2026-11-05','VARIABLE',NULL);
COMMIT;
