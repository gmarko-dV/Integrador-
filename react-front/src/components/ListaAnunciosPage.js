import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { LoginButton, Profile } from './AuthComponents';
import ListaAnuncios from './ListaAnuncios';
import './Dashboard.css';

const ListaAnunciosPage = () => {
  const { isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return (
      <div className="dashboard-loading">
        <p>Cargando...</p>
      </div>
    );
  }

  return (
    <div className="peruautos-homepage">
      {/* Header */}
      <header className="peruautos-header">
        <div className="header-container">
          <div className="logo-section">
            <h1 className="logo-text">checkAuto</h1>
          </div>

          <nav className="main-nav">
            <a href="/" className="nav-link">Inicio</a>
            <a href="/anuncios" className="nav-link">Buscar Autos</a>
            <div className="nav-link dropdown">
              Contacto <span className="dropdown-arrow">▼</span>
            </div>
          </nav>

          <div className="header-right">
            <div className="header-actions">
              {isAuthenticated ? <Profile /> : <LoginButton />}
            </div>
          </div>
        </div>
      </header>

      {/* Content Section */}
      <div className="content-section">
        <ListaAnuncios />
      </div>

      <footer className="dashboard-footer">
        <p>© 2024 checkAuto. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
};

export default ListaAnunciosPage;

