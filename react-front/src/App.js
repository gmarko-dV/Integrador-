import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthProvider from './components/AuthProvider';
import Dashboard from './components/Dashboard';
import Callback from './components/Callback';
import ListaAnunciosPage from './components/ListaAnunciosPage';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/callback" element={<Callback />} />
            <Route path="/anuncios" element={<ListaAnunciosPage />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
