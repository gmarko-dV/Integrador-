import { springApi } from './apiService';

const chatService = {
  // Enviar un mensaje al chat de IA
  async sendMessage(message, conversationHistory = []) {
    try {
      const response = await springApi.post('/chat', {
        message: message,
        conversationHistory: conversationHistory
      });
      return response.data;
    } catch (error) {
      console.error('Error al enviar mensaje al chat:', error);
      throw error;
    }
  }
};

export default chatService;

