import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import anuncioService from '../services/anuncioApiService';
import notificacionService from '../services/notificacionService';
import { setupAuthInterceptor } from '../services/apiService';
import { normalizeImageUrl } from '../utils/imageUtils';
import './DetalleAnuncio.css';

const DetalleAnuncio = () => {
  const { idAnuncio } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, getIdTokenClaims, user, loginWithRedirect } = useAuth();
  const [anuncio, setAnuncio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [imagenActual, setImagenActual] = useState(0);
  const [mostrarModalContacto, setMostrarModalContacto] = useState(false);
  const [mensajeContacto, setMensajeContacto] = useState('');
  const [contactando, setContactando] = useState(false);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
    }
    cargarAnuncio();
  }, [idAnuncio, isAuthenticated, getIdTokenClaims]);

  const cargarAnuncio = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await anuncioService.obtenerTodosLosAnuncios();
      if (response.success) {
        const anuncioEncontrado = response.anuncios.find(a => a.idAnuncio === parseInt(idAnuncio));
        if (anuncioEncontrado) {
          setAnuncio(anuncioEncontrado);
        } else {
          setError('Anuncio no encontrado');
        }
      } else {
        setError('Error al cargar el anuncio');
      }
    } catch (err) {
      console.error('Error al cargar anuncio:', err);
      setError('Error al cargar el anuncio. Por favor, intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const formatearPrecio = (precio) => {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 2,
    }).format(precio);
  };

  const cambiarImagen = (direccion) => {
    if (!anuncio || !anuncio.imagenes || anuncio.imagenes.length <= 1) return;
    
    if (direccion === 'siguiente') {
      setImagenActual((prev) => (prev + 1) % anuncio.imagenes.length);
    } else {
      setImagenActual((prev) => (prev - 1 + anuncio.imagenes.length) % anuncio.imagenes.length);
    }
  };

  const handleContactar = async () => {
    if (!isAuthenticated) {
      // Si no está autenticado, redirigir al login
      navigate(`/login?returnTo=/anuncio/${idAnuncio}`);
      return;
    }

    if (!mensajeContacto.trim()) {
      alert('Por favor ingresa un mensaje');
      return;
    }

    try {
      setContactando(true);
      const response = await notificacionService.contactarVendedor(
        anuncio.idUsuario,
        anuncio.idAnuncio,
        mensajeContacto.trim()
      );

      if (response.success) {
        alert('¡Mensaje enviado exitosamente! El vendedor recibirá tu mensaje.');
        setMostrarModalContacto(false);
        setMensajeContacto('');
      } else {
        alert('Error al enviar el mensaje: ' + (response.error || 'Error desconocido'));
      }
    } catch (err) {
      console.error('Error al enviar mensaje:', err);
      alert('Error al enviar el mensaje. Por favor, intenta nuevamente.');
    } finally {
      setContactando(false);
    }
  };

  if (loading) {
    return (
      <div className="detalle-anuncio-loading">
        <p>Cargando detalles del vehículo...</p>
      </div>
    );
  }

  if (error || !anuncio) {
    return (
      <div className="detalle-anuncio-error">
        <p>{error || 'Anuncio no encontrado'}</p>
        <button onClick={() => navigate('/')} className="btn-volver">
          Volver al inicio
        </button>
      </div>
    );
  }

  const imagenMostrada = anuncio.imagenes && anuncio.imagenes[imagenActual] 
    ? anuncio.imagenes[imagenActual].urlImagen 
    : null;
  const tieneMultiplesImagenes = anuncio.imagenes && anuncio.imagenes.length > 1;
  const esPropietario = isAuthenticated && user && anuncio.idUsuario === user.sub;

  return (
    <div className="detalle-anuncio-container">
      <div className="detalle-anuncio-header">
        <button onClick={() => navigate('/')} className="btn-volver-detalle">
          ← Volver
        </button>
      </div>

      <div className="detalle-anuncio-content">
        <div className="detalle-anuncio-imagenes">
          {imagenMostrada ? (
            <div className="imagen-principal-container">
              <img
                src={normalizeImageUrl(imagenMostrada)}
                alt={anuncio.modelo || 'Vehículo'}
                className="imagen-principal"
                onError={(e) => {
                  e.target.src = 'https://via.placeholder.com/800x500?text=Sin+Imagen';
                }}
              />
              {tieneMultiplesImagenes && (
                <>
                  <button
                    className="imagen-nav-button imagen-nav-prev"
                    onClick={() => cambiarImagen('anterior')}
                  >
                    ‹
                  </button>
                  <button
                    className="imagen-nav-button imagen-nav-next"
                    onClick={() => cambiarImagen('siguiente')}
                  >
                    ›
                  </button>
                  <div className="imagen-indicadores-detalle">
                    {anuncio.imagenes.map((_, index) => (
                      <span
                        key={index}
                        className={`indicador ${index === imagenActual ? 'active' : ''}`}
                        onClick={() => setImagenActual(index)}
                      />
                    ))}
                  </div>
                </>
              )}
            </div>
          ) : (
            <div className="imagen-placeholder-detalle">
              <span className="vehicle-icon">Auto</span>
            </div>
          )}

          {tieneMultiplesImagenes && anuncio.imagenes.length > 1 && (
            <div className="imagenes-miniatura">
              {anuncio.imagenes.slice(0, 4).map((imagen, index) => (
                <img
                  key={index}
                  src={normalizeImageUrl(imagen.urlImagen)}
                  alt={`Vista ${index + 1}`}
                  className={`miniatura ${index === imagenActual ? 'active' : ''}`}
                  onClick={() => setImagenActual(index)}
                  onError={(e) => {
                    e.target.style.display = 'none';
                  }}
                />
              ))}
            </div>
          )}
        </div>

        <div className="detalle-anuncio-info">
          <div className="detalle-anuncio-titulo">
            <h1>{anuncio.modelo || 'Sin modelo'}</h1>
            {anuncio.anio && <span className="anio-badge">{anuncio.anio}</span>}
          </div>

          <div className="detalle-anuncio-precio">
            {formatearPrecio(anuncio.precio || 0)}
          </div>

          <div className="detalle-anuncio-specs">
            {anuncio.tipoVehiculo && (
              <div className="spec-item">
                <span className="spec-label">Tipo:</span>
                <span className="spec-value">{anuncio.tipoVehiculo}</span>
              </div>
            )}
            {anuncio.kilometraje && (
              <div className="spec-item">
                <span className="spec-label">Kilometraje:</span>
                <span className="spec-value">{anuncio.kilometraje.toLocaleString('es-PE')} km</span>
              </div>
            )}
          </div>

          {anuncio.descripcion && (
            <div className="detalle-anuncio-descripcion">
              <h3>Descripción</h3>
              <p>{anuncio.descripcion}</p>
            </div>
          )}

          {isAuthenticated && (
            <div className="detalle-anuncio-contacto">
              <h3>Datos de Contacto</h3>
              {anuncio.emailContacto && (
                <div className="contacto-item">
                  <span className="contacto-label">Email:</span>
                  <span className="contacto-value">{anuncio.emailContacto}</span>
                </div>
              )}
              {anuncio.telefonoContacto && (
                <div className="contacto-item">
                  <span className="contacto-label">Teléfono:</span>
                  <span className="contacto-value">{anuncio.telefonoContacto}</span>
                </div>
              )}
            </div>
          )}

          {isAuthenticated && !esPropietario && (
            <button
              className="btn-contactar-detalle"
              onClick={() => setMostrarModalContacto(true)}
            >
              Contactar Vendedor
            </button>
          )}

          {!isAuthenticated && (
            <button
              className="btn-contactar-detalle"
              onClick={() => {
                navigate(`/login?returnTo=/anuncio/${idAnuncio}`);
              }}
            >
              Iniciar Sesión para Contactar
            </button>
          )}
        </div>
      </div>

      {mostrarModalContacto && (
        <div className="modal-overlay-detalle" onClick={() => setMostrarModalContacto(false)}>
          <div className="modal-content-detalle" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header-detalle">
              <h3>Contactar Vendedor</h3>
              <button
                className="modal-close-detalle"
                onClick={() => setMostrarModalContacto(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-body-detalle">
              <p>Envía un mensaje al vendedor sobre este vehículo:</p>
              <textarea
                className="mensaje-textarea-detalle"
                value={mensajeContacto}
                onChange={(e) => setMensajeContacto(e.target.value)}
                placeholder="Escribe tu mensaje aquí..."
                rows="5"
              />
            </div>
            <div className="modal-footer-detalle">
              <button
                className="btn-enviar-mensaje"
                onClick={handleContactar}
                disabled={contactando || !mensajeContacto.trim()}
              >
                {contactando ? 'Enviando...' : 'Enviar Mensaje'}
              </button>
              <button
                className="btn-cancelar"
                onClick={() => setMostrarModalContacto(false)}
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DetalleAnuncio;

