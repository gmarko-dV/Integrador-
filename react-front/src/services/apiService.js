import axios from 'axios';

// Configuración de las APIs
const SPRING_API_URL = 'http://localhost:8080/api';
const DJANGO_API_URL = 'http://localhost:8000/api';

// Cliente axios para Spring Boot
export const springApi = axios.create({
  baseURL: SPRING_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Cliente axios para Django
export const djangoApi = axios.create({
  baseURL: DJANGO_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar el token de Auth0 a las peticiones
export const setupAuthInterceptor = (getAccessTokenSilently) => {
  const interceptor = async (config) => {
    try {
      const token = await getAccessTokenSilently();
      config.headers.Authorization = `Bearer ${token}`;
    } catch (error) {
      console.error('Error obteniendo token:', error);
    }
    return config;
  };

  springApi.interceptors.request.use(interceptor);
  djangoApi.interceptors.request.use(interceptor);
};

// Servicios de autenticación
export const authService = {
  // Obtener información del usuario desde Spring Boot
  async getUserInfo() {
    const response = await springApi.get('/auth/user');
    return response.data;
  },

  // Obtener información del usuario desde Django
  async getUserProfile() {
    const response = await djangoApi.get('/auth/profile');
    return response.data;
  },

  // Verificar estado de autenticación
  async checkAuth() {
    const response = await springApi.get('/auth/check');
    return response.data;
  },

  // Obtener configuración de Auth0 desde Django
  async getAuthConfig() {
    const response = await djangoApi.get('/auth/config');
    return response.data;
  },

  // Health check de los servicios
  async healthCheck() {
    const springHealth = await springApi.get('/public/health');
    const djangoHealth = await djangoApi.get('/public/health');
    
    return {
      spring: springHealth.data,
      django: djangoHealth.data,
    };
  },
};

export default authService;
