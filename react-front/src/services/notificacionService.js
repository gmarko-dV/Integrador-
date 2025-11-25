import { springApi } from './apiService';

const notificacionService = {
  // Contactar a un vendedor (crear notificación)
  async contactarVendedor(idVendedor, idAnuncio, mensaje) {
    const response = await springApi.post('/notificaciones/contactar', {
      idVendedor,
      idAnuncio,
      mensaje: mensaje || 'Un comprador está interesado en tu anuncio'
    });
    return response.data;
  },

  // Obtener mis notificaciones
  async obtenerMisNotificaciones() {
    const response = await springApi.get('/notificaciones');
    return response.data;
  },

  // Obtener notificaciones no leídas
  async obtenerNotificacionesNoLeidas() {
    const response = await springApi.get('/notificaciones/no-leidas');
    return response.data;
  },

  // Marcar una notificación como leída
  async marcarComoLeida(idNotificacion) {
    const response = await springApi.put(`/notificaciones/${idNotificacion}/marcar-leida`);
    return response.data;
  },

  // Marcar todas las notificaciones como leídas
  async marcarTodasComoLeidas() {
    const response = await springApi.put('/notificaciones/marcar-todas-leidas');
    return response.data;
  }
};

export default notificacionService;

