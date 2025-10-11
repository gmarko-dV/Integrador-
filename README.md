# Proyecto Integrador - Autenticación con Auth0

Este proyecto implementa autenticación con Auth0 en una aplicación web con múltiples backends.

## Arquitectura

- **Frontend**: React con Auth0 React SDK
- **Backend 1**: Spring Boot (Puerto 8080)
- **Backend 2**: Django (Puerto 8000)
- **Base de Datos**: PostgreSQL
- **Autenticación**: Auth0

## Configuración de Auth0

### Credenciales actuales:
- **Domain**: `dev-gmarko.us.auth0.com`
- **Client ID**: `q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b`
- **Client Secret**: `TgPxpZmbLG-odXNg9ZxgZr8ie6nzCilfhyE1dUPtmEs3mycACIPAKY4kuwetW1DA`

### URLs de Callback configuradas:
- Spring Boot: `http://localhost:8080/login/oauth2/code/auth0`
- React: `http://localhost:3000/callback`

## Instrucciones de Instalación y Ejecución

### 1. Base de Datos PostgreSQL
```bash
# Crear base de datos
createdb car_sales_pe

# Configurar usuario postgres con contraseña: 123456
```

### 2. Spring Boot Backend
```bash
cd spring-user
./mvnw spring-boot:run
# O en Windows:
mvnw.cmd spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

### 3. Django Backend
```bash
cd django-admin
# Activar entorno virtual
# En Windows:
..\venv\Scripts\activate

# Instalar dependencias
pip install -r requirements.txt

# Ejecutar migraciones
python manage.py migrate

# Ejecutar servidor
python manage.py runserver
```

El backend estará disponible en: `http://localhost:8000`

### 4. React Frontend
```bash
cd react-front
npm install
npm start
```

El frontend estará disponible en: `http://localhost:3000`

## Endpoints Disponibles

### Spring Boot (Puerto 8080)
- `GET /api/public/health` - Health check
- `GET /api/public/info` - Información de la API
- `GET /api/auth/user` - Información del usuario autenticado
- `GET /api/auth/check` - Verificar estado de autenticación
- `GET /oauth2/authorization/auth0` - Iniciar login con Auth0

### Django (Puerto 8000)
- `GET /api/public/health` - Health check
- `GET /api/public/info` - Información de la API
- `GET /api/auth/config` - Configuración de Auth0
- `GET /api/auth/profile` - Perfil del usuario autenticado
- `POST /api/auth/login` - Login con código de autorización

## Flujo de Autenticación

1. **Usuario hace clic en "Iniciar Sesión"** en React
2. **React redirige a Auth0** para autenticación
3. **Auth0 autentica al usuario** y redirige de vuelta a React (`/callback`)
4. **React obtiene el token** de Auth0
5. **React puede hacer peticiones autenticadas** a ambos backends usando el token
6. **Los backends validan el token** con Auth0 y devuelven datos del usuario

## Pruebas

### 1. Verificar que los servicios estén funcionando:
- Spring Boot: `http://localhost:8080/api/public/health`
- Django: `http://localhost:8000/api/public/health`

### 2. Probar autenticación:
1. Ir a `http://localhost:3000`
2. Hacer clic en "Iniciar Sesión"
3. Completar el login en Auth0
4. Verificar que se muestra la información del usuario
5. Hacer clic en "Actualizar" en la sección "Información de Backends"
6. Verificar que se muestran datos de ambos backends

## Estructura del Proyecto

```
Integrador/
├── spring-user/           # Backend Spring Boot
├── django-admin/          # Backend Django
├── react-front/          # Frontend React
└── README.md
```

## Notas Importantes

- Asegúrate de que PostgreSQL esté ejecutándose
- Los puertos 3000, 8000 y 8080 deben estar disponibles
- Las URLs de callback en Auth0 deben coincidir con las configuradas
- El token JWT se comparte entre ambos backends para mantener la sesión
