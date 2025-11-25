import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import notificacionService from '../services/notificacionService';
import { setupAuthInterceptor } from '../services/apiService';
import './Notificaciones.css';

const Notificaciones = () => {
  const { isAuthenticated, getIdTokenClaims } = useAuth0();
  const [notificaciones, setNotificaciones] = useState([]);
  const [cantidadNoLeidas, setCantidadNoLeidas] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      // Configurar el interceptor de autenticación
      setupAuthInterceptor(getIdTokenClaims);
      cargarNotificaciones();
      // Recargar notificaciones cada 30 segundos
      const interval = setInterval(cargarNotificaciones, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, getIdTokenClaims]);

  const cargarNotificaciones = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('Cargando notificaciones...');
      // Obtener TODAS las notificaciones, no solo las no leídas
      const response = await notificacionService.obtenerMisNotificaciones();
      
      console.log('Respuesta de notificaciones:', response);
      
      if (response.success) {
        const todas = response.notificaciones || [];
        // Contar las no leídas para el badge
        const noLeidas = todas.filter(n => !n.leida && !n.leido);
        const cantidad = noLeidas.length;
        console.log(`Notificaciones recibidas: ${todas.length}, Cantidad no leídas: ${cantidad}`);
        setNotificaciones(todas);
        setCantidadNoLeidas(cantidad);
      } else {
        console.error('Error en respuesta:', response.error);
        setError(response.error || 'Error al cargar las notificaciones');
      }
    } catch (err) {
      console.error('Error al cargar notificaciones:', err);
      console.error('Error completo:', {
        message: err.message,
        response: err.response,
        request: err.request
      });
      setError('Error al cargar las notificaciones: ' + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const marcarComoLeida = async (idNotificacion) => {
    try {
      const response = await notificacionService.marcarComoLeida(idNotificacion);
      if (response.success) {
        // Actualizar la lista de notificaciones
        setNotificaciones(prev => 
          prev.map(notif => 
            notif.idNotificacion === idNotificacion 
              ? { ...notif, leida: true }
              : notif
          )
        );
        setCantidadNoLeidas(prev => Math.max(0, prev - 1));
      }
    } catch (err) {
      console.error('Error al marcar notificación como leída:', err);
      alert('Error al marcar la notificación como leída');
    }
  };

  const marcarTodasComoLeidas = async () => {
    try {
      const response = await notificacionService.marcarTodasComoLeidas();
      if (response.success) {
        setNotificaciones(prev => prev.map(notif => ({ ...notif, leida: true })));
        setCantidadNoLeidas(0);
      }
    } catch (err) {
      console.error('Error al marcar todas como leídas:', err);
      alert('Error al marcar todas las notificaciones como leídas');
    }
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-PE', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading) {
    return (
      <div className="notificaciones-loading">
        <p>Cargando notificaciones...</p>
      </div>
    );
  }

  return (
    <div className="notificaciones-container">
      <div className="notificaciones-header">
        <h3>🔔 Notificaciones</h3>
        {cantidadNoLeidas > 0 && (
          <span className="notificaciones-badge">{cantidadNoLeidas}</span>
        )}
        {notificaciones.length > 0 && (
          <button 
            className="marcar-todas-leidas-btn"
            onClick={marcarTodasComoLeidas}
          >
            Marcar todas como leídas
          </button>
        )}
      </div>

      {error && (
        <div className="notificaciones-error">
          <p>{error}</p>
          <button onClick={cargarNotificaciones}>Reintentar</button>
        </div>
      )}

      {notificaciones.length === 0 ? (
        <div className="notificaciones-empty">
          <p>No tienes notificaciones</p>
        </div>
      ) : (
        <div className="notificaciones-list">
          {notificaciones.map((notificacion) => (
            <div 
              key={notificacion.idNotificacion} 
              className={`notificacion-item ${(notificacion.leida || notificacion.leido) ? 'leida' : 'no-leida'}`}
            >
              <div className="notificacion-content">
                {notificacion.titulo && (
                  <h4 className="notificacion-titulo">{notificacion.titulo}</h4>
                )}
                {(notificacion.nombreComprador || notificacion.emailComprador) && (
                  <div className="notificacion-comprador">
                    <span className="comprador-label">De: </span>
                    <span className="comprador-info">
                      {notificacion.nombreComprador || notificacion.emailComprador}
                      {notificacion.nombreComprador && notificacion.emailComprador && (
                        <span className="comprador-email"> ({notificacion.emailComprador})</span>
                      )}
                    </span>
                  </div>
                )}
                <p className="notificacion-mensaje">{notificacion.mensaje}</p>
                <p className="notificacion-fecha">
                  {formatearFecha(notificacion.fechaCreacion)}
                </p>
              </div>
              {!(notificacion.leida || notificacion.leido) && (
                <button
                  className="marcar-leida-btn"
                  onClick={() => marcarComoLeida(notificacion.idNotificacion)}
                  title="Marcar como leída"
                >
                  ✓
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Notificaciones;

