# Configuración de Deep Links para Confirmación de Email

## 📱 Configuración: Misma URL para Web y Móvil

La app móvil ahora usa la **misma URL de la web** para confirmación de email. Android interceptará automáticamente el enlace cuando el usuario haga clic desde su email.

## ⚠️ IMPORTANTE: Configurar en Supabase Dashboard

Para que los enlaces de confirmación de email funcionen tanto en la **web** como en la **app móvil**, debes configurar las URLs de redirección en Supabase:

### 1. Configurar Site URL en Supabase

1. Ve al [Dashboard de Supabase](https://supabase.com/dashboard)
2. Selecciona tu proyecto **Checkauto**
3. Ve a **Authentication** → **URL Configuration**
4. En **Site URL**, puedes dejar la URL de tu web (ej: `http://localhost:3000` o tu dominio de producción)
   - Esta es la URL por defecto, pero cada plataforma puede especificar su propia URL

### 2. Configurar Redirect URLs (MÚLTIPLES URLs)

En la misma sección, en **Redirect URLs**, agrega **TODAS** las siguientes URLs (una por línea):

**Para la Web:**
```
http://localhost:3000/callback
https://tu-dominio.com/callback
```

**Ejemplo completo de Redirect URLs (solo las de la web):**
```
http://localhost:3000/callback
https://tu-dominio.com/callback
```

**✅ IMPORTANTE:** 
- La app móvil ahora usa la misma URL de la web (`http://localhost:3000/callback` o tu dominio)
- Android interceptará automáticamente el enlace cuando el usuario haga clic desde su email
- Si la app está instalada, se abrirá la app; si no, se abrirá el navegador

**✅ IMPORTANTE:** 
- Supabase permite múltiples Redirect URLs
- Cada plataforma (web/móvil) especifica su propia URL al hacer signUp
- La app móvil usa `checkauto://auth/callback`
- La web usa `window.location.origin + '/callback'`

### 3. Configurar Email Templates (Opcional)

1. Ve a **Authentication** → **Email Templates**
2. Selecciona **Confirm signup**
3. Asegúrate de que el enlace use la variable `{{ .ConfirmationURL }}`
4. Supabase automáticamente usará la URL de redirección configurada

### 4. Verificar configuración en la app

La app ya está configurada para:
- Recibir deep links con el esquema `checkauto://auth/callback`
- Manejar URLs de Supabase con el formato `https://kkjjgvqqzxothhojvzss.supabase.co/auth/v1/callback`
- Procesar tokens de confirmación automáticamente

## Flujo de confirmación

### En la App Móvil:
1. Usuario se registra en la app móvil
2. La app especifica `redirectTo: "http://localhost:3000/callback"` (misma URL de la web)
3. Supabase envía email de confirmación con la URL de la web
4. Usuario hace clic en el enlace del email desde su móvil
5. Android detecta que la app puede manejar esa URL (App Link)
6. Android pregunta al usuario: "Abrir con CheckAuto o con el navegador?"
7. Si elige la app, se abre la app y procesa los tokens
8. Si elige el navegador, se abre la web y procesa los tokens
9. En ambos casos, el usuario queda autenticado

### En la Web:
1. Usuario se registra en la web
2. La web especifica `emailRedirectTo: window.location.origin + '/callback'` al hacer signUp
3. Supabase envía email de confirmación con la URL de la web
4. Usuario hace clic en el enlace del email
5. El enlace redirige a `http://localhost:3000/callback?access_token=...&refresh_token=...` (o tu dominio)
6. El componente `Callback.js` procesa los tokens automáticamente
7. Supabase establece la sesión del usuario
8. El usuario es redirigido al dashboard (`/`)
9. Usuario queda autenticado

**Nota:** El componente `Callback` está en `react-front/src/components/Callback.js` y maneja:
- Confirmación de email (tokens en la URL)
- Callbacks de OAuth (Google, GitHub, etc.)
- Errores de autenticación
- Redirección automática después de confirmar

## Notas importantes

- El esquema `checkauto` debe coincidir con el `applicationId` de tu app (o puedes usar otro)
- Las URLs deben estar exactamente como se muestran arriba
- Después de cambiar la configuración en Supabase, puede tomar unos minutos en aplicarse

