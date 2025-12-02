import React, { useState, useEffect } from 'react';
import { useAuth } from './AuthProvider';
import { useSearchParams, useNavigate, useLocation } from 'react-router-dom';
import anuncioService from '../services/anuncioApiService';
import notificacionService from '../services/notificacionService';
import { setupAuthInterceptor } from '../services/apiService';
import './ListaAnuncios.css';

const ListaAnuncios = () => {
  const { isAuthenticated, getIdTokenClaims } = useAuth();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const tipoVehiculo = searchParams.get('tipo');
  const [anuncios, setAnuncios] = useState([]);
  const [anunciosFiltrados, setAnunciosFiltrados] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userId, setUserId] = useState(null);
  const [eliminando, setEliminando] = useState(null);
  const [imagenActual, setImagenActual] = useState({}); // { idAnuncio: indiceImagen }
  const [contactando, setContactando] = useState(null); // { idAnuncio: true/false }
  const [mostrarModalContacto, setMostrarModalContacto] = useState(false);
  const [anuncioSeleccionado, setAnuncioSeleccionado] = useState(null);
  const [mensajeContacto, setMensajeContacto] = useState('');
  
  // Detectar si estamos en "Mis Anuncios" (desde Dashboard con tab=anuncios)
  // Recalcular cada vez que cambien los searchParams
  const esMisAnuncios = React.useMemo(() => {
    return location.pathname === '/' && searchParams.get('tab') === 'anuncios';
  }, [location.pathname, searchParams]);
  
  // Verificar si hay un tipo de vehículo válido (no vacío y no solo "=")
  const tieneTipoValido = tipoVehiculo && tipoVehiculo.trim() !== '' && tipoVehiculo.trim() !== '=';

  useEffect(() => {
    obtenerUserId();
    
    // Configurar el interceptor de autenticación
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
    }
  }, [isAuthenticated, getIdTokenClaims]);

  useEffect(() => {
    // Cargar anuncios cuando cambie la autenticación, esMisAnuncios o userId
    if (esMisAnuncios) {
      // Si estamos en "Mis Anuncios", SOLO cargar los anuncios del usuario
      if (!isAuthenticated) {
        setAnuncios([]);
        setAnunciosFiltrados([]);
        setLoading(false);
        setError('Debes iniciar sesión para ver tus anuncios');
      } else if (userId) {
        // Solo cargar cuando tengamos el userId
        cargarAnuncios();
      } else {
        // Esperar a que se obtenga el userId
        setLoading(true);
      }
    } else {
      // Para otras vistas (no Mis Anuncios), cargar todos los anuncios
      cargarAnuncios();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, esMisAnuncios, userId]);

  useEffect(() => {
    // No filtrar por tipo si es "Mis Anuncios"
    if (esMisAnuncios) {
      setAnunciosFiltrados(anuncios);
    } else if (tipoVehiculo && anuncios.length > 0) {
      // Filtrar anuncios por tipo de vehículo usando el campo tipoVehiculo
      const filtrados = anuncios.filter(anuncio => {
        const anuncioTipo = (anuncio.tipoVehiculo || '').trim();
        return anuncioTipo.toLowerCase() === tipoVehiculo.toLowerCase();
      });
      setAnunciosFiltrados(filtrados);
    } else {
      setAnunciosFiltrados(anuncios);
    }
  }, [tipoVehiculo, anuncios, esMisAnuncios]);

  const obtenerUserId = async () => {
    if (isAuthenticated && getIdTokenClaims) {
      try {
        const claims = await getIdTokenClaims();
        setUserId(claims.sub);
      } catch (error) {
        console.error('Error al obtener userId:', error);
      }
    }
  };

  const cargarAnuncios = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Si es "Mis Anuncios", SOLO cargar los anuncios del usuario
      if (esMisAnuncios) {
        if (!isAuthenticated) {
          setError('Debes iniciar sesión para ver tus anuncios');
          setAnuncios([]);
          return;
        }
        
        if (!userId) {
          setError('Error: No se pudo identificar al usuario');
          setAnuncios([]);
          return;
        }
        
        const response = await anuncioService.obtenerMisAnuncios();
        if (response.success) {
          const anunciosRecibidos = response.anuncios || [];
          // Filtrar SOLO los anuncios del usuario actual como medida de seguridad
          const misAnuncios = anunciosRecibidos.filter(anuncio => {
            return anuncio.idUsuario === userId;
          });
          setAnuncios(misAnuncios);
        } else {
          setError('Error al cargar tus anuncios');
          setAnuncios([]);
        }
      } else {
        // Cargar todos los anuncios (para otras vistas)
        const response = await anuncioService.obtenerTodosLosAnuncios();
        if (response.success) {
          setAnuncios(response.anuncios || []);
        } else {
          setError('Error al cargar los anuncios');
        }
      }
    } catch (err) {
      console.error('Error al cargar anuncios:', err);
      if (esMisAnuncios) {
        setError('Error al cargar tus anuncios. Por favor, intenta nuevamente.');
        setAnuncios([]);
      } else {
        setError('Error al cargar los anuncios. Por favor, intenta nuevamente.');
      }
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

  const formatearFecha = (fecha) => {
    if (!fecha) return '';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-PE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const handleEliminar = async (idAnuncio) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar este anuncio?')) {
      return;
    }

    try {
      setEliminando(idAnuncio);
      const response = await anuncioService.eliminarAnuncio(idAnuncio);
      
      if (response.success) {
        // Recargar la lista de anuncios
        await cargarAnuncios();
        alert('Anuncio eliminado exitosamente');
      } else {
        alert('Error al eliminar el anuncio: ' + (response.error || 'Error desconocido'));
      }
    } catch (err) {
      console.error('Error al eliminar anuncio:', err);
      const errorMessage = err.response?.data?.error || err.message || 'Error al eliminar el anuncio';
      alert('Error al eliminar el anuncio: ' + errorMessage);
    } finally {
      setEliminando(null);
    }
  };

  const esMiAnuncio = (anuncio) => {
    return isAuthenticated && userId && anuncio.idUsuario === userId;
  };

  const abrirModalContacto = (anuncio) => {
    if (!isAuthenticated) {
      alert('Debes iniciar sesión para contactar al vendedor');
      return;
    }

    if (esMiAnuncio(anuncio)) {
      alert('No puedes contactarte contigo mismo');
      return;
    }

    setAnuncioSeleccionado(anuncio);
    setMensajeContacto(`Hola, estoy interesado en tu vehículo: ${anuncio.titulo || `${anuncio.modelo} ${anuncio.anio}`}. Me gustaría obtener más información.`);
    setMostrarModalContacto(true);
  };

  const cerrarModalContacto = () => {
    setMostrarModalContacto(false);
    setAnuncioSeleccionado(null);
    setMensajeContacto('');
  };

  const handleEnviarMensaje = async () => {
    if (!mensajeContacto.trim()) {
      alert('Por favor, escribe un mensaje');
      return;
    }

    if (!anuncioSeleccionado) {
      return;
    }

    try {
      setContactando({ ...contactando, [anuncioSeleccionado.idAnuncio]: true });
      
      const response = await notificacionService.contactarVendedor(
        anuncioSeleccionado.idUsuario,
        anuncioSeleccionado.idAnuncio,
        mensajeContacto.trim()
      );

      if (response.success) {
        alert('¡Mensaje enviado exitosamente! El vendedor recibirá tu mensaje.');
        cerrarModalContacto();
      } else {
        alert('Error al enviar el mensaje: ' + (response.error || 'Error desconocido'));
      }
    } catch (err) {
      console.error('Error al enviar mensaje:', err);
      
      let errorMessage = 'Error al enviar el mensaje';
      
      if (err.response) {
        // El servidor respondió con un código de error
        errorMessage = err.response.data?.error || err.response.data?.message || `Error ${err.response.status}: ${err.response.statusText}`;
      } else if (err.request) {
        // La petición se hizo pero no hubo respuesta
        errorMessage = 'No se pudo conectar con el servidor. Verifica que el backend esté corriendo en http://localhost:8080';
      } else {
        // Algo más pasó
        errorMessage = err.message || 'Error desconocido';
      }
      
      alert('Error al enviar el mensaje: ' + errorMessage);
    } finally {
      setContactando({ ...contactando, [anuncioSeleccionado?.idAnuncio]: false });
    }
  };

  const cambiarImagen = (idAnuncio, direccion, totalImagenes) => {
    const indiceActual = imagenActual[idAnuncio] || 0;
    let nuevoIndice;
    
    if (direccion === 'siguiente') {
      nuevoIndice = (indiceActual + 1) % totalImagenes;
    } else {
      nuevoIndice = (indiceActual - 1 + totalImagenes) % totalImagenes;
    }
    
    setImagenActual({
      ...imagenActual,
      [idAnuncio]: nuevoIndice
    });
  };

  const obtenerImagenActual = (anuncio) => {
    const indice = imagenActual[anuncio.idAnuncio] || 0;
    return anuncio.imagenes && anuncio.imagenes[indice] ? anuncio.imagenes[indice] : null;
  };

  if (loading) {
    return (
      <div className="lista-anuncios-loading">
        <p>Cargando anuncios...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="lista-anuncios-error">
        <p>{error}</p>
        <button onClick={cargarAnuncios} className="retry-button">
          Reintentar
        </button>
      </div>
    );
  }

  // En "Mis Anuncios" siempre mostrar los anuncios sin filtrar por tipo
  const anunciosAMostrar = esMisAnuncios ? anuncios : (tipoVehiculo ? anunciosFiltrados : anuncios);

  if (anunciosAMostrar.length === 0) {
    return (
      <div className="lista-anuncios-empty">
        <p>
          {esMisAnuncios
            ? 'Aún no tienes anuncios publicados'
            : tipoVehiculo 
            ? `No hay anuncios disponibles para ${tipoVehiculo} en este momento.`
            : 'No hay anuncios disponibles en este momento.'}
        </p>
      </div>
    );
  }

  return (
    <div className="lista-anuncios-container">
      <div className="lista-anuncios-header">
        <h2>
          {tieneTipoValido ? `Vehículos ${tipoVehiculo} en Venta` : 'Tus Anuncios'}
        </h2>
        <p>
          {esMisAnuncios 
            ? `${anunciosAMostrar.length} ${anunciosAMostrar.length === 1 ? 'anuncio publicado' : 'anuncios publicados'}`
            : `${anunciosAMostrar.length} ${anunciosAMostrar.length === 1 ? 'anuncio disponible' : 'anuncios disponibles'}${tipoVehiculo && anuncios.length > anunciosAMostrar.length ? ` (de ${anuncios.length} total)` : ''}`}
        </p>
      </div>
      <div className="lista-anuncios-grid">
        {anunciosAMostrar.map((anuncio) => {
          const tieneMultiplesImagenes = anuncio.imagenes && anuncio.imagenes.length > 1;
          const imagenMostrada = obtenerImagenActual(anuncio);
          const indiceActual = imagenActual[anuncio.idAnuncio] || 0;
          
          return (
            <div key={anuncio.idAnuncio} className="anuncio-card">
              <div className="anuncio-imagen-container">
                {anuncio.imagenes && anuncio.imagenes.length > 0 ? (
                  <>
                    {anuncio.tipoVehiculo && (
                      <div className="anuncio-badge">
                        {anuncio.tipoVehiculo.toUpperCase()}
                      </div>
                    )}
                    <img
                      src={`http://localhost:8080${imagenMostrada?.urlImagen || anuncio.imagenes[0].urlImagen}`}
                      alt={anuncio.titulo || anuncio.modelo}
                      className="anuncio-imagen"
                      onError={(e) => {
                        e.target.src = 'https://via.placeholder.com/300x200?text=Sin+Imagen';
                      }}
                    />
                    {tieneMultiplesImagenes && (
                      <div className="anuncio-imagen-controls">
                        <button
                          className="imagen-control-button imagen-control-prev"
                          onClick={(e) => {
                            e.stopPropagation();
                            cambiarImagen(anuncio.idAnuncio, 'anterior', anuncio.imagenes.length);
                          }}
                          aria-label="Imagen anterior"
                        >
                          ‹
                        </button>
                        <div className="imagen-indicadores">
                          {anuncio.imagenes.map((_, index) => (
                            <span
                              key={index}
                              className={`imagen-indicador ${index === indiceActual ? 'active' : ''}`}
                              onClick={(e) => {
                                e.stopPropagation();
                                setImagenActual({
                                  ...imagenActual,
                                  [anuncio.idAnuncio]: index
                                });
                              }}
                            />
                          ))}
                        </div>
                        <button
                          className="imagen-control-button imagen-control-next"
                          onClick={(e) => {
                            e.stopPropagation();
                            cambiarImagen(anuncio.idAnuncio, 'siguiente', anuncio.imagenes.length);
                          }}
                          aria-label="Siguiente imagen"
                        >
                          ›
                        </button>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="anuncio-imagen-placeholder">
                    <span>Sin Imagen</span>
                  </div>
                )}
              </div>
            <div className="anuncio-content">
              <div className="anuncio-header-info">
                <h3 className="anuncio-modelo">
                  {anuncio.modelo}
                </h3>
                <p className="anuncio-anio">{anuncio.anio}</p>
              </div>
              <div className="anuncio-precio-destacado">
                {formatearPrecio(anuncio.precio)}
              </div>
              <div className="anuncio-specs">
                {anuncio.tipoVehiculo && (
                  <div className="anuncio-spec-item">
                    <span className="spec-icon">●</span>
                    <span className="spec-text">{anuncio.tipoVehiculo}</span>
                  </div>
                )}
                <div className="anuncio-spec-item">
                  <span className="spec-icon">●</span>
                  <span className="spec-text">{anuncio.kilometraje?.toLocaleString('es-PE')} km</span>
                </div>
              </div>
              <div className="anuncio-separator"></div>
              <div className="anuncio-actions-new">
                <a 
                  href={`/anuncio/${anuncio.idAnuncio}`}
                  className="anuncio-ver-detalles"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(`/anuncio/${anuncio.idAnuncio}`);
                  }}
                >
                  Ver Detalles
                </a>
                {esMiAnuncio(anuncio) && (
                  <button
                    className="anuncio-delete-button-new"
                    onClick={(e) => {
                      e.preventDefault();
                      handleEliminar(anuncio.idAnuncio);
                    }}
                    disabled={eliminando === anuncio.idAnuncio}
                  >
                    {eliminando === anuncio.idAnuncio ? 'Eliminando...' : '🗑️ Eliminar'}
                  </button>
                )}
              </div>
            </div>
          </div>
        );
        })}
      </div>

      {/* Modal de Contacto */}
      {mostrarModalContacto && anuncioSeleccionado && (
        <div className="modal-overlay" onClick={cerrarModalContacto}>
          <div className="modal-contacto" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>💬 Enviar Mensaje al Vendedor</h3>
              <button className="modal-close" onClick={cerrarModalContacto}>×</button>
            </div>
            <div className="modal-body">
              <div className="anuncio-info-modal">
                <h4>{anuncioSeleccionado.titulo || `${anuncioSeleccionado.modelo} ${anuncioSeleccionado.anio}`}</h4>
                <p className="anuncio-precio-modal">{formatearPrecio(anuncioSeleccionado.precio)}</p>
              </div>
              <div className="mensaje-form-group">
                <label htmlFor="mensaje-contacto">Tu mensaje:</label>
                <textarea
                  id="mensaje-contacto"
                  className="mensaje-textarea"
                  rows="6"
                  value={mensajeContacto}
                  onChange={(e) => setMensajeContacto(e.target.value)}
                  placeholder="Escribe tu mensaje aquí. Por ejemplo: Hola, estoy interesado en este vehículo, ¿podrías darme más información?"
                />
                <p className="mensaje-hint">
                  El vendedor recibirá una notificación con tu mensaje
                </p>
              </div>
            </div>
            <div className="modal-footer">
              <button
                className="btn-cancelar"
                onClick={cerrarModalContacto}
                disabled={contactando && contactando[anuncioSeleccionado.idAnuncio]}
              >
                Cancelar
              </button>
              <button
                className="btn-enviar"
                onClick={handleEnviarMensaje}
                disabled={contactando && contactando[anuncioSeleccionado.idAnuncio] || !mensajeContacto.trim()}
              >
                {contactando && contactando[anuncioSeleccionado.idAnuncio] 
                  ? 'Enviando...' 
                  : '📤 Enviar Mensaje'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ListaAnuncios;

