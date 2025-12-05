import React, { useState } from 'react';
import { useAuth } from './AuthProvider';
import { LoginButton } from './AuthComponents';
import { API_ENDPOINTS } from '../config/api';
import './PlateSearch.css';

const PlateSearch = () => {
  const { user, isAuthenticated, getIdTokenClaims } = useAuth();
  const [plateNumber, setPlateNumber] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [searchResult, setSearchResult] = useState(null);
  const [error, setError] = useState(null);

  const validatePlate = (plate) => {
    const plateRegex = /^[A-Z0-9]{6,7}$/;
    return plateRegex.test(plate.toUpperCase());
  };

  // Datos de prueba para desarrollo
  const getMockVehicleData = (plate) => {
    const mockData = {
      'ABC123': {
        marca: 'Toyota',
        modelo: 'Corolla',
        anio_registro_api: '2020',
        placa: 'ABC123',
        vin: 'JT2BF28K304012345',
        uso: 'Particular',
        propietario: 'Juan Pérez',
        fecha_registro_api: '2020-03-15',
        descripcion_api: 'Vehículo en excelente estado, mantenimiento al día. Ideal para uso familiar.',
        image_url_api: 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?w=400'
      },
      'T3V213': {
        marca: 'Nissan',
        modelo: 'Sentra',
        anio_registro_api: '2018',
        placa: 'T3V213',
        vin: '1N4AL3AP8JC123456',
        uso: 'Particular',
        propietario: 'María González',
        fecha_registro_api: '2018-07-22',
        descripcion_api: 'Sedán compacto, económico y confiable. Perfecto para ciudad.',
        image_url_api: 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=400'
      }
    };
    return mockData[plate];
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

      // Verificar si es una placa de prueba
      const mockData = getMockVehicleData(formattedPlate);
      if (mockData) {
        // Usar datos de prueba
        console.log('Usando datos de prueba para:', formattedPlate);
        setTimeout(() => {
          setSearchResult(mockData);
          setIsLoading(false);
        }, 500); // Simular un pequeño delay
        return;
      }

      // Obtener el token de autenticación
      let authHeader = {};
      if (getIdTokenClaims) {
        try {
          const claims = await getIdTokenClaims();
          if (claims && claims.__raw) {
            authHeader['Authorization'] = `Bearer ${claims.__raw}`;
          }
        } catch (tokenError) {
          console.warn('No se pudo obtener el token:', tokenError);
        }
      }

      const response = await fetch(API_ENDPOINTS.PLATE_SEARCH, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authHeader
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
      
      console.log('Respuesta del servidor:', data);
      console.log('Status OK:', response.ok);
      console.log('Status code:', response.status);

      if (!response.ok) {
        // El backend puede devolver 'error' o 'message'
        const errorMessage = data.error || data.message || 'Error al buscar la placa';
        console.error('Error en respuesta:', errorMessage);
        throw new Error(errorMessage);
      }

      // Verificar si la respuesta tiene éxito
      if (data.success && data.vehicle) {
        console.log('Datos del vehículo recibidos:', data.vehicle);
        setSearchResult(data.vehicle);
      } else if (data.vehicle) {
        // Si hay vehicle aunque no tenga success, intentar mostrarlo
        console.log('Datos del vehículo recibidos (sin success):', data.vehicle);
        setSearchResult(data.vehicle);
      } else if (data.error) {
        console.error('Error en datos:', data.error);
        throw new Error(data.error);
      } else {
        console.error('Respuesta sin datos válidos:', data);
        throw new Error('No se pudo obtener información de la placa');
      }
      
    } catch (err) {
      setError(err.message);
      setIsLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="plate-search-login-required">
        <div className="plate-search-login-card">
          <span className="plate-search-lock-icon">🔒</span>
          <h3>Iniciar Sesión Requerido</h3>
          <p>Debes iniciar sesión para buscar placas de vehículos.</p>
          <div className="plate-search-login-button-wrapper">
            <LoginButton />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="plate-search-container">
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
