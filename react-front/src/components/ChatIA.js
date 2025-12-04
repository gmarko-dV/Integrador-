import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import chatService from '../services/chatService';
import anuncioService from '../services/anuncioApiService';
import { normalizeImageUrl } from '../utils/imageUtils';
import './ChatIA.css';

const ChatIA = ({ isFloating = false, onClose }) => {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: '¡Hola! 👋 Soy tu asistente virtual para ayudarte a encontrar el vehículo perfecto. ¿Qué características buscas en un auto? Por ejemplo, puedes decirme el tipo de vehículo (SUV, Sedán, Hatchback, etc.), el año, el precio máximo, o el kilometraje máximo que te interesa.'
    }
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [recommendedAnuncios, setRecommendedAnuncios] = useState([]);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || loading) return;

    const userMessage = inputMessage.trim();
    setInputMessage('');
    
    // Agregar mensaje del usuario
    const newUserMessage = {
      role: 'user',
      content: userMessage
    };
    
    setMessages(prev => [...prev, newUserMessage]);
    setLoading(true);

    try {
      // Construir historial de conversación
      const conversationHistory = messages.map(msg => ({
        role: msg.role,
        content: msg.content
      }));

      // Enviar mensaje al backend
      const response = await chatService.sendMessage(userMessage, conversationHistory);

      if (response.success) {
        // Agregar respuesta de la IA
        const assistantMessage = {
          role: 'assistant',
          content: response.response
        };
        
        setMessages(prev => [...prev, assistantMessage]);

        // Si hay recomendaciones, cargar los anuncios
        if (response.hasRecommendations && response.recommendedAnuncioIds?.length > 0) {
          await loadRecommendedAnuncios(response.recommendedAnuncioIds);
        } else {
          setRecommendedAnuncios([]);
        }
      } else {
        throw new Error(response.error || 'Error al procesar el mensaje');
      }
    } catch (error) {
      console.error('Error al enviar mensaje:', error);
      const errorMessage = {
        role: 'assistant',
        content: 'Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta nuevamente.'
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const loadRecommendedAnuncios = async (anuncioIds) => {
    try {
      const allAnunciosResponse = await anuncioService.obtenerTodosLosAnuncios();
      if (allAnunciosResponse.success) {
        const filtered = allAnunciosResponse.anuncios.filter(anuncio =>
          anuncioIds.includes(anuncio.idAnuncio)
        );
        setRecommendedAnuncios(filtered);
      }
    } catch (error) {
      console.error('Error al cargar anuncios recomendados:', error);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const formatearPrecio = (precio) => {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 2,
    }).format(precio);
  };

  const handleVerAnuncio = (idAnuncio) => {
    navigate(`/anuncio/${idAnuncio}`);
  };

  return (
    <div className={`chat-ia-container ${isFloating ? 'chat-ia-floating' : ''}`}>
      <div className="chat-ia-header">
        <div className="chat-ia-header-content">
          <div className="chat-ia-header-title">
            <h2>🤖 Asistente Virtual de Vehículos</h2>
          </div>
          {isFloating && onClose && (
            <button 
              className="chat-ia-close-btn" 
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onClose();
              }} 
              aria-label="Cerrar chat"
              type="button"
            >
              ✕
            </button>
          )}
        </div>
        <p>Pregúntame sobre las características que buscas y te ayudaré a encontrar el auto perfecto</p>
      </div>

      <div className="chat-ia-messages">
        {messages.map((message, index) => (
          <div
            key={index}
            className={`chat-message ${message.role === 'user' ? 'user-message' : 'assistant-message'}`}
          >
            <div className="message-content">
              {message.content}
            </div>
          </div>
        ))}
        
        {loading && (
          <div className="chat-message assistant-message">
            <div className="message-content">
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      {recommendedAnuncios.length > 0 && (
        <div className="chat-recommendations">
          <h3>Vehículos Recomendados</h3>
          <div className="recommendations-grid">
            {recommendedAnuncios.map((anuncio) => (
              <div key={anuncio.idAnuncio} className="recommendation-card">
                {anuncio.imagenes && anuncio.imagenes.length > 0 && (
                  <img
                    src={normalizeImageUrl(anuncio.imagenes[0].urlImagen)}
                    alt={anuncio.titulo || anuncio.modelo}
                    className="recommendation-image"
                    onError={(e) => {
                      e.target.src = 'https://via.placeholder.com/200x150?text=Sin+Imagen';
                    }}
                  />
                )}
                <div className="recommendation-content">
                  <h4>{anuncio.titulo || `${anuncio.modelo} ${anuncio.anio}`}</h4>
                  <div className="recommendation-details">
                    <span>Año: {anuncio.anio}</span>
                    <span>Km: {anuncio.kilometraje?.toLocaleString('es-PE')}</span>
                    {anuncio.tipoVehiculo && (
                      <span>Tipo: {anuncio.tipoVehiculo}</span>
                    )}
                  </div>
                  <div className="recommendation-price">
                    {formatearPrecio(anuncio.precio)}
                  </div>
                  <button
                    className="btn-ver-anuncio"
                    onClick={() => handleVerAnuncio(anuncio.idAnuncio)}
                  >
                    Ver Detalles
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="chat-ia-input">
        <textarea
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Escribe tu mensaje aquí..."
          rows="2"
          disabled={loading}
        />
        <button
          onClick={handleSendMessage}
          disabled={!inputMessage.trim() || loading}
          className="btn-send"
        >
          {loading ? 'Enviando...' : 'Enviar'}
        </button>
      </div>
    </div>
  );
};

export default ChatIA;

