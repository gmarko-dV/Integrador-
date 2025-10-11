import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { authService, setupAuthInterceptor } from '../services/apiService';

const BackendInfo = () => {
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  const [backendData, setBackendData] = useState({
    spring: null,
    django: null,
    health: null,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isAuthenticated) {
      setupAuthInterceptor(getAccessTokenSilently);
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  const fetchBackendData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const [springUser, djangoUser, health] = await Promise.all([
        authService.getUserInfo(),
        authService.getUserProfile(),
        authService.healthCheck(),
      ]);

      setBackendData({
        spring: springUser,
        django: djangoUser,
        health: health,
      });
    } catch (err) {
      setError(err.message);
      console.error('Error fetching backend data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="status-info">
        <strong>Nota:</strong> Inicia sesión para ver la información de los backends.
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h3 style={{ margin: 0, fontSize: '20px', fontWeight: '600' }}>
          Información de Backends
        </h3>
        <button
          onClick={fetchBackendData}
          disabled={loading}
          className="btn btn-primary"
          style={{ opacity: loading ? 0.6 : 1 }}
        >
          {loading ? 'Cargando...' : 'Actualizar'}
        </button>
      </div>

      {error && (
        <div className="status-error">
          <strong>Error:</strong> {error}
        </div>
      )}

      <div className="grid grid-2">
        {/* Spring Boot Info */}
        <div className="card">
          <h4 style={{ margin: '0 0 15px 0', fontSize: '18px', fontWeight: '600' }}>
            Spring Boot Backend
          </h4>
          {backendData.spring ? (
            <div className="backend-data">
              <p><strong>ID:</strong> {backendData.spring.id}</p>
              <p><strong>Nombre:</strong> {backendData.spring.name}</p>
              <p><strong>Email:</strong> {backendData.spring.email}</p>
              {backendData.spring.picture && (
                <img 
                  src={backendData.spring.picture} 
                  alt="Profile" 
                  style={{ width: '60px', height: '60px', borderRadius: '50%', marginTop: '10px' }}
                />
              )}
            </div>
          ) : (
            <p style={{ color: '#666', fontStyle: 'italic' }}>No hay datos disponibles</p>
          )}
        </div>

        {/* Django Info */}
        <div className="card">
          <h4 style={{ margin: '0 0 15px 0', fontSize: '18px', fontWeight: '600' }}>
            Django Backend
          </h4>
          {backendData.django ? (
            <div className="backend-data">
              <p><strong>ID:</strong> {backendData.django.id}</p>
              <p><strong>Username:</strong> {backendData.django.username}</p>
              <p><strong>Email:</strong> {backendData.django.email}</p>
              <p><strong>Nombre:</strong> {backendData.django.first_name} {backendData.django.last_name}</p>
            </div>
          ) : (
            <p style={{ color: '#666', fontStyle: 'italic' }}>No hay datos disponibles</p>
          )}
        </div>
      </div>

      {/* Health Status */}
      {backendData.health && (
        <div className="card" style={{ marginTop: '20px' }}>
          <h4 style={{ margin: '0 0 15px 0', fontSize: '18px', fontWeight: '600' }}>
            Estado de los Servicios
          </h4>
          <div className="grid grid-2">
            <div className="health-status">
              <div className={`health-dot ${backendData.health.spring?.status === 'OK' ? 'green' : 'red'}`}></div>
              <span>Spring Boot: {backendData.health.spring?.status || 'Desconocido'}</span>
            </div>
            <div className="health-status">
              <div className={`health-dot ${backendData.health.django?.status === 'OK' ? 'green' : 'red'}`}></div>
              <span>Django: {backendData.health.django?.status || 'Desconocido'}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BackendInfo;
