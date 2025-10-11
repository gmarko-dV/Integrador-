import React from 'react';
import { Auth0Provider } from '@auth0/auth0-react';

const AuthProvider = ({ children }) => {
  return (
    <Auth0Provider
      domain="dev-gmarko.us.auth0.com"
      clientId="q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b"
      redirectUri={window.location.origin + '/callback'}
    >
      {children}
    </Auth0Provider>
  );
};

export default AuthProvider;
