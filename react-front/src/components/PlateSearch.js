import React, { useState } from 'react';
import { useAuth } from './AuthProvider';
import { API_ENDPOINTS } from '../config/api';
import './PlateSearch.css';

const PlateSearch = () => {
  const { user, isAuthenticated } = useAuth();
  const [plateNumber, setPlateNumber] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [searchResult, setSearchResult] = useState(null);
  const [error, setError] = useState(null);

  const validatePlate = (plate) => {
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
      setError('Formato inválido. Debe tener 6-7 caracteres (ej: ABC123)');
      return;
    }

    setIsLoading(true);
    setError(null);
    setSearchResult(null);

    try {
      if (!isAuthenticated) {
        throw new Error('Debes iniciar sesión para buscar placas');
      }

      const response = await fetch(API_ENDPOINTS.PLATE_SEARCH, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
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
      
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="plate-search-login-required">
        <h3>🔐 Iniciar Sesión Requerido</h3>
        <p>Debes iniciar sesión para buscar placas de vehículos.</p>
        <button 
          onClick={() => window.location.href = '/login'}
          className="plate-search-button"
        >
          Iniciar Sesión
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="plate-search-form">
        <h3>🔍 Búsqueda de Placas</h3>
        
        <form onSubmit={handleSearch}>
          <div className="plate-search-input-group">
            <input
              type="text"
              value={plateNumber}
              onChange={(e) => setPlateNumber(e.target.value.toUpperCase())}
              placeholder="Ej: ABC123, T3V213"
              className="plate-search-input"
              maxLength="7"
              disabled={isLoading}
            />
            <button 
              type="submit" 
              className="plate-search-button"
              disabled={isLoading || !plateNumber.trim()}
            >
              {isLoading ? 'Buscando...' : 'Buscar'}
            </button>
          </div>
          
          {plateNumber && (
            <button 
              type="button" 
              onClick={() => {
                setPlateNumber('');
                setSearchResult(null);
                setError(null);
              }}
              className="plate-search-clear-button"
            >
              Limpiar
            </button>
          )}
        </form>

        {error && (
          <div className="plate-search-error">
            <strong>Error:</strong> {error}
          </div>
        )}
      </div>

      {searchResult && (
        <div className="plate-search-result">
          <h3>📋 Información del Vehículo</h3>
          <VehicleDetails vehicle={searchResult} />
        </div>
      )}
    </div>
  );
};

// Componente para mostrar los detalles del vehículo
const VehicleDetails = ({ vehicle }) => {
  return (
    <div>
      <div className="vehicle-details-container">
        {vehicle.image_url_api ? (
          <img 
            src={vehicle.image_url_api} 
            alt={`${vehicle.marca} ${vehicle.modelo}`}
            className="vehicle-image"
          />
        ) : (
          <div className="vehicle-image-container">
            <span className="vehicle-icon">Auto</span>
          </div>
        )}
        <div className="vehicle-basic-info">
          <h4>{vehicle.marca} {vehicle.modelo}</h4>
          <p className="vehicle-year">Año: {vehicle.anio_registro_api}</p>
          <p>Placa: <strong>{vehicle.placa}</strong></p>
        </div>
      </div>

      <div className="vehicle-specs-grid">
        <div className="vehicle-spec-item">
          <strong>VIN:</strong>
          <p>{vehicle.vin || 'No disponible'}</p>
        </div>
        <div className="vehicle-spec-item">
          <strong>Uso:</strong>
          <p>{vehicle.uso || 'No disponible'}</p>
        </div>
        <div className="vehicle-spec-item">
          <strong>Propietario:</strong>
          <p>{vehicle.propietario || 'No disponible'}</p>
        </div>
        <div className="vehicle-spec-item">
          <strong>Fecha de Registro:</strong>
          <p>{vehicle.fecha_registro_api || 'No disponible'}</p>
        </div>
        {vehicle.delivery_point && (
          <div className="vehicle-spec-item">
            <strong>Punto de Entrega:</strong>
            <p>{vehicle.delivery_point}</p>
          </div>
        )}
      </div>

      {vehicle.descripcion_api && (
        <div className="vehicle-description">
          <strong>Descripción:</strong>
          <p>{vehicle.descripcion_api}</p>
        </div>
      )}
    </div>
  );
};

export default PlateSearch;
