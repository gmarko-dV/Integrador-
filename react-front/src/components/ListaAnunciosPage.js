import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import { LoginButton, Profile } from './AuthComponents';
import ListaAnuncios from './ListaAnuncios';
import './Dashboard.css';

const ListaAnunciosPage = () => {
  const { isAuthenticated, isLoading } = useAuth0();
  const navigate = useNavigate();

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
            <a 
              href="/" 
              className="nav-link"
              onClick={(e) => {
                e.preventDefault();
                navigate('/');
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
            >
              Inicio
            </a>
            {isAuthenticated ? (
              <>
                <a 
                  href="#anuncios" 
                  className="nav-link"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/?tab=anuncios');
                  }}
                >
                  Mis Anuncios
                </a>
                <a 
                  href="#buscar-placa" 
                  className="nav-link"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/?tab=buscar');
                  }}
                >
                  Buscar Placa
                </a>
                <a 
                  href="#publicar" 
                  className="nav-link"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/?tab=publicar');
                  }}
                >
                  Publicar Auto
                </a>
                <a 
                  href="/chat" 
                  className="nav-link"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/chat');
                  }}
                >
                  Chat IA
                </a>
              </>
            ) : (
              <a 
                href="/anuncios" 
                className="nav-link"
                onClick={(e) => {
                  e.preventDefault();
                  navigate('/anuncios');
                }}
              >
                Buscar Autos
              </a>
            )}
          </nav>

          <div className="header-right">
            <div className="header-actions">
              {isAuthenticated ? <Profile /> : <LoginButton />}
            </div>
          </div>
        </div>
      </header>

      {/* Content Section */}
      <div className="content-section content-section-anuncios-page anuncios-page-white">
        <ListaAnuncios />
      </div>

      <footer className="dashboard-footer">
        <p>© 2025 checkAuto. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
};

export default ListaAnunciosPage;

