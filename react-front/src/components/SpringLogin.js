import React from 'react';

const SpringLogin = () => {
  const handleLogin = () => {
    // Redirigir a la página de login de Spring Boot
    window.location.href = 'http://localhost:8080/oauth2/authorization/auth0';
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Iniciar Sesión</h2>
        <p>Necesitas iniciar sesión para buscar placas de vehículos.</p>
        <button onClick={handleLogin} className="login-button">
          Iniciar Sesión con Auth0
        </button>
      </div>
    </div>
  );
};

export default SpringLogin;
