import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import chatService from '../services/chatService';
import anuncioService from '../services/anuncioApiService';
import { setupAuthInterceptor } from '../services/apiService';
import './ChatScreen.css';

const ChatScreen = () => {
  const { idConversacion } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, getIdTokenClaims, user } = useAuth();
  const [conversacion, setConversacion] = useState(null);
  const [mensajes, setMensajes] = useState([]);
  const [nuevoMensaje, setNuevoMensaje] = useState('');
  const [loading, setLoading] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const [anuncio, setAnuncio] = useState(null);
  const mensajesEndRef = useRef(null);
  const chatContainerRef = useRef(null);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      cargarConversacion();
      cargarMensajes();
      
      // Actualizar mensajes cada 5 segundos
      const interval = setInterval(() => {
        cargarMensajes();
      }, 5000);
      
      return () => clearInterval(interval);
    }
  }, [idConversacion, isAuthenticated, getIdTokenClaims]);

  useEffect(() => {
    scrollToBottom();
  }, [mensajes]);

  const scrollToBottom = () => {
    mensajesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const cargarConversacion = async () => {
    try {
      const response = await chatService.obtenerConversacion(idConversacion);
      if (response.success) {
        setConversacion(response.conversacion);
        // Cargar información del anuncio
        if (response.conversacion.idAnuncio) {
          cargarAnuncio(response.conversacion.idAnuncio);
        }
      }
    } catch (err) {
      console.error('Error al cargar conversación:', err);
      setError('Error al cargar la conversación');
    }
  };

  const cargarAnuncio = async (idAnuncio) => {
    try {
      const response = await anuncioService.obtenerAnuncioPorId(idAnuncio);
      if (response.success && response.anuncio) {
        setAnuncio(response.anuncio);
      }
    } catch (err) {
      console.error('Error al cargar anuncio:', err);
    }
  };

  const cargarMensajes = async () => {
    try {
      const response = await chatService.obtenerMensajes(idConversacion);
      if (response.success) {
        setMensajes(response.mensajes || []);
        // Marcar como leídos si hay mensajes nuevos
        if (response.mensajes && response.mensajes.length > 0) {
          const tieneNoLeidos = response.mensajes.some(
            m => !m.leido && m.idRemitente !== user?.sub
          );
          if (tieneNoLeidos) {
            await chatService.marcarComoLeido(idConversacion);
          }
        }
      }
    } catch (err) {
      console.error('Error al cargar mensajes:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEnviarMensaje = async (e) => {
    e.preventDefault();
    if (!nuevoMensaje.trim() || enviando) return;

    try {
      setEnviando(true);
      const response = await chatService.enviarMensaje(idConversacion, nuevoMensaje.trim());
      if (response.success) {
        setNuevoMensaje('');
        // Recargar mensajes
        await cargarMensajes();
        await cargarConversacion();
      }
    } catch (err) {
      console.error('Error al enviar mensaje:', err);
      setError('Error al enviar el mensaje. Por favor, intenta nuevamente.');
    } finally {
      setEnviando(false);
    }
  };

  const esMiMensaje = (mensaje) => {
    return mensaje.idRemitente === user?.sub;
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '';
    const date = new Date(fecha);
    const ahora = new Date();
    const diff = ahora - date;
    const minutos = Math.floor(diff / 60000);

    if (minutos < 1) return 'Ahora';
    if (minutos < 60) return `Hace ${minutos} min`;
    
    return date.toLocaleTimeString('es-PE', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  if (!isAuthenticated) {
    return (
      <div className="chat-screen-container">
        <div className="chat-screen-message">
          <p>Debes iniciar sesión para ver esta conversación.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="chat-screen-container">
        <div className="chat-screen-loading">
          <p>Cargando conversación...</p>
        </div>
      </div>
    );
  }

  if (error && !conversacion) {
    return (
      <div className="chat-screen-container">
        <div className="chat-screen-error">
          <p>{error}</p>
          <button onClick={() => navigate('/conversaciones')}>
            Volver a Conversaciones
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-screen-container">
      <div className="chat-screen-header">
        <button 
          className="btn-volver"
          onClick={() => navigate('/conversaciones')}
        >
          ← Volver
        </button>
        <div className="chat-header-info">
          <h2>
            {anuncio ? `${anuncio.modelo} ${anuncio.anio}` : `Anuncio #${conversacion?.idAnuncio}`}
          </h2>
          <span className="chat-header-usuario">
            {conversacion?.idVendedor === user?.sub ? 'Comprador' : 'Vendedor'}
          </span>
        </div>
        {anuncio && (
          <button
            className="btn-ver-anuncio"
            onClick={() => navigate(`/anuncio/${anuncio.idAnuncio}`)}
          >
            Ver Anuncio
          </button>
        )}
      </div>

      {error && (
        <div className="chat-screen-error-message">
          <p>{error}</p>
        </div>
      )}

      <div className="chat-mensajes-container" ref={chatContainerRef}>
        {mensajes.length === 0 ? (
          <div className="chat-empty">
            <p>No hay mensajes aún. ¡Envía el primero!</p>
          </div>
        ) : (
          mensajes.map((mensaje) => (
            <div
              key={mensaje.idMensaje}
              className={`mensaje-burbuja ${esMiMensaje(mensaje) ? 'mensaje-propio' : 'mensaje-otro'}`}
            >
              <div className="mensaje-contenido">
                <p>{mensaje.mensaje}</p>
                <span className="mensaje-fecha">
                  {formatearFecha(mensaje.fechaEnvio)}
                </span>
              </div>
            </div>
          ))
        )}
        <div ref={mensajesEndRef} />
      </div>

      <form className="chat-input-form" onSubmit={handleEnviarMensaje}>
        <input
          type="text"
          className="chat-input"
          value={nuevoMensaje}
          onChange={(e) => setNuevoMensaje(e.target.value)}
          placeholder="Escribe un mensaje..."
          disabled={enviando}
        />
        <button
          type="submit"
          className="btn-enviar-chat"
          disabled={!nuevoMensaje.trim() || enviando}
        >
          {enviando ? 'Enviando...' : 'Enviar'}
        </button>
      </form>
    </div>
  );
};

export default ChatScreen;

