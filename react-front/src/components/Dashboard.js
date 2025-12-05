import React, { useEffect, useState } from 'react';
import { useAuth } from './AuthProvider';
import { useNavigate, useLocation } from 'react-router-dom';
import { LoginButton, Profile } from './AuthComponents';
import { NotificationDropdown } from './Notificaciones';
import PlateSearch from './PlateSearch';
import PublicarAuto from './PublicarAuto';
import ListaAnuncios from './ListaAnuncios';
import AnunciosPreview from './AnunciosPreview';
import Contacto from './Contacto';
import ChatIAFloating from './ChatIAFloating';
import { authService, setupAuthInterceptor } from '../services/apiService';
import './Dashboard.css';

const Dashboard = () => {
  const { isAuthenticated, isLoading, user, getIdTokenClaims, loginWithRedirect } = useAuth();
  const [activeTab, setActiveTab] = useState(null); // null = mostrar hero, 'anuncios', 'buscar', 'publicar', 'contacto' = mostrar solo esa sección
  const [portadaImage, setPortadaImage] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

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

  // Verificar si hay parámetros en la URL para activar una sección específica
  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const tab = searchParams.get('tab');
    if (tab && ['anuncios', 'buscar', 'publicar', 'contacto'].includes(tab)) {
      setActiveTab(tab);
    } else {
      setActiveTab(null);
    }
  }, [location.search]);

  // Sincronizar usuario con Django cuando se autentica
  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      
      // Esperar un momento para que el interceptor esté configurado
      // Llamar automáticamente al endpoint de profile para crear el usuario en Django
      const syncUser = async () => {
        try {
          await new Promise(resolve => setTimeout(resolve, 500)); // Esperar 500ms
          await authService.getUserProfile();
        } catch (error) {
          // Intentar una vez más después de 2 segundos
          setTimeout(async () => {
            try {
              await authService.getUserProfile();
            } catch (retryError) {
              console.error('Error al sincronizar usuario con Django:', retryError);
            }
          }, 2000);
        }
      };
      
      syncUser();
    }
  }, [isAuthenticated, getIdTokenClaims]);

  const handleSearch = () => {
    // Navegar a la página principal con la pestaña de buscar placa activa
    // Si no está autenticado, PlateSearch mostrará el mensaje de login requerido
    navigate('/?tab=buscar');
    setActiveTab('buscar');
    // Hacer scroll suave hacia la sección de contenido
    setTimeout(() => {
      const contentSection = document.querySelector('.content-section');
      if (contentSection) {
        contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  };

  const handlePublicar = () => {
    if (isAuthenticated) {
      // Si está autenticado, navegar a la página principal con la pestaña de publicar activa
      navigate('/?tab=publicar');
      setActiveTab('publicar');
      // Hacer scroll suave hacia la sección de contenido
      setTimeout(() => {
        const contentSection = document.querySelector('.content-section');
        if (contentSection) {
          contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }, 100);
    } else {
      // Si no está autenticado, redirigir al login
      loginWithRedirect({
        authorizationParams: {
          prompt: 'login',
          screen_hint: 'signup',
          scope: 'openid profile email offline_access'
        },
        appState: {
          returnTo: '/?tab=publicar'
        }
      });
    }
  };

  const handleTipoVehiculoClick = (tipoVehiculo) => {
    navigate(`/anuncios?tipo=${encodeURIComponent(tipoVehiculo)}`);
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
            <a 
              href="#inicio" 
              className="nav-link"
              onClick={(e) => {
                e.preventDefault();
                setActiveTab(null);
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
                  className={`nav-link ${activeTab === 'anuncios' ? 'active' : ''}`}
                  onClick={(e) => {
                    e.preventDefault();
                    setActiveTab('anuncios');
                    navigate('/?tab=anuncios');
                    setTimeout(() => {
                      const contentSection = document.querySelector('.content-section');
                      if (contentSection) {
                        contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                      }
                    }, 100);
                  }}
                >
                  Mis Anuncios
                </a>
                <a 
                  href="#conversaciones" 
                  className="nav-link"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/conversaciones');
                  }}
                >
                  Conversaciones
                </a>
                <a 
                  href="#buscar-placa" 
                  className={`nav-link ${activeTab === 'buscar' ? 'active' : ''}`}
                  onClick={(e) => {
                    e.preventDefault();
                    setActiveTab('buscar');
                    navigate('/?tab=buscar');
                    setTimeout(() => {
                      const contentSection = document.querySelector('.content-section');
                      if (contentSection) {
                        contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                      }
                    }, 100);
                  }}
                >
                  Buscar Placa
                </a>
                <a 
                  href="#publicar" 
                  className={`nav-link ${activeTab === 'publicar' ? 'active' : ''}`}
                  onClick={(e) => {
                    e.preventDefault();
                    setActiveTab('publicar');
                    navigate('/?tab=publicar');
                    setTimeout(() => {
                      const contentSection = document.querySelector('.content-section');
                      if (contentSection) {
                        contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                      }
                    }, 100);
                  }}
                >
                  Publicar Auto
                </a>
              </>
            ) : (
              <a 
                href="#buscar" 
                className="nav-link"
                onClick={(e) => {
                  e.preventDefault();
                  setActiveTab('buscar');
                  navigate('/?tab=buscar');
                  setTimeout(() => {
                    const contentSection = document.querySelector('.content-section');
                    if (contentSection) {
                      contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    }
                  }, 100);
                }}
              >
                Buscar Autos
              </a>
            )}
            <a 
              href="#contacto" 
              className={`nav-link ${activeTab === 'contacto' ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                setActiveTab('contacto');
                navigate('/?tab=contacto');
                setTimeout(() => {
                  const contentSection = document.querySelector('.content-section');
                  if (contentSection) {
                    contentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                  }
                }, 100);
              }}
            >
              Contacto
            </a>
          </nav>

          <div className="header-right">
            <div className="header-actions">
              {isAuthenticated ? (
                <>
                  <NotificationDropdown />
                  <Profile />
                </>
              ) : (
                <LoginButton />
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section - Solo se muestra si no hay una sección activa */}
      {!activeTab && (
        <main className="hero-section">
          <div className="hero-content">
            <h1 className="hero-title">
              El portal más confiable para comprar y vender autos seminuevos y usados en Perú.
            </h1>
            <p className="hero-subtitle">
              En CheckAuto encontrarás una amplia selección de vehículos disponibles en todo el país, con precios competitivos y herramientas exclusivas como nuestro verificador de placas, que te permite confirmar la información del vehículo antes de tomar una decisión. ¿Listo para comprar o vender tu auto en Perú?
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
      )}

      {/* Vehicle Types Section - Solo se muestra si no hay una sección activa */}
      {!activeTab && (
        <section className="vehicle-types-section">
          <div className="vehicle-types-container">
            <h2 className="vehicle-types-title">BUSCAR POR TIPO DE VEHÍCULO</h2>
            <div className="vehicle-types-grid">
            <div className="vehicle-type-card" onClick={() => handleTipoVehiculoClick('Hatchback')}>
              <img 
                src={`${process.env.PUBLIC_URL}/vehiculos/hatchback.png`} 
                alt="Hatchback" 
                className="vehicle-type-image"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <h3 className="vehicle-type-name">Hatchback</h3>
            </div>
            <div className="vehicle-type-card" onClick={() => handleTipoVehiculoClick('Sedan')}>
              <img 
                src={`${process.env.PUBLIC_URL}/vehiculos/sedan.png`} 
                alt="Sedan" 
                className="vehicle-type-image"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <h3 className="vehicle-type-name">Sedan</h3>
            </div>
            <div className="vehicle-type-card" onClick={() => handleTipoVehiculoClick('Coupé')}>
              <img 
                src={`${process.env.PUBLIC_URL}/vehiculos/coupe.png`} 
                alt="Coupé" 
                className="vehicle-type-image"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <h3 className="vehicle-type-name">Coupé</h3>
            </div>
            <div className="vehicle-type-card" onClick={() => handleTipoVehiculoClick('SUV')}>
              <img 
                src={`${process.env.PUBLIC_URL}/vehiculos/suv.png`} 
                alt="SUV" 
                className="vehicle-type-image"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <h3 className="vehicle-type-name">SUV</h3>
            </div>
            <div className="vehicle-type-card" onClick={() => handleTipoVehiculoClick('Station Wagon')}>
              <img 
                src={`${process.env.PUBLIC_URL}/vehiculos/station-wagon.png`} 
                alt="Station Wagon" 
                className="vehicle-type-image"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <h3 className="vehicle-type-name">Station Wagon</h3>
            </div>
            <div className="vehicle-type-card" onClick={() => handleTipoVehiculoClick('Deportivo')}>
              <img 
                src={`${process.env.PUBLIC_URL}/vehiculos/deportivo.png`} 
                alt="Deportivo" 
                className="vehicle-type-image"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <h3 className="vehicle-type-name">Deportivo</h3>
            </div>
          </div>
        </div>
      </section>
      )}

      {/* Anuncios Preview - Solo se muestra si no hay una sección activa */}
      {!activeTab && (
        <AnunciosPreview limite={6} />
      )}

      {/* Content Section - Solo se muestra si hay una sección activa */}
      {activeTab && (
        <div className={`content-section ${activeTab === 'buscar' ? 'buscar-section' : activeTab === 'publicar' ? 'publicar-section' : activeTab === 'anuncios' ? 'anuncios-section' : activeTab === 'contacto' ? 'contacto-section' : ''}`}>
          {activeTab === 'contacto' ? (
            <Contacto />
          ) : isAuthenticated ? (
            <>
              {activeTab === 'anuncios' && <ListaAnuncios />}
              {activeTab === 'buscar' && <PlateSearch />}
              {activeTab === 'publicar' && <PublicarAuto />}
            </>
          ) : (
            <>
              {activeTab === 'buscar' ? (
                // Mostrar PlateSearch incluso si no está autenticado
                <PlateSearch />
              ) : (
                <div className="dashboard-login-card">
                  <h3>Inicia sesión para continuar</h3>
                  <p>
                    Necesitas iniciar sesión con tu cuenta de TECSUP para acceder a esta sección.
                  </p>
                  <LoginButton />
                </div>
              )}
            </>
          )}
        </div>
      )}


      <footer className="dashboard-footer">
        <p>© 2025 checkAuto. Todos los derechos reservados.</p>
      </footer>

      {/* Burbuja flotante del Chat IA */}
      <ChatIAFloating />
    </div>
  );
};

export default Dashboard;
