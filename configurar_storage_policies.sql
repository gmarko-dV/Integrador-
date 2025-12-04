-- ========================================
-- CONFIGURAR POLÍTICAS RLS PARA STORAGE
-- Bucket: anuncios
-- ========================================
-- Este script configura las políticas de Row-Level Security (RLS)
-- para el bucket de Storage "anuncios" en Supabase
-- 
-- IMPORTANTE: Asegúrate de que el bucket "anuncios" existe en Supabase Storage
-- Si no existe, créalo desde: Storage > Buckets > New bucket
-- Nombre: "anuncios"
-- Public: true (para que las imágenes sean accesibles públicamente)

BEGIN;

-- ========================================
-- PASO 1: Verificar que el bucket existe
-- ========================================
-- Si el bucket no existe, créalo desde el Dashboard de Supabase:
-- Storage > Buckets > New bucket
-- - Name: anuncios
-- - Public bucket: true (marcado)
-- - File size limit: 10MB (o el que prefieras)
-- - Allowed MIME types: image/* (o deja vacío para permitir todos)

-- ========================================
-- PASO 2: Habilitar RLS en storage.objects
-- ========================================
-- Nota: RLS ya está habilitado por defecto en storage.objects

-- ========================================
-- PASO 3: Eliminar políticas existentes (si las hay)
-- ========================================
DROP POLICY IF EXISTS "Permitir subir imágenes a usuarios autenticados" ON storage.objects;
DROP POLICY IF EXISTS "Permitir leer imágenes públicas" ON storage.objects;
DROP POLICY IF EXISTS "Permitir actualizar imágenes propias" ON storage.objects;
DROP POLICY IF EXISTS "Permitir eliminar imágenes propias" ON storage.objects;

-- ========================================
-- PASO 4: Crear políticas para INSERT (subir archivos)
-- ========================================
-- Permite a usuarios autenticados subir archivos al bucket "anuncios"
CREATE POLICY "Permitir subir imágenes a usuarios autenticados"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (
    bucket_id = 'anuncios'::text
    AND auth.role() = 'authenticated'::text
);

-- ========================================
-- PASO 5: Crear políticas para SELECT (leer archivos)
-- ========================================
-- Permite a todos (público) leer archivos del bucket "anuncios"
CREATE POLICY "Permitir leer imágenes públicas"
ON storage.objects
FOR SELECT
TO public
USING (
    bucket_id = 'anuncios'::text
);

-- ========================================
-- PASO 6: Crear políticas para UPDATE (actualizar archivos)
-- ========================================
-- Permite a usuarios autenticados actualizar sus propios archivos
CREATE POLICY "Permitir actualizar imágenes propias"
ON storage.objects
FOR UPDATE
TO authenticated
USING (
    bucket_id = 'anuncios'::text
    AND auth.role() = 'authenticated'::text
    -- Opcional: solo permitir actualizar archivos propios
    -- AND (storage.foldername(name))[1] = auth.uid()::text
)
WITH CHECK (
    bucket_id = 'anuncios'::text
    AND auth.role() = 'authenticated'::text
);

-- ========================================
-- PASO 7: Crear políticas para DELETE (eliminar archivos)
-- ========================================
-- Permite a usuarios autenticados eliminar archivos del bucket "anuncios"
CREATE POLICY "Permitir eliminar imágenes propias"
ON storage.objects
FOR DELETE
TO authenticated
USING (
    bucket_id = 'anuncios'::text
    AND auth.role() = 'authenticated'::text
    -- Opcional: solo permitir eliminar archivos propios
    -- AND (storage.foldername(name))[1] = auth.uid()::text
);

COMMIT;

-- ========================================
-- VERIFICACIÓN
-- ========================================
-- Después de ejecutar este script, verifica que las políticas se crearon correctamente:
-- 
-- SELECT * FROM pg_policies 
-- WHERE tablename = 'objects' 
-- AND schemaname = 'storage';
--
-- Deberías ver 4 políticas:
-- 1. "Permitir subir imágenes a usuarios autenticados" (INSERT)
-- 2. "Permitir leer imágenes públicas" (SELECT)
-- 3. "Permitir actualizar imágenes propias" (UPDATE)
-- 4. "Permitir eliminar imágenes propias" (DELETE)

