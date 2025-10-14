import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { LoginButton, Profile } from './AuthComponents';
import BackendInfo from './BackendInfo';
import PlateSearch from './PlateSearch';

const Dashboard = () => {
  const { isAuthenticated, isLoading, user } = useAuth0();

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
        {isAuthenticated ? (
          <div className="dashboard-grid">
            {/* Welcome Card */}
            <div className="card welcome-card">
              <div className="welcome-content">
                <div className="welcome-text">
                  <h2 style={{ marginTop: 0, fontSize: '28px', color: '#374151' }}>
                    ¡Bienvenido, {user.name}!
                  </h2>
                  <p className="welcome-subtitle">
                    Has iniciado sesión correctamente. Aquí tienes acceso a todas las funcionalidades.
                  </p>
                </div>
                <div className="welcome-avatar">
                  <img 
                    src={user.picture} 
                    alt={user.name}
                    className="welcome-avatar-img"
                  />
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="card">
              <h3>Acciones Rápidas</h3>
              <div className="quick-actions">
                <button className="action-btn">
                  <span className="action-icon">📊</span>
                  Ver Estadísticas
                </button>
                <button className="action-btn">
                  <span className="action-icon">⚙️</span>
                  Configuración
                </button>
                <button className="action-btn">
                  <span className="action-icon">📝</span>
                  Crear Nuevo
                </button>
                <button className="action-btn">
                  <span className="action-icon">📁</span>
                  Mis Archivos
                </button>
              </div>
            </div>

            {/* User Info Card */}
            <div className="card">
              <h3>Información del Usuario</h3>
              <div className="user-details">
                <div className="detail-item">
                  <strong>Nombre:</strong> {user.name}
                </div>
                <div className="detail-item">
                  <strong>Email:</strong> {user.email}
                </div>
                <div className="detail-item">
                  <strong>ID:</strong> {user.sub}
                </div>
                <div className="detail-item">
                  <strong>Última actualización:</strong> {new Date(user.updated_at).toLocaleString()}
                </div>
              </div>
            </div>

            {/* Búsqueda de Placas */}
            <div className="card plate-search-card">
              <PlateSearch />
            </div>

            {/* Backend Info */}
            <div className="card backend-info-card">
              <h3>Estado de los Servicios</h3>
              <BackendInfo />
            </div>
          </div>
        ) : (
          <div className="card">
            <h2 style={{ marginTop: 0, fontSize: '28px', color: '#374151' }}>
              Dashboard Principal
            </h2>
            <div className="status-info">
              <strong>¡Bienvenido!</strong> Inicia sesión para acceder a tu dashboard personalizado.
            </div>
            <div className="login-features">
              <h3>¿Qué puedes hacer al iniciar sesión?</h3>
              <ul className="features-list">
                <li>📊 Acceder a estadísticas personalizadas</li>
                <li>⚙️ Configurar tu perfil y preferencias</li>
                <li>📝 Crear y gestionar contenido</li>
                <li>📁 Organizar tus archivos</li>
                <li>🔒 Acceso seguro a todas las funcionalidades</li>
              </ul>
            </div>
            <div className="backend-info">
              <BackendInfo />
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
