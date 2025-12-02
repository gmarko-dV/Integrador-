import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthProvider from './components/AuthProvider';
import Dashboard from './components/Dashboard';
import Login from './components/Login';
import Callback from './components/Callback';
import ListaAnunciosPage from './components/ListaAnunciosPage';
import DetalleAnuncio from './components/DetalleAnuncio';
import ChatIA from './components/ChatIA';
import UserSettingsPage from './components/UserSettingsPage';
import './App.css';

function App() {
  return (
    <AuthProvider>
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
    </AuthProvider>
  );
}

export default App;
