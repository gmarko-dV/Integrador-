# CHECKAUTO - Sistema de Consulta de Placas de Vehículos

**CHECKAUTO** es el portal oficial de consulta de placas de vehículos en Perú. Sistema completo de búsqueda de placas con autenticación, integrando múltiples backends y una API SOAP externa.

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React App     │    │  Spring Boot    │    │  Django Admin   │
│   (Puerto 3000) │◄──►│  (Puerto 8080)  │    │  (Puerto 8000)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│    Supabase    │    │   PostgreSQL    │    │   API SOAP      │
│  (Auth + DB)   │    │  (Base de Datos)│    │  (placaapi.pe)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ Tecnologías Principales

### Frontend
- **React 19.2** con Hooks
- **Supabase Auth** para autenticación
- **React Router DOM 7.9** para navegación
- **Axios 1.12** para peticiones HTTP
- **CSS3** con diseño responsivo

### Backend Spring Boot
- **Spring Boot 3.5.6**
- **Spring Security** con OAuth2
- **Spring Data JPA** con Hibernate
- **PostgreSQL** (Supabase)
- **Java 17**

### Backend Django
- **Django 5.2.7**
- **Django REST Framework 3.16.1**
- **psycopg2 2.9.10** para PostgreSQL
- **django-cors-headers 4.6.0**
- **Python 3.8+**

### Base de Datos
- **PostgreSQL** (Supabase)
- **Supabase Storage** para imágenes
- **Supabase Auth** para autenticación

### Mobile (Android)
- **Kotlin**
- **Supabase Android SDK**
- **Deep Links** para integración

## 📋 Configuración de Supabase

### Credenciales
- **URL**: `https://kkjjgvqqzxothhojvzss.supabase.co`
- **Anon Key**: Configurada en cada servicio
- **Database**: PostgreSQL en Supabase
- **Storage Bucket**: `anuncios`

### Tablas Principales
- `anuncios` - Anuncios de vehículos
- `vehiculos` - Información de vehículos
- `notificaciones` - Notificaciones de usuarios
- `categoria_vehiculo` - Categorías de vehículos
- `historial_busqueda` - Historial de búsquedas

## 🚀 Instalación y Ejecución

### Prerrequisitos
- **Node.js 16+** y npm
- **Java 17+** y Maven
- **Python 3.8+** y pip
- **PostgreSQL** (Supabase)

### 1. Spring Boot Backend
```bash
cd spring-user
mvn clean install
mvn spring-boot:run
```
**Puerto:** `http://localhost:8080`

### 2. Django Backend
```bash
cd django-admin
python -m venv venv
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

pip install -r requirements.txt
python manage.py migrate
python manage.py runserver
```
**Puerto:** `http://localhost:8000`

### 3. React Frontend
```bash
cd react-front
npm install
npm start
```
**Puerto:** `http://localhost:3000`

### 4. Android App
```bash
cd CheckAuto2
./gradlew build
./gradlew installDebug
```

## 📁 Estructura del Proyecto

```
Integrador/
├── react-front/          # Frontend React
│   ├── src/
│   │   ├── components/  # Componentes React
│   │   ├── services/    # Servicios de API
│   │   └── config/      # Configuración
│   └── package.json
├── spring-user/         # Backend Spring Boot
│   ├── src/main/java/
│   │   └── com/integrador/
│   │       ├── config/  # Configuraciones
│   │       ├── controller/ # Controladores REST
│   │       ├── entity/  # Entidades JPA
│   │       └── service/ # Lógica de negocio
│   └── pom.xml
├── django-admin/        # Backend Django
│   ├── admin_backend/   # Configuración Django
│   ├── auth_app/        # App de autenticación
│   └── requirements.txt
└── CheckAuto2/          # App Android
    └── app/src/main/
        └── java/com/tecsup/checkauto/
```

## 🔗 Endpoints Principales

### Spring Boot (Puerto 8080)
- `GET /api/public/health` - Health check
- `POST /api/plate-search` - Buscar placa
- `GET /api/plate-search/history` - Historial de búsquedas
- `GET /api/auth/user` - Información del usuario

### Django (Puerto 8000)
- `GET /api/public/health` - Health check
- `GET /api/auth/profile` - Perfil del usuario
- `GET /admin/` - Panel de administración

## 🔍 Funcionalidades

- ✅ **Autenticación** con Supabase
- ✅ **Búsqueda de placas** de vehículos peruanos
- ✅ **Integración con API SOAP** (placaapi.pe)
- ✅ **Gestión de anuncios** de vehículos
- ✅ **Chat IA** flotante integrado
- ✅ **Notificaciones** en tiempo real
- ✅ **Panel de administración** Django
- ✅ **App Android** con deep links

## 📝 Notas Importantes

- **Supabase** se usa para autenticación, base de datos y storage
- **Los puertos 3000, 8000 y 8080** deben estar disponibles
- **Las tablas se crean automáticamente** mediante migraciones
- **La API SOAP** requiere conexión a internet
- **El token JWT** se comparte entre backends para mantener sesión

## 🐛 Solución de Problemas

### Error de conexión a base de datos
Verificar que las credenciales de Supabase estén correctas en:
- `spring-user/src/main/resources/application.properties`
- `django-admin/admin_backend/settings.py`
- `react-front/src/config/supabase.js`

### Error "Failed to fetch" en frontend
Verificar que los backends estén ejecutándose en los puertos correctos.

### Error de migraciones Django
```bash
cd django-admin
python manage.py migrate
```

---

**Estado del Proyecto**: ✅ **FUNCIONAL** - Sistema completo operativo
