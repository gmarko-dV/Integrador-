import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { LoginButton, Profile } from './AuthComponents';
import BackendInfo from './BackendInfo';
import PlateSearch from './PlateSearch';
import PublicarAuto from './PublicarAuto';
import ListaAnuncios from './ListaAnuncios';
import { authService, setupAuthInterceptor } from '../services/apiService';
import './Dashboard.css';

const Dashboard = () => {
  const { isAuthenticated, isLoading, user, getIdTokenClaims } = useAuth0();
  const [activeTab, setActiveTab] = useState('anuncios');

  // Sincronizar usuario con Django cuando se autentica
  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      
      // Esperar un momento para que el interceptor esté configurado
      // Llamar automáticamente al endpoint de profile para crear el usuario en Django
      const syncUser = async () => {
        try {
          await new Promise(resolve => setTimeout(resolve, 500)); // Esperar 500ms
          const profile = await authService.getUserProfile();
          console.log('Usuario sincronizado con Django:', profile);
        } catch (error) {
          console.error('Error al sincronizar usuario con Django:', error);
          // Intentar una vez más después de 2 segundos
          setTimeout(async () => {
            try {
              const profile = await authService.getUserProfile();
              console.log('Usuario sincronizado con Django (segundo intento):', profile);
            } catch (retryError) {
              console.error('Error al sincronizar usuario con Django (segundo intento):', retryError);
            }
          }, 2000);
        }
      };
      
      syncUser();
    }
  }, [isAuthenticated, getIdTokenClaims]);

  if (isLoading) {
    return (
      <div className="dashboard-loading">
        <p>Cargando...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>CHECKAUTO</h1>
        <div>
          {isAuthenticated ? <Profile /> : <LoginButton />}
        </div>
      </header>

      <main className="dashboard-main">
        <div className="dashboard-hero">
          <h2>Portal de Consulta de Vehículos en Perú</h2>
          <p>Consulta información de vehículos registrados en Perú</p>
        </div>

        {isAuthenticated ? (
          <div>
            <div className="dashboard-welcome">
              <p>
                Bienvenido, <strong>{user?.name || user?.email}</strong>
              </p>
            </div>
            <div className="dashboard-tabs">
              <button
                className={`tab-button ${activeTab === 'anuncios' ? 'active' : ''}`}
                onClick={() => setActiveTab('anuncios')}
              >
                🚗 Ver Anuncios
              </button>
              <button
                className={`tab-button ${activeTab === 'buscar' ? 'active' : ''}`}
                onClick={() => setActiveTab('buscar')}
              >
                🔍 Buscar Placa
              </button>
              <button
                className={`tab-button ${activeTab === 'publicar' ? 'active' : ''}`}
                onClick={() => setActiveTab('publicar')}
              >
                ➕ Publicar Auto
              </button>
            </div>
            {activeTab === 'anuncios' && <ListaAnuncios />}
            {activeTab === 'buscar' && <PlateSearch />}
            {activeTab === 'publicar' && <PublicarAuto />}
          </div>
        ) : (
          <>
            <ListaAnuncios />
            <div className="dashboard-login-card">
              <h3>Inicia sesión para continuar</h3>
              <p>
                Necesitas iniciar sesión con tu cuenta de TECSUP para buscar placas de vehículos o publicar tu auto.
              </p>
              <LoginButton />
            </div>

            <div className="dashboard-features">
              <div className="dashboard-feature-card">
                <div className="dashboard-feature-icon">🔍</div>
                <h4>Consulta Rápida</h4>
                <p>Busca información de vehículos en segundos</p>
              </div>
              <div className="dashboard-feature-card">
                <div className="dashboard-feature-icon">📊</div>
                <h4>Datos Oficiales</h4>
                <p>Información verificada del gobierno</p>
              </div>
              <div className="dashboard-feature-card">
                <div className="dashboard-feature-icon">🛡️</div>
                <h4>Seguro</h4>
                <p>Tu información está protegida</p>
              </div>
            </div>

            <div className="dashboard-services">
              <h3>Estado de los Servicios</h3>
              <BackendInfo />
            </div>
          </>
        )}
      </main>

      <footer className="dashboard-footer">
        <p>© 2024 CHECKAUTO. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
};

export default Dashboard;
