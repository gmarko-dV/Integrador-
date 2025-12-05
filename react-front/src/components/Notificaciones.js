import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from './AuthProvider';
import notificacionService from '../services/notificacionService';
import { setupAuthInterceptor } from '../services/apiService';
import './Notificaciones.css';

// Hook personalizado para obtener notificaciones
export const useNotificaciones = () => {
  const { isAuthenticated, getIdTokenClaims } = useAuth();
  const [notificaciones, setNotificaciones] = useState([]);
  const [cantidadNoLeidas, setCantidadNoLeidas] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const cargarNotificaciones = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('=== CARGANDO NOTIFICACIONES (Frontend) ===');
      const response = await notificacionService.obtenerMisNotificaciones();
      
      console.log('Respuesta del servidor:', response);
      
      if (response.success) {
        const todas = response.notificaciones || [];
        console.log('Total de notificaciones recibidas:', todas.length);
        const noLeidas = todas.filter(n => !n.leida && !n.leido);
        const cantidad = noLeidas.length;
        console.log('Notificaciones no leídas:', cantidad);
        console.log('Notificaciones:', todas);
        setNotificaciones(todas);
        setCantidadNoLeidas(cantidad);
      } else {
        console.error('Error en respuesta:', response.error);
        setError(response.error || 'Error al cargar las notificaciones');
      }
    } catch (err) {
      console.error('Error al cargar notificaciones:', err);
      console.error('Detalles del error:', err.response?.data);
      setError('Error al cargar las notificaciones: ' + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      cargarNotificaciones();
      const interval = setInterval(cargarNotificaciones, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, getIdTokenClaims, cargarNotificaciones]);

  return {
    notificaciones,
    cantidadNoLeidas,
    loading,
    error,
    cargarNotificaciones,
    setNotificaciones,
    setCantidadNoLeidas
  };
};

// Componente de dropdown de notificaciones
export const NotificationDropdown = () => {
  const { isAuthenticated } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);
  const {
    notificaciones,
    cantidadNoLeidas,
    loading,
    error,
    cargarNotificaciones,
    setNotificaciones,
    setCantidadNoLeidas
  } = useNotificaciones();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      // Recargar notificaciones cuando se abre el dropdown
      console.log('Dropdown abierto, recargando notificaciones...');
      cargarNotificaciones();
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, cargarNotificaciones]);
  
  // Recargar notificaciones periódicamente cuando el dropdown está abierto
  useEffect(() => {
    if (isOpen) {
      const interval = setInterval(() => {
        console.log('Recargando notificaciones automáticamente...');
        cargarNotificaciones();
      }, 5000); // Cada 5 segundos
      
      return () => clearInterval(interval);
    }
  }, [isOpen, cargarNotificaciones]);

  const marcarComoLeida = async (idNotificacion) => {
    try {
      const response = await notificacionService.marcarComoLeida(idNotificacion);
      if (response.success) {
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

  return (
    <div className="notification-dropdown-container" ref={dropdownRef}>
      <button 
        className="notification-bell-button"
        onClick={() => setIsOpen(!isOpen)}
        title="Ver notificaciones"
      >
        <span className="notification-bell-icon">🔔</span>
        {cantidadNoLeidas > 0 && (
          <span className="notification-badge">{cantidadNoLeidas}</span>
        )}
      </button>

      {isOpen && (
        <div className="notification-dropdown">
          <div className="notificaciones-header">
            <h3>
              Notificaciones
              {cantidadNoLeidas > 0 && (
                <span className="notificaciones-badge">{cantidadNoLeidas}</span>
              )}
            </h3>
            {cantidadNoLeidas > 0 && (
              <button 
                className="marcar-todas-leidas-btn"
                onClick={marcarTodasComoLeidas}
                title="Marcar todas como leídas"
              >
                ✓ Todas
              </button>
            )}
          </div>

          {error && (
            <div className="notificaciones-error">
              <p>{error}</p>
              <button onClick={cargarNotificaciones}>Reintentar</button>
            </div>
          )}

          {loading ? (
            <div className="notificaciones-loading">
              <p>Cargando notificaciones...</p>
            </div>
          ) : notificaciones.length === 0 ? (
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
      )}
    </div>
  );
};

// Componente original (mantener para compatibilidad)
const Notificaciones = () => {
  const { isAuthenticated, getIdTokenClaims } = useAuth();
  const [notificaciones, setNotificaciones] = useState([]);
  const [cantidadNoLeidas, setCantidadNoLeidas] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      cargarNotificaciones();
      const interval = setInterval(cargarNotificaciones, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, getIdTokenClaims]);

  const cargarNotificaciones = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await notificacionService.obtenerMisNotificaciones();
      
      if (response.success) {
        const todas = response.notificaciones || [];
        const noLeidas = todas.filter(n => !n.leida && !n.leido);
        const cantidad = noLeidas.length;
        setNotificaciones(todas);
        setCantidadNoLeidas(cantidad);
      } else {
        console.error('Error en respuesta:', response.error);
        setError(response.error || 'Error al cargar las notificaciones');
      }
    } catch (err) {
      console.error('Error al cargar notificaciones:', err);
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
        <h3>
          Notificaciones
          {cantidadNoLeidas > 0 && (
            <span className="notificaciones-badge">{cantidadNoLeidas}</span>
          )}
        </h3>
        {cantidadNoLeidas > 0 && (
          <button 
            className="marcar-todas-leidas-btn"
            onClick={marcarTodasComoLeidas}
            title="Marcar todas como leídas"
          >
            ✓ Todas
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

