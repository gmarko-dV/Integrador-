import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import './Callback.css';

const Callback = () => {
  const navigate = useNavigate();
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    // Supabase maneja automáticamente el callback de OAuth
    // Solo necesitamos esperar a que la autenticación se complete y redirigir
    if (!isLoading) {
      if (isAuthenticated) {
        // Usuario autenticado, redirigir al inicio
        navigate('/');
      } else {
        // Si no está autenticado después de cargar, puede haber un error
        // Esperar un poco más por si está procesando
        const timer = setTimeout(() => {
          navigate('/');
        }, 2000);
        return () => clearTimeout(timer);
      }
    }
  }, [isAuthenticated, isLoading, navigate]);

  return (
    <div className="callback-container">
      <div className="callback-content">
        <div className="callback-spinner"></div>
        <h2>Procesando autenticación...</h2>
        <p>Por favor espera un momento</p>
      </div>
    </div>
  );
};

export default Callback;
