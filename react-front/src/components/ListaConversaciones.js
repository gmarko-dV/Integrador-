import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import chatService from '../services/chatService';
import { setupAuthInterceptor } from '../services/apiService';
import anuncioService from '../services/anuncioApiService';
import './ListaConversaciones.css';

const ListaConversaciones = () => {
  const navigate = useNavigate();
  const { isAuthenticated, getIdTokenClaims, user } = useAuth();
  const [conversaciones, setConversaciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [mensajesNoLeidos, setMensajesNoLeidos] = useState(0);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      cargarConversaciones();
      cargarMensajesNoLeidos();
      
      // Actualizar cada 30 segundos
      const interval = setInterval(() => {
        cargarConversaciones();
        cargarMensajesNoLeidos();
      }, 30000);
      
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, getIdTokenClaims]);

  const cargarConversaciones = async () => {
    try {
      setLoading(true);
      const response = await chatService.obtenerConversaciones();
      if (response.success) {
        setConversaciones(response.conversaciones || []);
      }
    } catch (err) {
      console.error('Error al cargar conversaciones:', err);
      setError('Error al cargar las conversaciones');
    } finally {
      setLoading(false);
    }
  };

  const cargarMensajesNoLeidos = async () => {
    try {
      const response = await chatService.contarNoLeidos();
      if (response.success) {
        setMensajesNoLeidos(response.cantidad || 0);
      }
    } catch (err) {
      console.error('Error al cargar mensajes no leídos:', err);
    }
  };

  const obtenerNombreOtroUsuario = (conversacion) => {
    if (!user || !user.sub) return 'Usuario';
    const esVendedor = conversacion.idVendedor === user.sub;
    return esVendedor ? 'Comprador' : 'Vendedor';
  };

  const obtenerUltimoMensaje = (conversacion) => {
    if (conversacion.mensajes && conversacion.mensajes.length > 0) {
      const ultimo = conversacion.mensajes[conversacion.mensajes.length - 1];
      return ultimo.mensaje;
    }
    return 'Sin mensajes';
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '';
    const date = new Date(fecha);
    const ahora = new Date();
    const diff = ahora - date;
    const minutos = Math.floor(diff / 60000);
    const horas = Math.floor(diff / 3600000);
    const dias = Math.floor(diff / 86400000);

    if (minutos < 1) return 'Ahora';
    if (minutos < 60) return `Hace ${minutos} min`;
    if (horas < 24) return `Hace ${horas} h`;
    if (dias < 7) return `Hace ${dias} d`;
    return date.toLocaleDateString('es-PE');
  };

  const contarMensajesNoLeidosConversacion = (conversacion) => {
    if (!conversacion.mensajes || !user || !user.sub) return 0;
    return conversacion.mensajes.filter(
      m => !m.leido && m.idRemitente !== user.sub
    ).length;
  };

  if (!isAuthenticated) {
    return (
      <div className="lista-conversaciones-container">
        <div className="lista-conversaciones-message">
          <p>Debes iniciar sesión para ver tus conversaciones.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="lista-conversaciones-container">
        <div className="lista-conversaciones-loading">
          <p>Cargando conversaciones...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="lista-conversaciones-container">
      <div className="lista-conversaciones-header">
        <button 
          className="btn-volver-lista"
          onClick={() => navigate('/')}
        >
          ← Volver
        </button>
        <div className="header-content">
          <h2>Mis Conversaciones</h2>
          {mensajesNoLeidos > 0 && (
            <span className="badge-no-leidos">{mensajesNoLeidos}</span>
          )}
        </div>
      </div>

      {error && (
        <div className="lista-conversaciones-error">
          <p>{error}</p>
        </div>
      )}

      {conversaciones.length === 0 ? (
        <div className="lista-conversaciones-empty">
          <p>No tienes conversaciones aún.</p>
          <p>Cuando contactes a un vendedor o alguien te contacte, aparecerán aquí.</p>
        </div>
      ) : (
        <div className="lista-conversaciones-grid">
          {conversaciones.map((conversacion) => {
            const noLeidos = contarMensajesNoLeidosConversacion(conversacion);
            return (
              <div
                key={conversacion.idConversacion}
                className="conversacion-card"
                onClick={() => navigate(`/chat/${conversacion.idConversacion}`)}
              >
                <div className="conversacion-avatar">
                  {obtenerNombreOtroUsuario(conversacion).charAt(0)}
                </div>
                <div className="conversacion-content">
                  <div className="conversacion-header">
                    <div className="conversacion-info">
                      <h3>Anuncio #{conversacion.idAnuncio}</h3>
                      <span className="conversacion-usuario">
                        {obtenerNombreOtroUsuario(conversacion)}
                      </span>
                    </div>
                    {noLeidos > 0 && (
                      <span className="badge-mensajes">{noLeidos}</span>
                    )}
                  </div>
                  <div className="conversacion-preview">
                    <p>{obtenerUltimoMensaje(conversacion)}</p>
                    <span className="conversacion-fecha">
                      {formatearFecha(conversacion.fechaUltimoMensaje || conversacion.fechaCreacion)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default ListaConversaciones;

