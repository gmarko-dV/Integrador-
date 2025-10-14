import React, { useState, useEffect, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();

  return (
    <button 
      onClick={() => loginWithRedirect()}
      className="btn btn-primary"
    >
      Iniciar Sesión
    </button>
  );
};

const LogoutButton = () => {
  const { logout } = useAuth0();

  return (
    <button 
      onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
      className="btn btn-danger"
    >
      Cerrar Sesión
    </button>
  );
};

const Profile = () => {
  const { user, isAuthenticated, isLoading } = useAuth0();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef(null);

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
    return <div className="loading">Cargando...</div>;
  }

  if (!isAuthenticated) {
    return <div>No estás autenticado</div>;
  }

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
  };

  return (
    <div className="user-profile" ref={menuRef}>
      <div className="user-avatar-container" onClick={toggleMenu}>
        <img 
          className="user-avatar" 
          src={user.picture} 
          alt={user.name}
          title="Ver perfil"
        />
        <span className="user-name">{user.name}</span>
        <span className="dropdown-arrow">▼</span>
      </div>
      
      {isMenuOpen && (
        <div className="user-dropdown">
          <div className="dropdown-header">
            <img 
              className="dropdown-avatar" 
              src={user.picture} 
              alt={user.name} 
            />
            <div className="dropdown-user-info">
              <h4>{user.name}</h4>
              <p>{user.email}</p>
            </div>
          </div>
          
          <div className="dropdown-divider"></div>
          
          <div className="dropdown-menu">
            <button className="dropdown-item" onClick={closeMenu}>
              <span className="menu-icon">👤</span>
              Mi Perfil
            </button>
            <button className="dropdown-item" onClick={closeMenu}>
              <span className="menu-icon">⚙️</span>
              Configuración
            </button>
            <button className="dropdown-item" onClick={closeMenu}>
              <span className="menu-icon">📊</span>
              Estadísticas
            </button>
            <button className="dropdown-item" onClick={closeMenu}>
              <span className="menu-icon">❓</span>
              Ayuda
            </button>
          </div>
          
          <div className="dropdown-divider"></div>
          
          <div className="dropdown-menu">
            <LogoutButton />
          </div>
        </div>
      )}
    </div>
  );
};

export { LoginButton, LogoutButton, Profile };
