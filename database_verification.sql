-- ========================================
-- SCRIPT DE VERIFICACIÓN POST-RESET
-- Ejecutar después de database_reset.sql
-- ========================================

-- Verificar que todas las tablas existen
SELECT 
    'Tablas creadas' as verificacion,
    COUNT(*) as total,
    string_agg(table_name, ', ' ORDER BY table_name) as tablas
FROM information_schema.tables 
WHERE table_schema = 'public' 
    AND table_type = 'BASE TABLE'
    AND table_name IN (
        'anuncios', 'imagenes', 'notificaciones', 
        'historial_busqueda', 'vehiculos', 'categorias_vehiculo'
    );

-- Verificar columnas de cada tabla
SELECT 
    'Columnas de anuncios' as verificacion,
    COUNT(*) as total_columnas
FROM information_schema.columns 
WHERE table_name = 'anuncios' AND table_schema = 'public';

SELECT 
    'Columnas de imagenes' as verificacion,
    COUNT(*) as total_columnas
FROM information_schema.columns 
WHERE table_name = 'imagenes' AND table_schema = 'public';

SELECT 
    'Columnas de notificaciones' as verificacion,
    COUNT(*) as total_columnas
FROM information_schema.columns 
WHERE table_name = 'notificaciones' AND table_schema = 'public';

-- Verificar que RLS está habilitado
SELECT 
    'RLS habilitado' as verificacion,
    tablename,
    rowsecurity as rls_activo
FROM pg_tables 
WHERE schemaname = 'public' 
    AND tablename IN (
        'anuncios', 'imagenes', 'notificaciones', 
        'historial_busqueda', 'vehiculos', 'categorias_vehiculo'
    )
ORDER BY tablename;

-- Verificar políticas RLS
SELECT 
    'Políticas RLS' as verificacion,
    tablename,
    COUNT(*) as total_politicas
FROM pg_policies 
WHERE schemaname = 'public'
GROUP BY tablename
ORDER BY tablename;

-- Verificar índices
SELECT 
    'Índices' as verificacion,
    tablename,
    indexname
FROM pg_indexes 
WHERE schemaname = 'public' 
    AND tablename IN (
        'anuncios', 'imagenes', 'notificaciones', 
        'historial_busqueda', 'vehiculos', 'categorias_vehiculo'
    )
ORDER BY tablename, indexname;

-- Verificar categorías insertadas
SELECT 
    'Categorías iniciales' as verificacion,
    COUNT(*) as total,
    string_agg(nombre, ', ' ORDER BY id_categoria) as categorias
FROM categorias_vehiculo;

-- Verificar foreign keys
SELECT 
    'Foreign Keys' as verificacion,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
    AND tc.table_schema = 'public'
    AND tc.table_name IN (
        'anuncios', 'imagenes', 'notificaciones', 
        'historial_busqueda', 'vehiculos', 'categorias_vehiculo'
    )
ORDER BY tc.table_name, kcu.column_name;

-- Verificar constraints CHECK
SELECT 
    'Constraints CHECK' as verificacion,
    table_name,
    constraint_name,
    check_clause
FROM information_schema.check_constraints
WHERE constraint_schema = 'public'
    AND constraint_name IN (
        SELECT constraint_name 
        FROM information_schema.table_constraints 
        WHERE table_schema = 'public' 
            AND table_name IN ('anuncios', 'imagenes')
    )
ORDER BY table_name;

-- Resumen final
SELECT 
    '✅ VERIFICACIÓN COMPLETA' as estado,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('anuncios', 'imagenes', 'notificaciones', 'historial_busqueda', 'vehiculos', 'categorias_vehiculo')) as tablas_creadas,
    (SELECT COUNT(*) FROM pg_policies WHERE schemaname = 'public') as politicas_rls,
    (SELECT COUNT(*) FROM categorias_vehiculo) as categorias_insertadas,
    (SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public' AND tablename IN ('anuncios', 'imagenes', 'notificaciones', 'historial_busqueda', 'vehiculos', 'categorias_vehiculo')) as indices_creados;

