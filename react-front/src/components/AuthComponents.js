import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from './AuthProvider';
import { useNavigate } from 'react-router-dom';
import './AuthComponents.css';

const LoginButton = () => {
  const navigate = useNavigate();

  const handleLogin = () => {
    // Navegar a la página de login
    navigate('/login');
  };

  return (
    <button onClick={handleLogin} className="login-button">
      Iniciar Sesión
    </button>
  );
};

const LogoutButton = () => {
  const { logout } = useAuth();

  return (
    <button 
      onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
      className="logout-button"
    >
      Cerrar Sesión
    </button>
  );
};

const Profile = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false);
      }
    };

    if (isMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isMenuOpen]);

  if (isLoading) {
    return <span className="profile-loading">Cargando...</span>;
  }

  if (!isAuthenticated || !user) {
    return null;
  }

  // Obtener inicial para el avatar
  const getInitial = () => {
    if (user?.name) return user.name.charAt(0).toUpperCase();
    if (user?.email) return user.email.charAt(0).toUpperCase();
    return 'U';
  };

  // Obtener nombre para mostrar
  const displayName = user?.name || user?.email?.split('@')[0] || 'Usuario';

  return (
    <div className="profile-container" ref={menuRef}>
      <div 
        onClick={() => setIsMenuOpen(!isMenuOpen)}
        className={`profile-toggle ${isMenuOpen ? 'active' : ''}`}
      >
        {user?.picture ? (
          <img 
            src={user.picture} 
            alt={displayName}
            className="profile-avatar"
            onError={(e) => {
              e.target.style.display = 'none';
            }}
          />
        ) : (
          <div className="profile-avatar-placeholder">
            {getInitial()}
          </div>
        )}
        <span className="profile-name">{displayName}</span>
        <span className="profile-arrow">▼</span>
      </div>
        
      {isMenuOpen && (
        <div className="profile-dropdown">
          <div className="profile-dropdown-header">
            {user?.picture ? (
              <img 
                src={user.picture} 
                alt={displayName}
                className="profile-dropdown-avatar"
                onError={(e) => {
                  e.target.style.display = 'none';
                }}
              />
            ) : (
              <div className="profile-dropdown-avatar-placeholder">
                {getInitial()}
              </div>
            )}
            <div className="profile-dropdown-name">
              {displayName}
            </div>
            <div className="profile-dropdown-email">
              {user?.email || ''}
            </div>
          </div>
          
          <div className="profile-dropdown-divider">
            <button 
              className="profile-settings-button"
              onClick={() => {
                setIsMenuOpen(false);
                navigate('/configuracion');
              }}
            >
              ⚙️ Configuración
            </button>
          </div>
          
          <div className="profile-dropdown-divider">
            <LogoutButton />
          </div>
        </div>
      )}
    </div>
  );
};

export { LoginButton, LogoutButton, Profile };
