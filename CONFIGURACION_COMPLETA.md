# ✅ Configuración Completa de Proyectos con Supabase

Este documento resume todas las configuraciones realizadas para que los 3 proyectos funcionen correctamente con Supabase.

## 📋 Resumen de Cambios Realizados

### ✅ 1. Base de Datos (Supabase)
- ✅ Tablas creadas: `anuncios`, `imagenes`, `notificaciones`, `historial_busqueda`, `vehiculos`, `categorias_vehiculo`, `profiles`
- ✅ RLS (Row Level Security) configurado en todas las tablas
- ✅ Índices creados para optimizar consultas
- ✅ 6 categorías iniciales insertadas
- ✅ Storage bucket `anuncios` configurado con políticas RLS

### ✅ 2. App Móvil (Android/Kotlin)
**Archivos actualizados:**
- ✅ `SupabaseService.kt`: Agregado campo `id_categoria` a `AnuncioSupabase`
- ✅ `ModelConverter.kt`: Actualizado para manejar `id_categoria`
- ✅ `Anuncio.kt`: Agregado campo `idCategoria` al modelo
- ✅ `SupabaseConfig.kt`: Configuración correcta de Supabase
- ✅ `ModelConverter.kt`: Mejorada normalización de URLs de imágenes

**Configuración:**
- URL: `https://kkjjgvqqzxothhojvzss.supabase.co`
- Bucket: `anuncios`
- Autenticación: Configurada con persistencia de sesión

### ✅ 3. Spring Boot (Java)
**Archivos actualizados:**
- ✅ `Anuncio.java`: Agregado campo `idCategoria` con getter/setter
- ✅ `application.properties`: Configuración de conexión a Supabase

**Configuración:**
- Host: `db.kkjjgvqqzxothhojvzss.supabase.co:5432`
- Database: `postgres`
- JWT: Configurado para validar tokens de Supabase
- Hibernate: `ddl-auto=none` (no modifica el esquema)

### ✅ 4. Django (Python)
**Archivos actualizados:**
- ✅ `models.py`: Modelo `Anuncio` ya tiene campo `id_categoria` (ForeignKey)
- ✅ `settings.py`: Configuración de conexión a Supabase

**Configuración:**
- Host: `db.kkjjgvqqzxothhojvzss.supabase.co`
- Database: `postgres`
- Port: `5432`
- SSL: Requerido

## 🔧 Verificación de Configuración

### App Móvil
```kotlin
// Verificar en SupabaseConfig.kt
const val SUPABASE_URL = "https://kkjjgvqqzxothhojvzss.supabase.co"
const val STORAGE_BUCKET_ANUNCIOS = "anuncios"
```

### Spring Boot
```properties
# Verificar en application.properties
spring.datasource.url=jdbc:postgresql://db.kkjjgvqqzxothhojvzss.supabase.co:5432/postgres
spring.jpa.hibernate.ddl-auto=none
```

### Django
```python
# Verificar en settings.py
DATABASES = {
    'default': {
        'HOST': 'db.kkjjgvqqzxothhojvzss.supabase.co',
        'PORT': '5432',
        'NAME': 'postgres',
    }
}
```

## 📊 Estructura de Tablas

### Tablas Principales
1. **anuncios** - Anuncios de vehículos
   - Campos: id_anuncio, id_usuario, modelo, precio, id_categoria, etc.
   - RLS: Lectura pública, escritura solo para el dueño

2. **imagenes** - Imágenes de anuncios
   - Campos: id_imagen, id_anuncio, url_imagen, orden, etc.
   - RLS: Lectura pública, escritura solo para el dueño del anuncio

3. **categorias_vehiculo** - Categorías (Sedán, Hatchback, SUV, etc.)
   - Campos: id_categoria, nombre, codigo, activo
   - RLS: Lectura pública de categorías activas

4. **profiles** - Perfiles de usuario
   - Campos: id (UUID), nombre_completo, telefono, foto_url, etc.
   - RLS: Solo el usuario puede ver/editar su propio perfil

5. **notificaciones** - Notificaciones de interés
6. **historial_busqueda** - Historial de búsquedas
7. **vehiculos** - Información de vehículos consultados

## 🔐 Políticas RLS Configuradas

### Anuncios
- ✅ Lectura pública de anuncios activos
- ✅ Usuarios autenticados pueden crear sus propios anuncios
- ✅ Solo el dueño puede modificar/eliminar

### Imágenes
- ✅ Lectura pública de imágenes de anuncios activos
- ✅ Solo el dueño del anuncio puede crear/modificar/eliminar imágenes

### Storage Bucket `anuncios`
- ✅ INSERT: Usuarios autenticados pueden subir
- ✅ SELECT: Lectura pública
- ✅ UPDATE: Usuarios autenticados pueden actualizar
- ✅ DELETE: Usuarios autenticados pueden eliminar

## 🚀 Próximos Pasos

### 1. Sincronizar Django Migrations
```bash
cd django-admin
python manage.py migrate --fake-initial
```

### 2. Probar Conexiones
- **App Móvil**: Intentar iniciar sesión y crear un anuncio
- **Spring Boot**: Verificar que la aplicación se conecta correctamente
- **Django**: Verificar que el admin puede acceder a las tablas

### 3. Verificar Storage
- Subir una imagen desde la app móvil
- Verificar que se guarda en el bucket `anuncios`
- Verificar que la URL se guarda correctamente en la tabla `imagenes`

## 🐛 Solución de Problemas

### Error: "relation does not exist"
- Verificar que todas las tablas se crearon correctamente
- Ejecutar el script `database_reset.sql` nuevamente

### Error: "new row violates row-level security policy"
- Verificar que el usuario está autenticado
- Verificar que las políticas RLS están configuradas correctamente
- Verificar las políticas del Storage bucket

### Error de conexión en Spring Boot
- Verificar que `spring.jpa.hibernate.ddl-auto=none`
- Verificar credenciales en `application.properties`

### Error de conexión en Django
- Verificar credenciales en `settings.py`
- Ejecutar `python manage.py migrate --fake-initial`

## ✅ Checklist Final

- [x] Base de datos creada con todas las tablas
- [x] RLS configurado en todas las tablas
- [x] Storage bucket `anuncios` creado y configurado
- [x] App Móvil actualizada con `id_categoria`
- [x] Spring Boot actualizado con `idCategoria`
- [x] Django ya tenía `id_categoria` configurado
- [x] ModelConverter actualizado en App Móvil
- [x] Configuraciones de conexión verificadas

## 📝 Notas Importantes

1. **Autenticación**: Todos los proyectos usan Supabase Auth
   - App Móvil: SDK de Supabase
   - Spring Boot: JWT validation con Supabase
   - Django: JWT validation personalizada

2. **Storage**: Las imágenes se almacenan en Supabase Storage
   - Bucket: `anuncios`
   - URLs se guardan en la tabla `imagenes`

3. **RLS**: Todas las tablas tienen Row Level Security habilitado
   - Protege los datos a nivel de base de datos
   - Cada usuario solo puede acceder a sus propios datos (excepto lectura pública de anuncios)

4. **Compatibilidad**: Los 3 proyectos pueden trabajar con la misma base de datos sin conflictos

