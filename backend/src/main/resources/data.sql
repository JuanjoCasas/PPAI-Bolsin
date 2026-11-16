-- =====================================================================
-- Datos de prueba del CU 28 Registrar recepción de bolsín
-- Se ejecuta automáticamente después de que Hibernate crea las tablas.
-- Nombres de estados, actores y tipos de documento según la ERS.
--
-- Usuario logueado: jperez (Juan Pérez, Encargado de Bolsines de la Comisión Médica Córdoba).
--
-- Bolsines (peso en gramos):
--   1001 (precinto 50001) Rosario -> Córdoba, Enviado  -> aparece; 2 remitos, 3 documentaciones
--   1002 (precinto 50002) Mendoza -> Córdoba, Enviado  -> aparece; 1 remito, 1 documentación
--   1003 (precinto 50003) Rosario -> Córdoba, Enviado  -> aparece, pero su documentación
--        quedó en EnBolsinSaliente (dato inconsistente a propósito): al confirmar,
--        el patrón State RECHAZA aceptar() y se hace rollback.
--   1004 (precinto 50004) Córdoba -> Rosario, Enviado  -> NO aparece (otra CM destino)
--   1005 (precinto 50005) Mendoza -> Córdoba, Cerrado  -> NO aparece (todavía no fue enviado)
-- =====================================================================

-- COMISIONES MÉDICAS
INSERT INTO comisiones_medicas (codigo, nombre, direccion, email, telefono) VALUES
(1, 'Comisión Médica Córdoba', 'Av. Colón 1200, Córdoba', 'ppaibolsines+cordoba@gmail.com', '0351-4000001'),
(2, 'Comisión Médica Rosario', 'Bv. Oroño 800, Rosario', 'ppaibolsines+rosario@gmail.com', '0341-4000002'),
(3, 'Comisión Médica Mendoza', 'Av. San Martín 500, Mendoza', 'ppaibolsines+mendoza@gmail.com', '0261-4000003'),
(4, 'Comisión Médica Central', 'Av. de Mayo 700, C.A.B.A.', 'ppaibolsines+central@gmail.com', '011-40000004');

-- ROLES
INSERT INTO roles (nombre, descripcion) VALUES
('EB', 'Encargado de Bolsines'),
('GCM', 'Gerente de Comisión Médica');

-- EMPLEADOS
INSERT INTO empleados (email, nombre, apellido, cm_codigo, rol_nombre) VALUES
('juan.perez@cm.test', 'Juan', 'Pérez', 1, 'EB'),
('maria.gomez@cm.test', 'María', 'Gómez', 2, 'EB'),
('carlos.ruiz@cm.test', 'Carlos', 'Ruiz', 3, 'EB');

-- USUARIOS
INSERT INTO usuarios (nombre, contrasena, empleado_email) VALUES
('jperez', 'test', 'juan.perez@cm.test'),
('mgomez', 'test', 'maria.gomez@cm.test');

-- SESIONES (la activa es la que no tiene fecha de fin)
INSERT INTO sesiones (id, fecha_inicio, fecha_fin, usuario_nombre) VALUES
(1, '2026-11-10 08:00:00', '2026-11-10 17:00:00', 'mgomez'),
(2, '2026-11-13 08:00:00', NULL, 'jperez');

-- TIPOS DE DOCUMENTO
INSERT INTO tipos_documento (nombre, descripcion) VALUES
('Expediente', 'Expediente de trámite'),
('Dictamen', 'Dictamen emitido por la comisión médica'),
('Estudio médico', 'Estudio médico del trabajador'),
('Carta documento', 'Carta documento recibida o enviada');

-- ESTADOS (solo ámbitos Bolsín y Remito; Documentación usa el patrón State)
INSERT INTO estados (id, ambito, nombre, descripcion) VALUES
(1,  'Bolsin', 'Creado', 'Bolsín registrado al momento de su armado'),
(2,  'Bolsin', 'Cerrado', 'Bolsín cerrado con un precinto'),
(3,  'Bolsin', 'Enviado', 'Bolsín retirado por el correo'),
(4,  'Bolsin', 'Recibido en CM destino', 'Bolsín recibido en la CM destino'),
(5,  'Bolsin', 'De baja', 'Bolsín eliminado'),
(6,  'Remito', 'Creado', 'Remito generado, todavía no incluido en un bolsín'),
(7,  'Remito', 'En bolsín saliente', 'Remito incluido en un bolsín por enviar'),
(8,  'Remito', 'En bolsín enviado', 'Remito en un bolsín retirado por el correo'),
(9,  'Remito', 'Recibido y Aceptado', 'Remito recibido y aceptado en la CM destino'),
(10, 'Remito', 'Recibido y aceptado parcial', 'No se recibió toda la documentación del remito');

-- BOLSINES (peso en gramos)
INSERT INTO bolsines (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo) VALUES
(1001, '2026-11-11', 50001, 2500, 2, 1),
(1002, '2026-11-11', 50002, 1200, 3, 1),
(1003, '2026-11-12', 50003, 800,  2, 1),
(1004, '2026-11-12', 50004, 1000, 1, 2),
(1005, '2026-11-12', 50005, 500,  3, 1);

-- CAMBIOS DE ESTADO DE BOLSÍN (el actual es el que no tiene fecha de fin)
INSERT INTO cambios_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_email, bolsin_numero) VALUES
(1,  '2026-11-11 09:00:00', '2026-11-11 12:00:00', 1, 'maria.gomez@cm.test', 1001),
(2,  '2026-11-11 12:00:00', '2026-11-12 08:00:00', 2, 'maria.gomez@cm.test', 1001),
(3,  '2026-11-12 08:00:00', NULL,                  3, 'maria.gomez@cm.test', 1001),
(4,  '2026-11-11 10:00:00', '2026-11-11 16:00:00', 2, 'carlos.ruiz@cm.test', 1002),
(5,  '2026-11-12 08:30:00', NULL,                  3, 'carlos.ruiz@cm.test', 1002),
(6,  '2026-11-12 09:00:00', '2026-11-12 14:00:00', 2, 'maria.gomez@cm.test', 1003),
(7,  '2026-11-12 14:00:00', NULL,                  3, 'maria.gomez@cm.test', 1003),
(8,  '2026-11-12 09:30:00', '2026-11-12 13:00:00', 2, 'juan.perez@cm.test',  1004),
(9,  '2026-11-12 13:00:00', NULL,                  3, 'juan.perez@cm.test',  1004),
(10, '2026-11-12 11:00:00', NULL,                  2, 'carlos.ruiz@cm.test', 1005);

-- REMITOS
INSERT INTO remitos (numero, fecha, cm_origen_codigo, cm_destino_codigo, estado_id, bolsin_numero) VALUES
(2001, '2026-11-10', 2, 1, 8, 1001),
(2002, '2026-11-10', 2, 1, 8, 1001),
(2003, '2026-11-10', 3, 1, 8, 1002),
(2004, '2026-11-11', 2, 1, 8, 1003),
(2005, '2026-11-11', 1, 2, 8, 1004),
(2006, '2026-11-11', 3, 1, 7, 1005);

-- DOCUMENTACIONES (estado_actual = nombre de la clase estado concreta)
INSERT INTO documentaciones (numero, asunto, fecha_pase, tipo_documento_nombre, cm_origen_codigo, estado_actual) VALUES
(3001, 'Expediente 4521/26 - Accidente laboral', '2026-11-09', 'Expediente', 2, 'EnBolsinEnviado'),
(3002, 'Dictamen de incapacidad parcial', '2026-11-09', 'Dictamen', 2, 'EnBolsinEnviado'),
(3003, 'Estudio audiométrico trabajador 33.456.789', '2026-11-09', 'Estudio médico', 2, 'EnBolsinEnviado'),
(3004, 'Expediente 8890/26 - Enfermedad profesional', '2026-11-09', 'Expediente', 3, 'EnBolsinEnviado'),
(3005, 'Carta documento ART - Rechazo de siniestro', '2026-11-10', 'Carta documento', 2, 'EnBolsinSaliente'),
(3006, 'Expediente 1200/26 - Divergencia en la prestación', '2026-11-10', 'Expediente', 1, 'EnBolsinEnviado'),
(3007, 'Estudio radiológico trabajador 28.111.222', '2026-11-10', 'Estudio médico', 3, 'EnBolsinSaliente');

-- DETALLES DE REMITO
INSERT INTO detalles_remito (id, area_cmc_destino, documentacion_numero, remito_numero) VALUES
(1, 'Mesa de entradas', 3001, 2001),
(2, 'Mesa de entradas', 3002, 2001),
(3, 'Archivo', 3003, 2002),
(4, 'Mesa de entradas', 3004, 2003),
(5, 'Mesa de entradas', 3005, 2004),
(6, 'Mesa de entradas', 3006, 2005),
(7, 'Archivo', 3007, 2006);

-- CAMBIOS DE ESTADO DE DOCUMENTACIÓN
INSERT INTO cambios_estado_documentacion (id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_email, documentacion_numero) VALUES
(1,  '2026-11-11 09:00:00', '2026-11-12 08:00:00', 'EnBolsinSaliente', 'maria.gomez@cm.test', 3001),
(2,  '2026-11-12 08:00:00', NULL,                  'EnBolsinEnviado',  'maria.gomez@cm.test', 3001),
(3,  '2026-11-11 09:00:00', '2026-11-12 08:00:00', 'EnBolsinSaliente', 'maria.gomez@cm.test', 3002),
(4,  '2026-11-12 08:00:00', NULL,                  'EnBolsinEnviado',  'maria.gomez@cm.test', 3002),
(5,  '2026-11-11 09:00:00', '2026-11-12 08:00:00', 'EnBolsinSaliente', 'maria.gomez@cm.test', 3003),
(6,  '2026-11-12 08:00:00', NULL,                  'EnBolsinEnviado',  'maria.gomez@cm.test', 3003),
(7,  '2026-11-11 10:00:00', '2026-11-12 08:30:00', 'EnBolsinSaliente', 'carlos.ruiz@cm.test', 3004),
(8,  '2026-11-12 08:30:00', NULL,                  'EnBolsinEnviado',  'carlos.ruiz@cm.test', 3004),
(9,  '2026-11-12 09:00:00', NULL,                  'EnBolsinSaliente', 'maria.gomez@cm.test', 3005),
(10, '2026-11-12 09:30:00', '2026-11-12 13:00:00', 'EnBolsinSaliente', 'juan.perez@cm.test',  3006),
(11, '2026-11-12 13:00:00', NULL,                  'EnBolsinEnviado',  'juan.perez@cm.test',  3006),
(12, '2026-11-12 11:00:00', NULL,                  'EnBolsinSaliente', 'carlos.ruiz@cm.test', 3007);

-- Las tablas con id autogenerado arrancan después de los ids cargados a mano
ALTER TABLE sesiones ALTER COLUMN id RESTART WITH 100;
ALTER TABLE cambios_estado_bolsin ALTER COLUMN id RESTART WITH 100;
ALTER TABLE cambios_estado_documentacion ALTER COLUMN id RESTART WITH 100;
