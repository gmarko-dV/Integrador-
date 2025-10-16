# CHECKAUTO™ - Sistema de Consulta de Placas de Vehículos

**CHECKAUTO™** es el portal oficial de consulta de placas de vehículos en Perú. Este proyecto implementa un sistema completo de búsqueda de placas de vehículos peruanos con autenticación Auth0, integrando múltiples backends y una API SOAP externa.

## 🚀 Funcionalidades Principales

### ✅ Implementadas
- **Autenticación segura** con Auth0 (OAuth2 + JWT)
- **Búsqueda de placas** de vehículos peruanos en tiempo real
- **Integración con API SOAP** oficial de Perú (placaapi.pe)
- **Base de datos PostgreSQL** para almacenar vehículos e historial
- **Frontend React** con interfaz moderna y responsiva
- **Backend Spring Boot** con endpoints RESTful
- **Backend Django** para administración
- **Historial de búsquedas** por usuario
- **Validación de placas** peruanas (formato: 3 letras + 3-4 números)

### 🔄 En Desarrollo
- Panel de administración Django
- Estadísticas de búsquedas
- Sistema de favoritos
- Notificaciones en tiempo real

## 🏗️ Arquitectura del Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React App     │    │  Spring Boot    │    │  Django Admin   │
│   (Puerto 3000) │◄──►│  (Puerto 8080)  │    │  (Puerto 8000)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Auth0       │    │   PostgreSQL    │    │   API SOAP      │
│  (Autenticación)│    │  (Base de Datos)│    │  (placaapi.pe)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ Tecnologías Utilizadas

### Frontend
- **React 18** con Hooks
- **Auth0 React SDK** para autenticación
- **CSS3** con diseño moderno y responsivo inspirado en portales automotrices
- **JavaScript ES6+**
- **Diseño tipo "Peruautos"** con fondo de carretera y efectos visuales

### Backend Spring Boot
- **Spring Boot 3.x**
- **Spring Security** con OAuth2
- **Spring Data JPA** con Hibernate
- **Jackson** para JSON
- **RestTemplate** para llamadas SOAP
- **PostgreSQL** con soporte JSONB

### Backend Django
- **Django 5.x**
- **Django REST Framework**
- **psycopg2** para PostgreSQL
- **CORS Headers**

### Base de Datos
- **PostgreSQL 15+**
- **Tablas principales:**
  - `vehiculos` - Información de vehículos
  - `historial_busqueda` - Historial de búsquedas por usuario

### Servicios Externos
- **Auth0** para autenticación
- **API SOAP placaapi.pe** para datos de vehículos

## 📋 Configuración de Auth0

### Credenciales actuales:
- **Domain**: `dev-gmarko.us.auth0.com`
- **Client ID**: `q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b`
- **Client Secret**: `TgPxpZmbLG-odXNg9ZxgZr8ie6nzCilfhyE1dUPtmEs3mycACIPAKY4kuwetW1DA`

### URLs de Callback configuradas:
- Spring Boot: `http://localhost:8080/login/oauth2/code/auth0`
- React: `http://localhost:3000/callback`

## 🚀 Instrucciones de Instalación y Ejecución

### 1. Prerrequisitos
- **Node.js 16+** y npm
- **Java 17+** y Maven
- **Python 3.8+** y pip
- **PostgreSQL 15+**

### 2. Base de Datos PostgreSQL
```bash
# Crear base de datos
createdb car_sales_pe

# Configurar usuario postgres con contraseña: 123456
# La aplicación creará automáticamente las tablas necesarias
```

### 3. Spring Boot Backend
```bash
cd spring-user

# Compilar y ejecutar
mvn clean compile
mvn spring-boot:run

# O en Windows:
mvnw.cmd spring-boot:run
```

**El backend estará disponible en:** `http://localhost:8080`

### 4. Django Backend
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

**El backend estará disponible en:** `http://localhost:8000`

### 5. React Frontend
```bash
cd react-front
npm install
npm start
```

**El frontend estará disponible en:** `http://localhost:3000`

## 🔗 Endpoints Disponibles

### Spring Boot (Puerto 8080)

#### Endpoints Públicos
- `GET /api/public/health` - Health check
- `GET /api/public/info` - Información de la API

#### Endpoints de Autenticación
- `GET /api/auth/user` - Información del usuario autenticado
- `GET /api/auth/check` - Verificar estado de autenticación
- `GET /oauth2/authorization/auth0` - Iniciar login con Auth0

#### Endpoints de Búsqueda de Placas
- `POST /api/plate-search` - Buscar información de placa
- `GET /api/plate-search/history` - Obtener historial de búsquedas
- `GET /api/plate-search/recent` - Obtener vehículos recientes
- `GET /api/plate-search/validate/{plate}` - Validar formato de placa
- `GET /api/plate-search/test` - Test de conectividad
- `GET /api/plate-search/raw/{placa}` - Ver JSON crudo de la API

### Django (Puerto 8000)
- `GET /api/public/health` - Health check
- `GET /api/public/info` - Información de la API
- `GET /api/auth/config` - Configuración de Auth0
- `GET /api/auth/profile` - Perfil del usuario autenticado
- `POST /api/auth/login` - Login con código de autorización

## 🔍 Flujo de Búsqueda de Placas

1. **Usuario inicia sesión** con Auth0
2. **Usuario ingresa placa** en el formulario (formato: ABC123)
3. **Frontend valida formato** de placa peruana
4. **Spring Boot consulta API SOAP** de placaapi.pe
5. **API externa devuelve datos** del vehículo en JSON
6. **Spring Boot procesa y guarda** datos en PostgreSQL
7. **Frontend muestra información** completa del vehículo
8. **Se registra búsqueda** en historial del usuario

## 📊 Datos Obtenidos de la API

Para cada placa consultada, el sistema obtiene:

### Información Básica
- **Marca y Modelo** del vehículo
- **Año de registro**
- **Descripción completa**
- **Número VIN**

### Información Específica de Perú
- **Propietario** del vehículo
- **Uso** (Particular, Comercial, etc.)
- **Punto de entrega**
- **Fecha de registro**
- **URL de imagen** del vehículo

### Datos Técnicos
- **Tamaño del motor**
- **Tipo de combustible**
- **Número de asientos**

## 🧪 Pruebas del Sistema

### 1. Verificar Servicios
```bash
# Spring Boot
curl http://localhost:8080/api/public/health

# Django
curl http://localhost:8000/api/public/health
```

### 2. Probar Búsqueda de Placas
1. Ir a `http://localhost:3000`
2. Iniciar sesión con Auth0
3. Ingresar placa válida (ej: ABC123, B6U175)
4. Verificar que se muestran todos los datos del vehículo
5. Verificar que aparece la imagen del vehículo
6. Comprobar historial de búsquedas

### 3. Probar Validación
- Intentar placas inválidas (ej: 123ABC, ABC12)
- Verificar mensajes de error apropiados

## 📁 Estructura del Proyecto

```
Integrador/
├── spring-user/                    # Backend Spring Boot
│   ├── src/main/java/com/integrador/
│   │   ├── config/                # Configuraciones
│   │   ├── controller/            # Controladores REST
│   │   ├── dto/                   # Data Transfer Objects
│   │   ├── entity/                # Entidades JPA
│   │   ├── repository/            # Repositorios JPA
│   │   └── service/               # Lógica de negocio
│   ├── src/main/resources/
│   │   └── application.properties # Configuración de BD
│   └── pom.xml                    # Dependencias Maven
├── django-admin/                   # Backend Django
│   ├── admin_backend/             # Configuración Django
│   ├── auth_app/                  # App de autenticación
│   └── requirements.txt           # Dependencias Python
├── react-front/                    # Frontend React
│   ├── src/
│   │   ├── components/            # Componentes React
│   │   ├── config/                # Configuración API
│   │   └── services/              # Servicios de API
│   └── package.json               # Dependencias Node.js
└── README.md                      # Este archivo
```

## 🔧 Configuración de Base de Datos

### Tabla `vehiculos`
```sql
CREATE TABLE vehiculos (
    id_vehiculo BIGSERIAL PRIMARY KEY,
    placa VARCHAR(20) UNIQUE NOT NULL,
    descripcion_api TEXT,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    anio_registro_api VARCHAR(10),
    vin VARCHAR(100),
    uso VARCHAR(200),
    propietario TEXT,
    delivery_point TEXT,
    fecha_registro_api TIMESTAMP,
    image_url_api TEXT,
    datos_api JSONB,
    fecha_actualizacion_api TIMESTAMP
);
```

### Tabla `historial_busqueda`
```sql
CREATE TABLE historial_busqueda (
    id_historial BIGSERIAL PRIMARY KEY,
    id_usuario VARCHAR(255) NOT NULL,
    placa_consultada VARCHAR(20) NOT NULL,
    fecha_consulta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resultado_api JSONB
);
```

## 🚨 Notas Importantes

- **PostgreSQL debe estar ejecutándose** antes de iniciar las aplicaciones
- **Los puertos 3000, 8000 y 8080** deben estar disponibles
- **Las URLs de callback en Auth0** deben coincidir con las configuradas
- **La API SOAP de placaapi.pe** requiere conexión a internet
- **El token JWT se comparte** entre ambos backends para mantener la sesión
- **Las tablas se crean automáticamente** al iniciar Spring Boot

## 🐛 Solución de Problemas

### Error: "No plugin found for prefix 'spring-boot'"
```bash
cd spring-user
mvn clean
mvn compile
mvn spring-boot:run
```

### Error: "FATAL: no existe la base de datos"
Verificar que la base de datos `car_sales_pe` existe en PostgreSQL.

### Error: "Failed to fetch" en frontend
Verificar que Spring Boot esté ejecutándose en puerto 8080.

### Imagen no se muestra
Verificar que el campo `image_url_api` esté siendo enviado correctamente desde el backend.

## 📈 Próximos Pasos

1. **Implementar panel de administración** en Django
2. **Agregar estadísticas** de búsquedas
3. **Sistema de favoritos** para usuarios
4. **Notificaciones en tiempo real** con WebSockets
5. **API de reportes** y analytics
6. **Sistema de cache** para mejorar rendimiento
7. **Tests automatizados** con Jest y JUnit

## 👥 Contribuidores

- **Desarrollo Backend Spring Boot**: Integración API SOAP, autenticación Auth0
- **Desarrollo Frontend React**: Interfaz de usuario, integración con Auth0
- **Desarrollo Backend Django**: Panel de administración (en desarrollo)
- **Base de Datos**: Diseño y optimización PostgreSQL

---

**Estado del Proyecto**: ✅ **FUNCIONAL** - Búsqueda de placas operativa con autenticación completa