import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useSearchParams } from 'react-router-dom';
import './Callback.css';

const Callback = () => {

  const { isLoading, error, isAuthenticated } = useAuth0();
  const [searchParams] = useSearchParams();
  const urlError = searchParams.get('error');
  const code = searchParams.get('code');
  const [hasRedirected, setHasRedirected] = useState(false);
  
  useEffect(() => {
    if (hasRedirected) return;
    
    if (urlError || error) return;
    
    if (!isLoading && isAuthenticated) {
      setHasRedirected(true);
      setTimeout(() => {
        window.location.href = '/';
      }, 500);
      return;
    }
    
    if (code && isLoading) return;
    
    if (!isLoading && !isAuthenticated && !code && !urlError && !error) {
      const timer = setTimeout(() => {
        if (!hasRedirected) {
          setHasRedirected(true);
          window.location.href = '/';
        }
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [isLoading, isAuthenticated, code, urlError, error, hasRedirected]);

  if (isLoading) {
    return (
      <div className="callback-loading">
        <p>Procesando login...</p>
      </div>
    );
  }

  // Detectar error de dominio no permitido
  const hasAccessDeniedError = urlError === 'access_denied' || 
                               (error && !isLoading && (
                                 error.error === 'access_denied' || 
                                 error.statusCode === 400 ||
                                 error.statusCode === 403
                               ));

  // Mostrar error de acceso denegado
  if (hasAccessDeniedError && !isLoading) {
    return (
      <div className="callback-error callback-error-access-denied">
        <h2>⚠️ Acceso Denegado</h2>
        <p className="callback-error-message">
          Solo se permiten correos institucionales de <strong>TECSUP</strong>.
        </p>
        <p className="callback-error-description">
          Por favor, inicia sesión con una cuenta que termine en <strong>@tecsup.edu.pe</strong>
        </p>
        <button 
          onClick={() => window.location.href = '/'}
          className="callback-error-button"
        >
          Volver al Inicio
        </button>
      </div>
    );
  }

  // Mostrar otros errores
  if (error && !hasAccessDeniedError) {
    return (
      <div className="callback-error callback-error-other">
        <h2>Error de Autenticación</h2>
        <p className="callback-error-message">
          {error.message || error.error_description || 'Error al procesar el login'}
        </p>
        <button 
          onClick={() => window.location.href = '/'}
          className="callback-error-button"
        >
          Volver al Inicio
        </button>
      </div>
    );
  }

  if (hasRedirected && isAuthenticated) {
    return null;
  }

  return (
    <div className="callback-success">
      <p><strong>¡Login exitoso!</strong></p>
      <p>Redirigiendo...</p>
    </div>
  );
};

export default Callback;
