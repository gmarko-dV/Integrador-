import { springApi } from './apiService';

const chatService = {
  /**
   * Obtener todas las conversaciones del usuario autenticado
   */
  async obtenerConversaciones() {
    const response = await springApi.get('/conversaciones');
    return response.data;
  },

  /**
   * Crear o obtener una conversación existente
   */
  async crearObtenerConversacion(idAnuncio, idVendedor, idComprador, mensaje = null) {
    const response = await springApi.post('/conversaciones', {
      idAnuncio,
      idVendedor,
      idComprador,
      mensaje: mensaje || null
    });
    return response.data;
  },

  /**
   * Obtener una conversación por ID
   */
  async obtenerConversacion(idConversacion) {
    const response = await springApi.get(`/conversaciones/${idConversacion}`);
    return response.data;
  },

  /**
   * Obtener todos los mensajes de una conversación
   */
  async obtenerMensajes(idConversacion) {
    const response = await springApi.get(`/conversaciones/${idConversacion}/mensajes`);
    return response.data;
  },

  /**
   * Enviar un mensaje en una conversación
   */
  async enviarMensaje(idConversacion, mensaje) {
    const response = await springApi.post(`/conversaciones/${idConversacion}/mensajes`, {
      mensaje
    });
    return response.data;
  },

  /**
   * Marcar mensajes como leídos
   */
  async marcarComoLeido(idConversacion) {
    const response = await springApi.put(`/conversaciones/${idConversacion}/mensajes/leer`);
    return response.data;
  },

  /**
   * Contar mensajes no leídos del usuario
   */
  async contarNoLeidos() {
    const response = await springApi.get('/conversaciones/mensajes/no-leidos');
    return response.data;
  },

  /**
   * Archivar una conversación
   */
  async archivarConversacion(idConversacion) {
    const response = await springApi.put(`/conversaciones/${idConversacion}/archivar`);
    return response.data;
  }
};

export default chatService;
