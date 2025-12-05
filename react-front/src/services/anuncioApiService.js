import { springApi } from './apiService';

const anuncioService = {
  // Crear un nuevo anuncio
  async crearAnuncio(formData) {
    // NO establecer Content-Type manualmente para FormData
    // axios lo hace automáticamente con el boundary correcto
    const response = await springApi.post('/anuncios', formData);
    return response.data;
  },

  // Obtener mis anuncios
  async obtenerMisAnuncios() {
    const response = await springApi.get('/anuncios/mis-anuncios');
    return response.data;
  },

  // Obtener todos los anuncios
  async obtenerTodosLosAnuncios() {
    const response = await springApi.get('/anuncios');
    return response.data;
  },

  // Obtener un anuncio por ID
  async obtenerAnuncioPorId(id) {
    const response = await springApi.get(`/anuncios/${id}`);
    return response.data;
  },

  // Actualizar un anuncio
  async actualizarAnuncio(id, formData) {
    const response = await springApi.put(`/anuncios/${id}`, formData);
    return response.data;
  },

  // Eliminar un anuncio
  async eliminarAnuncio(id) {
    const response = await springApi.delete(`/anuncios/${id}`);
    return response.data;
  },
};

export default anuncioService;

