import React, { useState, useRef, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import './AuthComponents.css';

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();

  const handleLogin = () => {
    loginWithRedirect({
      authorizationParams: {
        prompt: 'login',
        screen_hint: 'signup'
      }
    });
  };

  return (
    <button onClick={handleLogin} className="login-button">
      Iniciar Sesión
    </button>
  );
};

const LogoutButton = () => {
  const { logout } = useAuth0();

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
    return <span className="profile-loading">Cargando...</span>;
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="profile-container" ref={menuRef}>
      <div 
        onClick={() => setIsMenuOpen(!isMenuOpen)}
        className={`profile-toggle ${isMenuOpen ? 'active' : ''}`}
      >
        <img 
          src={user.picture} 
          alt={user.name}
          className="profile-avatar"
        />
        <span className="profile-name">{user.name || user.email}</span>
        <span className="profile-arrow">▼</span>
      </div>
      
      {isMenuOpen && (
        <div className="profile-dropdown">
          <div className="profile-dropdown-header">
            <img 
              src={user.picture} 
              alt={user.name}
              className="profile-dropdown-avatar"
            />
            <div className="profile-dropdown-name">
              {user.name}
            </div>
            <div className="profile-dropdown-email">
              {user.email}
            </div>
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
