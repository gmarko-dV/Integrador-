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
  const [portadaImage, setPortadaImage] = useState(null);

  // Cargar imagen de portada
  useEffect(() => {
    const portadaUrl = 'https://images.unsplash.com/photo-1449824913935-59a10b8d2000?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80';
    const img = new Image();
    img.src = portadaUrl;
    img.onload = () => {
      setPortadaImage(portadaUrl);
    };
    img.onerror = () => {
      // Si no existe la imagen, usar el fallback del CSS
      setPortadaImage(null);
    };
  }, []);

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

  const handleSearch = () => {
    setActiveTab('anuncios');
    // Aquí puedes agregar lógica de búsqueda
  };

  const handlePublicar = () => {
    if (isAuthenticated) {
      setActiveTab('publicar');
    } else {
      // Redirigir a login si no está autenticado
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  if (isLoading) {
    return (
      <div className="dashboard-loading">
        <p>Cargando...</p>
      </div>
    );
  }

  // Estilo de fondo dinámico
  const homepageStyle = portadaImage
    ? {
        background: `url(${portadaImage}) center center/cover no-repeat`,
        backgroundAttachment: 'fixed',
        backgroundPosition: 'center',
        backgroundSize: 'cover',
        backgroundRepeat: 'no-repeat'
      }
    : {};

  return (
    <div className="peruautos-homepage" style={homepageStyle}>
      {/* Header */}
      <header className="peruautos-header">
        <div className="header-container">
          <div className="logo-section">
            <h1 className="logo-text">checkAuto</h1>
          </div>

          <nav className="main-nav">
            <a href="#inicio" className="nav-link">Inicio</a>
            <a href="#buscar" className="nav-link">Buscar Autos</a>
            <div className="nav-link dropdown">
              Contacto <span className="dropdown-arrow">▼</span>
            </div>
          </nav>

          <div className="header-right">
            <div className="header-actions">
              {isAuthenticated ? <Profile /> : <LoginButton />}
              <button className="btn-publicar-header" onClick={handlePublicar}>
                PUBLICAR
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            El portal confiable para la compra y venta de autos nuevos y usados en Perú.
          </h1>
          <p className="hero-subtitle">
            En CheckAuto® encontrarás miles de vehículos disponibles en todas las ciudades del país, con precios competitivos y herramientas como nuestro buscador de placas para verificar la información del auto antes de decidir. ¿Listo para comprar o vender tu vehículo en Perú?
          </p>

          {/* Action Buttons */}
          <div className="hero-actions">
            <button className="btn-buscar-autos" onClick={handleSearch}>
              <span className="btn-icon">🔍</span>
              Buscar Autos
            </button>
            <button className="btn-publicar-anuncio" onClick={handlePublicar}>
              Publicar Anuncio
            </button>
          </div>
        </div>
      </main>

      {/* Content Section */}
      <div className="content-section">
        {isAuthenticated ? (
          <div className="authenticated-content">
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
      </div>

      <footer className="dashboard-footer">
        <p>© 2024 checkAuto. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
};

export default Dashboard;
