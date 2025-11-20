import React from 'react';
import { Auth0Provider } from '@auth0/auth0-react';

const AuthProvider = ({ children }) => {
  // Dejar que el componente Callback maneje toda la lógica de redirección
  // No usar onRedirectCallback para evitar conflictos
  return (
    <Auth0Provider
      domain="dev-gmarko.us.auth0.com"
      clientId="q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b"
      authorizationParams={{
        redirect_uri: window.location.origin + '/callback',
        // No usar audience si no hay un API configurado en Auth0
        // audience: 'q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b',
        scope: 'openid profile email offline_access' // offline_access para refresh tokens
      }}
      useRefreshTokens={true}
      cacheLocation="localstorage"
    >
      {children}
    </Auth0Provider>
  );
};

export default AuthProvider;
