import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import anuncioService from '../services/anuncioApiService';
import { setupAuthInterceptor } from '../services/apiService';
import { useAuth } from './AuthProvider';
import './AnunciosPreview.css';

const AnunciosPreview = ({ limite = 6 }) => {
  const { isAuthenticated, getIdTokenClaims } = useAuth();
  const navigate = useNavigate();
  const [anuncios, setAnuncios] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
    }
    cargarAnuncios();
  }, [isAuthenticated, getIdTokenClaims]);

  const cargarAnuncios = async () => {
    try {
      setLoading(true);
      const response = await anuncioService.obtenerTodosLosAnuncios();
      if (response.success) {
        const anunciosLimitados = (response.anuncios || []).slice(0, limite);
        setAnuncios(anunciosLimitados);
      }
    } catch (err) {
      console.error('Error al cargar anuncios:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatearPrecio = (precio) => {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 0,
    }).format(precio);
  };

  const handleVerDetalles = (idAnuncio) => {
    navigate(`/anuncio/${idAnuncio}`);
  };

  if (loading) {
    return (
      <div className="anuncios-preview-loading">
        <p>Cargando anuncios...</p>
      </div>
    );
  }

  if (anuncios.length === 0) {
    return null;
  }

  return (
    <section className="anuncios-preview-section">
      <div className="anuncios-preview-container">
        <h2 className="anuncios-preview-title">Vehículos en Venta</h2>
        <div className="anuncios-preview-grid">
          {anuncios.map((anuncio) => {
            const imagenPrincipal = anuncio.imagenes && anuncio.imagenes.length > 0 
              ? anuncio.imagenes[0].urlImagen 
              : null;

            return (
              <div key={anuncio.idAnuncio} className="anuncio-preview-card">
                <div className="anuncio-preview-image-container">
                  {imagenPrincipal ? (
                    <img
                      src={`http://localhost:8080${imagenPrincipal}`}
                      alt={anuncio.modelo || 'Vehículo'}
                      className="anuncio-preview-image"
                      onError={(e) => {
                        e.target.src = 'https://via.placeholder.com/300x200?text=Sin+Imagen';
                      }}
                    />
                  ) : (
                    <div className="anuncio-preview-placeholder">
                      <span className="vehicle-icon">Auto</span>
                    </div>
                  )}
                  {anuncio.tipoVehiculo && (
                    <div className="anuncio-preview-badge">
                      {anuncio.tipoVehiculo}
                    </div>
                  )}
                </div>
                <div className="anuncio-preview-content">
                  <div className="anuncio-preview-header">
                    <h3 className="anuncio-preview-modelo">
                      {anuncio.modelo || 'Sin modelo'}
                    </h3>
                    {anuncio.anio && (
                      <span className="anuncio-preview-anio">{anuncio.anio}</span>
                    )}
                  </div>
                  <div className="anuncio-preview-precio">
                    {formatearPrecio(anuncio.precio || 0)}
                  </div>
                  <div className="anuncio-preview-specs">
                    {anuncio.tipoVehiculo && (
                      <div className="anuncio-preview-spec">
                        <span className="spec-icon">●</span>
                        <span>{anuncio.tipoVehiculo}</span>
                      </div>
                    )}
                    {anuncio.kilometraje && (
                      <div className="anuncio-preview-spec">
                        <span className="spec-icon">📊</span>
                        <span>{anuncio.kilometraje.toLocaleString('es-PE')} km</span>
                      </div>
                    )}
                  </div>
                  <div className="anuncio-preview-divider"></div>
                  <button
                    className="anuncio-preview-button"
                    onClick={() => handleVerDetalles(anuncio.idAnuncio)}
                  >
                    Ver Detalles
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default AnunciosPreview;

