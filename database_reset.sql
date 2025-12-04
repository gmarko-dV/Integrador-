-- ========================================
-- SCRIPT DE RESET COMPLETO DE BASE DE DATOS
-- Compatible con: Supabase, Spring Boot, Django
-- ========================================

-- ADVERTENCIA: Este script eliminará TODOS los datos existentes
-- Ejecutar solo si estás seguro de querer empezar desde cero

BEGIN;

-- ========================================
-- PASO 1: ELIMINAR TABLAS EXISTENTES
-- (en orden inverso de dependencias)
-- ========================================

DROP TABLE IF EXISTS historial_busqueda CASCADE;
DROP TABLE IF EXISTS notificaciones CASCADE;
DROP TABLE IF EXISTS imagenes CASCADE;
DROP TABLE IF EXISTS anuncios CASCADE;
DROP TABLE IF EXISTS categorias_vehiculo CASCADE;
DROP TABLE IF EXISTS vehiculos CASCADE;

-- ========================================
-- PASO 2: CREAR TABLAS
-- ========================================

-- Tabla: categorias_vehiculo
CREATE TABLE categorias_vehiculo (
    id_categoria BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    codigo VARCHAR(50) UNIQUE,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    url_imagen TEXT,
    imagen_url TEXT
);

-- Tabla: vehiculos
CREATE TABLE vehiculos (
    id_vehiculo BIGSERIAL PRIMARY KEY,
    placa VARCHAR(20) UNIQUE,
    descripcion_api TEXT,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    anio_registro_api VARCHAR(10),
    vin VARCHAR(100),
    uso VARCHAR(200),
    propietario TEXT,
    delivery_point TEXT,
    fecha_registro_api TIMESTAMPTZ,
    image_url_api TEXT,
    datos_api JSONB,
    fecha_actualizacion_api TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    tipo_vehiculo VARCHAR(50)
);

-- Tabla: anuncios
CREATE TABLE anuncios (
    id_anuncio BIGSERIAL PRIMARY KEY,
    id_usuario VARCHAR(255) NOT NULL,
    titulo VARCHAR(200),
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL CHECK (anio >= 1900),
    kilometraje INTEGER NOT NULL CHECK (kilometraje >= 0),
    precio NUMERIC(12, 2) NOT NULL CHECK (precio > 0),
    descripcion TEXT NOT NULL,
    email_contacto VARCHAR(255),
    telefono_contacto VARCHAR(20),
    tipo_vehiculo VARCHAR(50),
    id_categoria BIGINT REFERENCES categorias_vehiculo(id_categoria) ON DELETE SET NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla: imagenes
CREATE TABLE imagenes (
    id_imagen BIGSERIAL PRIMARY KEY,
    id_anuncio BIGINT NOT NULL REFERENCES anuncios(id_anuncio) ON DELETE CASCADE,
    url_imagen TEXT NOT NULL,
    nombre_archivo VARCHAR(255),
    tipo_archivo VARCHAR(50),
    tamano_archivo BIGINT,
    fecha_subida TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    orden INTEGER NOT NULL DEFAULT 0
);

-- Tabla: notificaciones
CREATE TABLE notificaciones (
    id_notificacion BIGSERIAL PRIMARY KEY,
    id_usuario INTEGER,
    id_vendedor VARCHAR(255),
    id_comprador VARCHAR(255),
    nombre_comprador VARCHAR(255),
    email_comprador VARCHAR(255),
    id_anuncio BIGINT REFERENCES anuncios(id_anuncio) ON DELETE SET NULL,
    titulo VARCHAR(200),
    mensaje TEXT,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metadata JSONB,
    tipo VARCHAR(50) NOT NULL DEFAULT 'interes'
);

-- Tabla: historial_busqueda
CREATE TABLE historial_busqueda (
    id_historial BIGSERIAL PRIMARY KEY,
    id_usuario VARCHAR(255),
    placa_consultada VARCHAR(20),
    fecha_consulta TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resultado_api JSONB
);

-- ========================================
-- PASO 3: CREAR ÍNDICES
-- ========================================

-- Índices para anuncios
CREATE INDEX idx_anuncios_id_usuario ON anuncios(id_usuario);
CREATE INDEX idx_anuncios_activo ON anuncios(activo);
CREATE INDEX idx_anuncios_fecha_creacion ON anuncios(fecha_creacion DESC);
CREATE INDEX idx_anuncios_tipo_vehiculo ON anuncios(tipo_vehiculo);
CREATE INDEX idx_anuncios_id_categoria ON anuncios(id_categoria);

-- Índices para imagenes
CREATE INDEX idx_imagenes_id_anuncio ON imagenes(id_anuncio);
CREATE INDEX idx_imagenes_orden ON imagenes(id_anuncio, orden);

-- Índices para notificaciones
CREATE INDEX idx_notificaciones_id_vendedor ON notificaciones(id_vendedor);
CREATE INDEX idx_notificaciones_id_comprador ON notificaciones(id_comprador);
CREATE INDEX idx_notificaciones_id_anuncio ON notificaciones(id_anuncio);
CREATE INDEX idx_notificaciones_leido ON notificaciones(leido);
CREATE INDEX idx_notificaciones_fecha_creacion ON notificaciones(fecha_creacion DESC);

-- Índices para historial_busqueda
CREATE INDEX idx_historial_id_usuario ON historial_busqueda(id_usuario);
CREATE INDEX idx_historial_fecha_consulta ON historial_busqueda(fecha_consulta DESC);

-- Índices para vehiculos
CREATE INDEX idx_vehiculos_placa ON vehiculos(placa);
CREATE INDEX idx_vehiculos_tipo_vehiculo ON vehiculos(tipo_vehiculo);

-- ========================================
-- PASO 4: CONFIGURAR RLS (ROW LEVEL SECURITY) PARA SUPABASE
-- ========================================

-- Habilitar RLS en todas las tablas
ALTER TABLE anuncios ENABLE ROW LEVEL SECURITY;
ALTER TABLE imagenes ENABLE ROW LEVEL SECURITY;
ALTER TABLE notificaciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE historial_busqueda ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehiculos ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias_vehiculo ENABLE ROW LEVEL SECURITY;

-- Políticas para anuncios
-- Permitir lectura pública de anuncios activos
CREATE POLICY "Anuncios públicos - lectura"
    ON anuncios FOR SELECT
    USING (activo = TRUE);

-- Permitir a usuarios autenticados crear sus propios anuncios
CREATE POLICY "Anuncios - crear"
    ON anuncios FOR INSERT
    WITH CHECK (auth.uid()::text = id_usuario);

-- Permitir a usuarios modificar solo sus propios anuncios
CREATE POLICY "Anuncios - actualizar propios"
    ON anuncios FOR UPDATE
    USING (auth.uid()::text = id_usuario)
    WITH CHECK (auth.uid()::text = id_usuario);

-- Permitir a usuarios eliminar solo sus propios anuncios
CREATE POLICY "Anuncios - eliminar propios"
    ON anuncios FOR DELETE
    USING (auth.uid()::text = id_usuario);

-- Políticas para imagenes
-- Permitir lectura pública de imágenes de anuncios activos
CREATE POLICY "Imagenes públicas - lectura"
    ON imagenes FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM anuncios 
            WHERE anuncios.id_anuncio = imagenes.id_anuncio 
            AND anuncios.activo = TRUE
        )
    );

-- Permitir a usuarios crear imágenes para sus propios anuncios
CREATE POLICY "Imagenes - crear"
    ON imagenes FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM anuncios 
            WHERE anuncios.id_anuncio = imagenes.id_anuncio 
            AND anuncios.id_usuario = auth.uid()::text
        )
    );

-- Permitir a usuarios modificar imágenes de sus propios anuncios
CREATE POLICY "Imagenes - actualizar propias"
    ON imagenes FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM anuncios 
            WHERE anuncios.id_anuncio = imagenes.id_anuncio 
            AND anuncios.id_usuario = auth.uid()::text
        )
    );

-- Permitir a usuarios eliminar imágenes de sus propios anuncios
CREATE POLICY "Imagenes - eliminar propias"
    ON imagenes FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM anuncios 
            WHERE anuncios.id_anuncio = imagenes.id_anuncio 
            AND anuncios.id_usuario = auth.uid()::text
        )
    );

-- Políticas para notificaciones
-- Permitir a usuarios ver sus propias notificaciones
CREATE POLICY "Notificaciones - lectura propias"
    ON notificaciones FOR SELECT
    USING (
        id_vendedor = auth.uid()::text 
        OR id_comprador = auth.uid()::text
    );

-- Permitir a usuarios crear notificaciones
CREATE POLICY "Notificaciones - crear"
    ON notificaciones FOR INSERT
    WITH CHECK (true);

-- Permitir a usuarios actualizar sus propias notificaciones
CREATE POLICY "Notificaciones - actualizar propias"
    ON notificaciones FOR UPDATE
    USING (
        id_vendedor = auth.uid()::text 
        OR id_comprador = auth.uid()::text
    );

-- Políticas para historial_busqueda
-- Permitir a usuarios ver su propio historial
CREATE POLICY "Historial - lectura propio"
    ON historial_busqueda FOR SELECT
    USING (id_usuario = auth.uid()::text);

-- Permitir a usuarios crear su propio historial
CREATE POLICY "Historial - crear"
    ON historial_busqueda FOR INSERT
    WITH CHECK (id_usuario = auth.uid()::text);

-- Políticas para vehiculos
-- Permitir lectura pública
CREATE POLICY "Vehiculos - lectura pública"
    ON vehiculos FOR SELECT
    USING (true);

-- Permitir a usuarios autenticados crear/actualizar vehículos
CREATE POLICY "Vehiculos - crear/actualizar"
    ON vehiculos FOR ALL
    USING (auth.role() = 'authenticated');

-- Políticas para categorias_vehiculo
-- Permitir lectura pública de categorías activas
CREATE POLICY "Categorias - lectura pública"
    ON categorias_vehiculo FOR SELECT
    USING (activo = TRUE);

-- Solo administradores pueden modificar categorías
-- (Esto se puede ajustar según necesidades)

-- ========================================
-- PASO 5: INSERTAR DATOS INICIALES
-- ========================================

-- Insertar categorías de vehículos
INSERT INTO categorias_vehiculo (nombre, codigo, descripcion, activo) VALUES
('Sedán', 'SEDAN', 'Vehículos de 4 puertas con maletero separado', TRUE),
('Hatchback', 'HATCHBACK', 'Vehículos compactos con portón trasero', TRUE),
('SUV', 'SUV', 'Vehículos utilitarios deportivos', TRUE),
('Coupe', 'COUPE', 'Vehículos de 2 puertas', TRUE),
('Station Wagon', 'WAGON', 'Vehículos familiares con gran capacidad de carga', TRUE),
('Deportivo', 'DEPORTIVO', 'Vehículos de alto rendimiento', TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- ========================================
-- PASO 6: CONFIGURAR STORAGE BUCKET PARA IMÁGENES
-- ========================================

-- Nota: Los buckets de Storage se crean desde el Dashboard de Supabase
-- Asegúrate de crear un bucket llamado "anuncios" con las siguientes políticas:

-- Política de Storage para INSERT (subir imágenes):
-- Nombre: "Permitir subir imágenes a usuarios autenticados"
-- Operación: INSERT
-- Policy definition:
--   (bucket_id = 'anuncios'::text) AND (auth.role() = 'authenticated')

-- Política de Storage para SELECT (leer imágenes):
-- Nombre: "Permitir lectura pública de imágenes"
-- Operación: SELECT
-- Policy definition:
--   bucket_id = 'anuncios'::text

-- Política de Storage para UPDATE:
-- Nombre: "Permitir actualizar imágenes propias"
-- Operación: UPDATE
-- Policy definition:
--   (bucket_id = 'anuncios'::text) AND (auth.role() = 'authenticated')

-- Política de Storage para DELETE:
-- Nombre: "Permitir eliminar imágenes propias"
-- Operación: DELETE
-- Policy definition:
--   (bucket_id = 'anuncios'::text) AND (auth.role() = 'authenticated')

COMMIT;

-- ========================================
-- VERIFICACIÓN
-- ========================================

-- Verificar que las tablas se crearon correctamente
SELECT 
    table_name,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE table_schema = 'public' 
    AND table_type = 'BASE TABLE'
    AND table_name IN (
        'anuncios', 'imagenes', 'notificaciones', 
        'historial_busqueda', 'vehiculos', 'categorias_vehiculo'
    )
ORDER BY table_name;

-- Verificar políticas RLS
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, policyname;

-- Verificar categorías insertadas
SELECT * FROM categorias_vehiculo ORDER BY id_categoria;

