import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthProvider, { useAuth } from './components/AuthProvider';
import Dashboard from './components/Dashboard';
import Login from './components/Login';
import Callback from './components/Callback';
import ListaAnunciosPage from './components/ListaAnunciosPage';
import DetalleAnuncio from './components/DetalleAnuncio';
import ChatIA from './components/ChatIA';
import UserSettingsPage from './components/UserSettingsPage';
import { setupAuthInterceptor } from './services/apiService';
import './App.css';

// Componente interno para configurar el interceptor globalmente
const AppContent = () => {
  const { isAuthenticated, getIdTokenClaims } = useAuth();

  useEffect(() => {
    // Configurar el interceptor globalmente cuando el usuario se autentica
    if (isAuthenticated && getIdTokenClaims) {
      console.log('🔐 Configurando interceptor de autenticación globalmente');
      setupAuthInterceptor(getIdTokenClaims);
    }
  }, [isAuthenticated, getIdTokenClaims]);

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/login" element={<Login />} />
          <Route path="/callback" element={<Callback />} />
          <Route path="/anuncios" element={<ListaAnunciosPage />} />
          <Route path="/anuncio/:idAnuncio" element={<DetalleAnuncio />} />
          <Route path="/chat" element={<ChatIA />} />
          <Route path="/configuracion" element={<UserSettingsPage />} />
        </Routes>
      </div>
    </Router>
  );
};

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
