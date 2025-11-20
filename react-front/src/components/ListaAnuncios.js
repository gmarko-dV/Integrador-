import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import anuncioService from '../services/anuncioApiService';
import './ListaAnuncios.css';

const ListaAnuncios = () => {
  const { isAuthenticated, getIdTokenClaims } = useAuth0();
  const [anuncios, setAnuncios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userId, setUserId] = useState(null);
  const [eliminando, setEliminando] = useState(null);
  const [imagenActual, setImagenActual] = useState({}); // { idAnuncio: indiceImagen }

  useEffect(() => {
    cargarAnuncios();
    obtenerUserId();
  }, [isAuthenticated, getIdTokenClaims]);

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
      const response = await anuncioService.obtenerTodosLosAnuncios();
      if (response.success) {
        setAnuncios(response.anuncios || []);
      } else {
        setError('Error al cargar los anuncios');
      }
    } catch (err) {
      console.error('Error al cargar anuncios:', err);
      setError('Error al cargar los anuncios. Por favor, intenta nuevamente.');
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

  if (anuncios.length === 0) {
    return (
      <div className="lista-anuncios-empty">
        <p>No hay anuncios disponibles en este momento.</p>
      </div>
    );
  }

  return (
    <div className="lista-anuncios-container">
      <div className="lista-anuncios-header">
        <h2>🚗 Vehículos en Venta</h2>
        <p>{anuncios.length} {anuncios.length === 1 ? 'anuncio disponible' : 'anuncios disponibles'}</p>
      </div>
      <div className="lista-anuncios-grid">
        {anuncios.map((anuncio) => {
          const tieneMultiplesImagenes = anuncio.imagenes && anuncio.imagenes.length > 1;
          const imagenMostrada = obtenerImagenActual(anuncio);
          const indiceActual = imagenActual[anuncio.idAnuncio] || 0;
          
          return (
            <div key={anuncio.idAnuncio} className="anuncio-card">
              <div className="anuncio-imagen-container">
                {anuncio.imagenes && anuncio.imagenes.length > 0 ? (
                  <>
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
              <h3 className="anuncio-titulo">
                {anuncio.titulo || `${anuncio.modelo} ${anuncio.anio}`}
              </h3>
              <div className="anuncio-details">
                <div className="anuncio-detail-item">
                  <span className="detail-label">Año:</span>
                  <span className="detail-value">{anuncio.anio}</span>
                </div>
                <div className="anuncio-detail-item">
                  <span className="detail-label">Kilometraje:</span>
                  <span className="detail-value">
                    {anuncio.kilometraje?.toLocaleString('es-PE')} km
                  </span>
                </div>
              </div>
              <p className="anuncio-descripcion">
                {anuncio.descripcion?.length > 100
                  ? `${anuncio.descripcion.substring(0, 100)}...`
                  : anuncio.descripcion}
              </p>
              <div className="anuncio-footer">
                <div className="anuncio-precio">
                  {formatearPrecio(anuncio.precio)}
                </div>
                <div className="anuncio-fecha">
                  {formatearFecha(anuncio.fechaCreacion)}
                </div>
              </div>
              {esMiAnuncio(anuncio) && (
                <div className="anuncio-actions">
                  <button
                    className="anuncio-delete-button"
                    onClick={() => handleEliminar(anuncio.idAnuncio)}
                    disabled={eliminando === anuncio.idAnuncio}
                  >
                    {eliminando === anuncio.idAnuncio ? 'Eliminando...' : '🗑️ Eliminar'}
                  </button>
                </div>
              )}
            </div>
          </div>
        );
        })}
      </div>
    </div>
  );
};

export default ListaAnuncios;

