# Instrucciones para agregar el Logo de IA

## Para la aplicación Web (React)

1. Copia el archivo `LOGO IA.png` desde tu carpeta de Descargas
2. Renómbralo a `logo-ia.png` (todo en minúsculas, con guión)
3. Colócalo en la carpeta: `react-front/public/logo-ia.png`

El código ya está configurado para usar este archivo. Si el logo no se carga, se mostrará el emoji 🤖 como respaldo.

## Para la aplicación Móvil (Android)

1. Copia el archivo `LOGO IA.png` desde tu carpeta de Descargas
2. Renómbralo a `logo_ia.png` (todo en minúsculas, con guión bajo)
3. Colócalo en la carpeta: `CheckAuto2/app/src/main/res/drawable/logo_ia.png`

**Nota importante:** El nombre del archivo debe ser `logo_ia.png` (con guión bajo, no espacios ni guiones medios) porque Android requiere nombres de recursos sin guiones medios.

Después de copiar el archivo, necesitarás:
- Recompilar la app Android
- El logo aparecerá automáticamente en la burbuja flotante del chat IA

## Verificación

- **Web:** Abre la aplicación en el navegador y verifica que el logo aparezca en la burbuja flotante (esquina inferior derecha)
- **Móvil:** Compila y ejecuta la app, el logo debería aparecer en la burbuja flotante del chat IA

