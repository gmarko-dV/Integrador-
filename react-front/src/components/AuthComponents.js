import React, { useState, useRef, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/apiService';
import { setupAuthInterceptor } from '../services/apiService';
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
  const { user, isAuthenticated, isLoading, getUser, getIdTokenClaims } = useAuth0();
  const [userProfile, setUserProfile] = useState(user);
  const [djangoProfile, setDjangoProfile] = useState(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef(null);
  const navigate = useNavigate();
  
  // Obtener perfil completo del usuario desde Auth0 y Django
  useEffect(() => {
    const fetchUserProfile = async () => {
      if (isAuthenticated) {
        try {
          // PRIMERO obtener perfil desde Django (antes que Auth0) para tener el nombre actualizado
          if (getIdTokenClaims) {
            setupAuthInterceptor(getIdTokenClaims);
            try {
              // Esperar un momento para asegurar que el interceptor esté configurado
              await new Promise(resolve => setTimeout(resolve, 200));
              
              const djangoUser = await authService.getUserProfile();
              
              // SIEMPRE guardar el perfil de Django, incluso si no tiene nombre
              if (djangoUser && typeof djangoUser === 'object' && djangoUser !== null) {
                setDjangoProfile(djangoUser);
                
                // Obtener perfil de Auth0 después (si está disponible)
                let fullUser = user;
                if (getUser) {
                  try {
                    fullUser = await getUser();
                  } catch (auth0Error) {
                    fullUser = user;
                  }
                }
                
                // SIEMPRE priorizar el nombre de Django si está disponible
                // Verificar tanto first_name como last_name (y también Last_name por si acaso)
                const firstName = djangoUser.first_name || djangoUser.First_name || '';
                const lastName = djangoUser.last_name || djangoUser.Last_name || '';
                
                if (firstName || lastName) {
                  const fullName = `${firstName || ''} ${lastName || ''}`.trim();
                  if (fullName) {
                    // Usar el nombre de Django - esto tiene PRIORIDAD ABSOLUTA
                    setUserProfile({
                      ...fullUser,
                      name: fullName
                    });
                    return; // Salir temprano para evitar sobrescribir
                  }
                }
                
                // Si no hay nombre en Django, usar Auth0 para el nombre
                setUserProfile(fullUser || user);
              } else {
                setDjangoProfile(null);
                setUserProfile(user);
              }
            } catch (djangoError) {
              console.error('Error obteniendo perfil de Django:', djangoError);
              // Si falla Django, usar solo Auth0
              setDjangoProfile(null);
              setUserProfile(user);
            }
          } else {
            // Si no hay getIdTokenClaims, usar solo Auth0
            setDjangoProfile(null);
            setUserProfile(user);
          }
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
    
    // Escuchar eventos de actualización de perfil
    const handleProfileUpdate = async (event) => {
      // PRIMERO actualizar inmediatamente desde el evento (sin esperar)
      if (event.detail && event.detail.fullName) {
        setUserProfile(prev => {
          if (prev?.name !== event.detail.fullName) {
            return {
              ...prev,
              name: event.detail.fullName
            };
          }
          return prev;
        });
      }
      
      // LUEGO recargar desde Django para obtener el nombre actualizado y sincronizar
      if (isAuthenticated && getIdTokenClaims) {
        try {
          setupAuthInterceptor(getIdTokenClaims);
          // Esperar un momento para asegurar que el backend haya guardado
          await new Promise(resolve => setTimeout(resolve, 500));
          const djangoUser = await authService.getUserProfile();
          
          // Actualizar el perfil de Django - esto disparará el useEffect que actualiza el nombre
          setDjangoProfile(djangoUser);
          
          if (djangoUser.first_name || djangoUser.last_name) {
            const fullName = `${djangoUser.first_name || ''} ${djangoUser.last_name || ''}`.trim();
            if (fullName) {
              // Actualizar desde Django para asegurar sincronización
              setUserProfile(prev => {
                if (prev?.name !== fullName) {
                  return {
                    ...prev,
                    name: fullName
                  };
                }
                return prev;
              });
            }
          }
        } catch (error) {
          console.error('Error recargando perfil desde Django:', error);
          // Si falla, el nombre ya se actualizó desde el evento, así que está bien
        }
      }
    };
    
    // Escuchar eventos tanto en window como en document
    window.addEventListener('profileUpdated', handleProfileUpdate);
    document.addEventListener('profileUpdated', handleProfileUpdate);
    
    return () => {
      window.removeEventListener('profileUpdated', handleProfileUpdate);
      document.removeEventListener('profileUpdated', handleProfileUpdate);
    };
    // Solo ejecutar cuando cambie isAuthenticated o getIdTokenClaims, no cuando cambie user
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, getIdTokenClaims]);
  
  // Actualizar el nombre cuando cambie el perfil de Django - ESTE TIENE MÁXIMA PRIORIDAD
  // Este useEffect se ejecuta después y siempre sobrescribe el nombre con el de Django
  useEffect(() => {
    if (djangoProfile) {
      // Manejar tanto last_name como Last_name (por si hay inconsistencia)
      const firstName = djangoProfile.first_name || djangoProfile.First_name || '';
      const lastName = djangoProfile.last_name || djangoProfile.Last_name || '';
      
      if (firstName || lastName) {
        const fullName = `${firstName || ''} ${lastName || ''}`.trim();
        if (fullName) {
          // SIEMPRE actualizar el nombre desde Django, incluso si ya existe uno
          // Esto asegura que el nombre de Django siempre tenga prioridad
          setUserProfile(prev => {
            const currentName = prev?.name || '';
            // Solo actualizar si el nombre es diferente para evitar loops infinitos
            if (currentName !== fullName) {
              return {
                ...prev,
                name: fullName
              };
            }
            return prev;
          });
        }
      }
    }
  }, [djangoProfile]);

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
