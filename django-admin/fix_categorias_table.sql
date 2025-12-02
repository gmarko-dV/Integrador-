-- Script para agregar las columnas faltantes a la tabla categorias_vehiculo existente

-- Agregar la columna codigo si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'categorias_vehiculo' AND column_name = 'codigo'
    ) THEN
        ALTER TABLE categorias_vehiculo ADD COLUMN codigo VARCHAR(50);
        
        -- Crear valores únicos para codigo basados en nombre (si hay datos)
        UPDATE categorias_vehiculo 
        SET codigo = LOWER(REPLACE(REPLACE(nombre, ' ', '-'), 'á', 'a'))
        WHERE codigo IS NULL;
        
        -- Hacer la columna NOT NULL y UNIQUE
        ALTER TABLE categorias_vehiculo 
        ALTER COLUMN codigo SET NOT NULL,
        ADD CONSTRAINT categorias_vehiculo_codigo_unique UNIQUE (codigo);
        
        -- Crear índice para codigo
        CREATE INDEX IF NOT EXISTS categorias_vehiculo_codigo_5c39b7b4_like 
        ON categorias_vehiculo (codigo varchar_pattern_ops);
    END IF;
END $$;

-- Agregar la columna activo si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'categorias_vehiculo' AND column_name = 'activo'
    ) THEN
        ALTER TABLE categorias_vehiculo ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
END $$;

-- Agregar la columna fecha_creacion si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'categorias_vehiculo' AND column_name = 'fecha_creacion'
    ) THEN
        ALTER TABLE categorias_vehiculo 
        ADD COLUMN fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Agregar la columna id_categoria a anuncios si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'anuncios' AND column_name = 'id_categoria'
    ) THEN
        ALTER TABLE anuncios ADD COLUMN id_categoria INTEGER NULL;
        ALTER TABLE anuncios ADD CONSTRAINT anuncios_id_categoria_249ddfcd_fk_categoria 
            FOREIGN KEY (id_categoria) REFERENCES categorias_vehiculo(id_categoria) 
            DEFERRABLE INITIALLY DEFERRED;
        CREATE INDEX IF NOT EXISTS anuncios_id_categoria_249ddfcd ON anuncios (id_categoria);
    END IF;
END $$;

