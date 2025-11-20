import React, { useState, useRef, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import './AuthComponents.css';

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();

  const handleLogin = () => {
    loginWithRedirect({
      authorizationParams: {
        prompt: 'login',
        screen_hint: 'signup',
        // No usar audience si no hay un API configurado
        // audience: 'q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b',
        scope: 'openid profile email offline_access' // offline_access para refresh tokens
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
  const { user, isAuthenticated, isLoading, getUser } = useAuth0();
  const [userProfile, setUserProfile] = useState(user);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef(null);
  
  // Obtener perfil completo del usuario desde Auth0
  useEffect(() => {
    const fetchUserProfile = async () => {
      if (isAuthenticated && getUser) {
        try {
          const fullUser = await getUser();
          console.log('Perfil completo del usuario:', fullUser);
          setUserProfile(fullUser);
        } catch (error) {
          console.error('Error obteniendo perfil completo:', error);
          // Usar el usuario básico si falla
          setUserProfile(user);
        }
      } else {
        setUserProfile(user);
      }
    };
    
    fetchUserProfile();
  }, [isAuthenticated, getUser, user]);
  
  // Debug: ver qué información del usuario tenemos
  useEffect(() => {
    if (userProfile) {
      console.log('Información del usuario (actualizada):', userProfile);
      console.log('Imagen de perfil:', userProfile.picture);
      console.log('Nombre:', userProfile.name);
      console.log('Email:', userProfile.email);
    }
  }, [userProfile]);

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
        {userProfile?.picture ? (
          <img 
            src={userProfile.picture} 
            alt={userProfile.name || 'Usuario'}
            className="profile-avatar"
            onError={(e) => {
              console.error('Error cargando imagen de perfil:', userProfile.picture);
              e.target.style.display = 'none';
            }}
          />
        ) : (
          <div className="profile-avatar-placeholder">
            {userProfile?.name ? userProfile.name.charAt(0).toUpperCase() : userProfile?.email ? userProfile.email.charAt(0).toUpperCase() : 'U'}
          </div>
        )}
        <span className="profile-name">{userProfile?.name || userProfile?.email || 'Usuario'}</span>
        <span className="profile-arrow">▼</span>
      </div>
      
      {isMenuOpen && (
        <div className="profile-dropdown">
          <div className="profile-dropdown-header">
            {userProfile?.picture ? (
              <img 
                src={userProfile.picture} 
                alt={userProfile.name || 'Usuario'}
                className="profile-dropdown-avatar"
                onError={(e) => {
                  console.error('Error cargando imagen de perfil en dropdown:', userProfile.picture);
                  e.target.style.display = 'none';
                }}
              />
            ) : (
              <div className="profile-dropdown-avatar-placeholder">
                {userProfile?.name ? userProfile.name.charAt(0).toUpperCase() : userProfile?.email ? userProfile.email.charAt(0).toUpperCase() : 'U'}
              </div>
            )}
            <div className="profile-dropdown-name">
              {userProfile?.name || 'Usuario'}
            </div>
            <div className="profile-dropdown-email">
              {userProfile?.email || ''}
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
