import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { API_ENDPOINTS } from '../config/api';
import SpringLogin from './SpringLogin';

const PlateSearch = () => {
  const { user, isAuthenticated } = useAuth0();
  const [plateNumber, setPlateNumber] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [searchResult, setSearchResult] = useState(null);
  const [error, setError] = useState(null);
  const [searchHistory, setSearchHistory] = useState([]);

  const validatePlate = (plate) => {
    // Validación ampliada para placas peruanas
    // Acepta: ABC123, ABC1234, T3V213, A1B2C3, etc.
    const plateRegex = /^[A-Z0-9]{6,7}$/;
    return plateRegex.test(plate.toUpperCase());
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!plateNumber.trim()) {
      setError('Por favor ingresa un número de placa');
      return;
    }

    const formattedPlate = plateNumber.toUpperCase().trim();
    
    if (!validatePlate(formattedPlate)) {
      setError('Formato de placa inválido. Debe tener entre 6-7 caracteres alfanuméricos (ej: ABC123, T3V213)');
      return;
    }

    setIsLoading(true);
    setError(null);
    setSearchResult(null);

    try {
      // Primero, verificar si el usuario está autenticado
      if (!isAuthenticated) {
        throw new Error('Debes iniciar sesión para buscar placas');
      }

      const response = await fetch(API_ENDPOINTS.PLATE_SEARCH, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // Para incluir cookies de sesión
        body: JSON.stringify({
          plateNumber: formattedPlate,
          userId: user?.sub
        })
      });

      if (response.status === 401) {
        throw new Error('Sesión expirada. Por favor, inicia sesión nuevamente.');
      }

      if (response.status === 403) {
        throw new Error('No tienes permisos para realizar esta acción.');
      }

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Error al buscar la placa');
      }

      setSearchResult(data.vehicle);
      setSearchHistory(prev => [data.vehicle, ...prev.slice(0, 4)]); // Mantener últimas 5 búsquedas
      
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClearSearch = () => {
    setPlateNumber('');
    setSearchResult(null);
    setError(null);
  };

  if (!isAuthenticated) {
    return <SpringLogin />;
  }

  return (
    <div className="plate-search-container">
      {/* Formulario de búsqueda */}
      <div className="card">
        <h3>🔍 Búsqueda de Placas de Vehículos</h3>
        <p className="search-description">
          Ingresa el número de placa de un vehículo para obtener información detallada desde la base de datos oficial de Perú.
        </p>
        
        <form onSubmit={handleSearch} className="search-form">
          <div className="input-group">
            <input
              type="text"
              value={plateNumber}
              onChange={(e) => setPlateNumber(e.target.value.toUpperCase())}
              placeholder="Ej: ABC123, T3V213, B6U175"
              className="plate-input"
              maxLength="7"
              disabled={isLoading}
            />
            <button 
              type="submit" 
              className="btn btn-primary search-btn"
              disabled={isLoading || !plateNumber.trim()}
            >
              {isLoading ? (
                <>
                  <span className="spinner-small"></span>
                  Buscando...
                </>
              ) : (
                <>
                  🔍 Buscar
                </>
              )}
            </button>
          </div>
          
          {plateNumber && (
            <button 
              type="button" 
              onClick={handleClearSearch}
              className="btn btn-secondary clear-btn"
              disabled={isLoading}
            >
              ✕ Limpiar
            </button>
          )}
        </form>

        {error && (
          <div className="status-error">
            <strong>Error:</strong> {error}
          </div>
        )}
      </div>

      {/* Resultado de la búsqueda */}
      {searchResult && (
        <div className="card vehicle-result">
          <h3>📋 Información del Vehículo</h3>
          <VehicleDetails vehicle={searchResult} />
        </div>
      )}

      {/* Historial de búsquedas */}
      {searchHistory.length > 0 && (
        <div className="card search-history">
          <h3>📚 Búsquedas Recientes</h3>
          <div className="history-list">
            {searchHistory.map((vehicle, index) => (
              <div key={index} className="history-item" onClick={() => setSearchResult(vehicle)}>
                <span className="plate-badge">{vehicle.placa}</span>
                <span className="vehicle-info">
                  {vehicle.marca} {vehicle.modelo} ({vehicle.anio_registro_api})
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

// Componente para mostrar los detalles del vehículo
const VehicleDetails = ({ vehicle }) => {
  return (
    <div className="vehicle-details">
      <div className="vehicle-header">
        <div className="vehicle-image">
          {vehicle.image_url_api ? (
            <img 
              src={vehicle.image_url_api} 
              alt={`${vehicle.marca} ${vehicle.modelo}`}
              className="vehicle-img"
            />
          ) : (
            <div className="no-image">
              <span>🚗</span>
              <p>Sin imagen disponible</p>
            </div>
          )}
        </div>
        <div className="vehicle-basic-info">
          <h4>{vehicle.marca} {vehicle.modelo}</h4>
          <p className="vehicle-year">Año: {vehicle.anio_registro_api}</p>
          <p className="vehicle-plate">Placa: <strong>{vehicle.placa}</strong></p>
        </div>
      </div>

      <div className="vehicle-specs">
        <div className="specs-grid">
          <div className="spec-item">
            <span className="spec-label">VIN:</span>
            <span className="spec-value">{vehicle.vin || 'No disponible'}</span>
          </div>
          <div className="spec-item">
            <span className="spec-label">Uso:</span>
            <span className="spec-value">{vehicle.uso || 'No disponible'}</span>
          </div>
          <div className="spec-item">
            <span className="spec-label">Propietario:</span>
            <span className="spec-value">{vehicle.propietario || 'No disponible'}</span>
          </div>
          <div className="spec-item">
            <span className="spec-label">Punto de Entrega:</span>
            <span className="spec-value">{vehicle.delivery_point || 'No disponible'}</span>
          </div>
          <div className="spec-item">
            <span className="spec-label">Fecha de Registro:</span>
            <span className="spec-value">{vehicle.fecha_registro_api || 'No disponible'}</span>
          </div>
        </div>
      </div>

      {vehicle.descripcion_api && (
        <div className="vehicle-description">
          <h5>Descripción:</h5>
          <p>{vehicle.descripcion_api}</p>
        </div>
      )}

      <div className="vehicle-actions">
        <button className="btn btn-primary">
          📝 Crear Anuncio
        </button>
        <button className="btn btn-secondary">
          ❤️ Agregar a Favoritos
        </button>
        <button className="btn btn-secondary">
          📊 Ver Estadísticas
        </button>
      </div>
    </div>
  );
};

export default PlateSearch;
