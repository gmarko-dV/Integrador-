// Configuración de la API
export const API_CONFIG = {
  BACKEND_URL: process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080',
  PLATE_API_USERNAME: process.env.REACT_APP_PLATE_API_USERNAME || 'jhom12'
};

export const API_ENDPOINTS = {
  PLATE_SEARCH: `${API_CONFIG.BACKEND_URL}/api/plate-search`,
  PLATE_HISTORY: `${API_CONFIG.BACKEND_URL}/api/plate-search/history`,
  PLATE_RECENT: `${API_CONFIG.BACKEND_URL}/api/plate-search/recent`,
  PLATE_VALIDATE: `${API_CONFIG.BACKEND_URL}/api/plate-search/validate`
};
