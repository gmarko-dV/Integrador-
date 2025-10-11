import React from 'react';
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

  if (isLoading) {
    return <div className="loading">Cargando...</div>;
  }

  if (!isAuthenticated) {
    return <div>No estás autenticado</div>;
  }

  return (
    <div className="user-profile">
      <div className="user-info">
        <h3>{user.name}</h3>
        <p>{user.email}</p>
      </div>
      <img 
        className="user-avatar" 
        src={user.picture} 
        alt={user.name} 
      />
      <LogoutButton />
    </div>
  );
};

export { LoginButton, LogoutButton, Profile };
