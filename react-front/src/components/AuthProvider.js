import React from 'react';
import { Auth0Provider } from '@auth0/auth0-react';

const AuthProvider = ({ children }) => {
  // Dejar que el componente Callback maneje toda la lógica de redirección
  // No usar onRedirectCallback para evitar conflictos
  return (
    <Auth0Provider
      domain="dev-gmarko.us.auth0.com"
      clientId="q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b"
      redirectUri={window.location.origin + '/callback'}
      useRefreshTokens={true}
      cacheLocation="localstorage"
      audience="q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b" // API Identifier - necesario para obtener access tokens
    >
      {children}
    </Auth0Provider>
  );
};

export default AuthProvider;
