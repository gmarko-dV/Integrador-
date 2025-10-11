import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { LoginButton, Profile } from './AuthComponents';
import BackendInfo from './BackendInfo';

const Dashboard = () => {
  const { isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <nav className="navbar">
        <div className="container">
          <div className="navbar-content">
            <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 'bold' }}>
              Mi App con Auth0
            </h1>
            {isAuthenticated ? (
              <Profile />
            ) : (
              <LoginButton />
            )}
          </div>
        </div>
      </nav>

      <main className="container" style={{ padding: '40px 20px' }}>
        <div className="card">
          <h2 style={{ marginTop: 0, fontSize: '28px', color: '#374151' }}>
            Dashboard Principal
          </h2>
          {isAuthenticated ? (
            <div className="status-success">
              <strong>¡Autenticación exitosa!</strong> Puedes acceder a todas las funcionalidades.
            </div>
          ) : (
            <div className="status-info">
              <strong>Bienvenido!</strong> Inicia sesión para acceder a tu dashboard.
            </div>
          )}

          {/* Información de Backends */}
          <div className="backend-info">
            <BackendInfo />
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
