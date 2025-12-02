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

// Variable para almacenar la función getIdTokenClaims
let getIdTokenClaimsFn = null;
let interceptorIdSpring = null;
let interceptorIdDjango = null;

// Interceptor para agregar el token de Supabase a las peticiones
export const setupAuthInterceptor = (getIdTokenClaims) => {
  // Guardar la función para uso futuro
  getIdTokenClaimsFn = getIdTokenClaims;
  
  // Remover interceptores anteriores si existen
  if (interceptorIdSpring !== null) {
    springApi.interceptors.request.eject(interceptorIdSpring);
  }
  if (interceptorIdDjango !== null) {
    djangoApi.interceptors.request.eject(interceptorIdDjango);
  }
  
  const interceptor = async (config) => {
    try {
      // Si es FormData, eliminar Content-Type para que Axios lo establezca automáticamente
      if (config.data instanceof FormData) {
        delete config.headers['Content-Type'];
      }
      
      if (getIdTokenClaimsFn) {
        // Obtener access token de Supabase
        const claims = await getIdTokenClaimsFn();
        if (claims && claims.__raw) {
          const token = claims.__raw;
          config.headers.Authorization = `Bearer ${token}`;
        }
      }
    } catch (error) {
      // No lanzar el error para que la petición continúe (pero fallará con 401)
      console.error('Error al agregar token:', error);
    }
    return config;
  };

  interceptorIdSpring = springApi.interceptors.request.use(interceptor);
  interceptorIdDjango = djangoApi.interceptors.request.use(interceptor);
};

// Función para obtener el ID token manualmente si es necesario
export const getToken = async () => {
  if (getIdTokenClaimsFn) {
    try {
      const claims = await getIdTokenClaimsFn();
      return claims?.__raw || null;
    } catch (error) {
      return null;
    }
  }
  return null;
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
    try {
      const response = await djangoApi.get('/auth/profile');
      
      // Asegurar que siempre devolvamos un objeto
      if (response.data && typeof response.data === 'object') {
        return response.data;
      } else {
        return response.data || {};
      }
    } catch (error) {
      console.error('Error en getUserProfile:', error);
      throw error;
    }
  },

  // Actualizar perfil del usuario en Django
  async updateProfile(firstName, lastName) {
    const response = await djangoApi.put('/auth/profile/update/', {
      first_name: firstName,
      last_name: lastName
    });
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
