import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { LoginButton, Profile } from './AuthComponents';
import BackendInfo from './BackendInfo';
import PlateSearch from './PlateSearch';

const Dashboard = () => {
  const { isAuthenticated, isLoading, user } = useAuth0();

  if (isLoading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  return (
    <div className="checkauto-homepage">
      {/* Header/Navigation */}
      <header className="checkauto-header">
        <div className="header-container">
          <div className="logo-section">
            <div className="logo-icon">
              <div className="car-icon"></div>
            </div>
            <h1 className="logo-text">CHECKAUTO<span className="tm">™</span></h1>
          </div>
          
          <nav className="main-nav">
            <a href="#inicio" className="nav-link">Inicio</a>
            <a href="#buscar" className="nav-link">Buscar Placas</a>
            <a href="#ventas" className="nav-link">Vender Auto</a>
            <a href="#comprar" className="nav-link">Comprar Auto</a>
            <a href="#contacto" className="nav-link dropdown">
              Contacto <span className="dropdown-arrow">▼</span>
            </a>
          </nav>

          <div className="header-social">
            <div className="social-item">
              <span className="social-icon instagram"></span>
              <span className="social-text">checkauto.pe</span>
            </div>
            <div className="social-item">
              <span className="social-icon tiktok"></span>
              <span className="social-text">@checkauto.pe</span>
            </div>
          </div>

          <div className="header-auth">
            {isAuthenticated ? (
              <Profile />
            ) : (
              <LoginButton />
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="checkauto-main">
        <div className="hero-section">
          <div className="hero-content">
            <h1 className="hero-title">
              El Portal Oficial de Compra, Venta y Consulta de Vehículos en Perú
            </h1>
            <p className="hero-subtitle">
              En CHECKAUTO® encontrarás miles de autos nuevos y usados a lo largo de todas las ciudades y regiones del Perú. 
              ¿Estás listo para comprar, vender o verificar la información de tu vehículo?
            </p>
            
            {isAuthenticated && (
              <div className="authenticated-content">
                <div className="welcome-message">
                  <h2>¡Bienvenido, {user.name}!</h2>
                  <p>Ahora puedes acceder a todas las funcionalidades de compra, venta y consulta de vehículos.</p>
                </div>
                <PlateSearch />
              </div>
            )}
          </div>
        </div>

        {/* Features Section */}
        <section className="features-section">
          <div className="features-container">
            <h2 className="features-title">¿Por qué elegir CHECKAUTO?</h2>
            <div className="features-list">
              <div className="feature-item-simple">
                <span className="feature-icon">🔍</span>
                <div className="feature-content">
                  <h3>Consulta Instantánea</h3>
                  <p>Obtén información completa de cualquier vehículo registrado en el Perú en segundos.</p>
                </div>
              </div>
              <div className="feature-item-simple">
                <span className="feature-icon">💰</span>
                <div className="feature-content">
                  <h3>Compra y Venta</h3>
                  <p>Miles de autos nuevos y usados disponibles en todas las ciudades del Perú.</p>
                </div>
              </div>
              <div className="feature-item-simple">
                <span className="feature-icon">📊</span>
                <div className="feature-content">
                  <h3>Datos Oficiales</h3>
                  <p>Información verificada directamente desde las bases de datos oficiales del gobierno.</p>
                </div>
              </div>
              <div className="feature-item-simple">
                <span className="feature-icon">🛡️</span>
                <div className="feature-content">
                  <h3>Seguro y Confidencial</h3>
                  <p>Tu información está protegida con los más altos estándares de seguridad.</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {!isAuthenticated && (
          <section className="status-section">
            <div className="status-container">
              <h3>Estado de los Servicios</h3>
              <BackendInfo />
            </div>
          </section>
        )}
      </main>

      <footer className="checkauto-footer">
        <div className="footer-container">
          <div className="footer-content">
            <div className="footer-logo">
              <div className="car-icon"></div>
              <span>CHECKAUTO™</span>
            </div>
            <div className="footer-links">
              <a href="#privacy">Privacidad</a>
              <a href="#terms">Términos</a>
              <a href="#help">Ayuda</a>
              <a href="#contact">Contacto</a>
            </div>
            <div className="footer-social">
              <span>checkauto.pe</span>
              <span>@checkauto.pe</span>
            </div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2024 CHECKAUTO. Todos los derechos reservados.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Dashboard;