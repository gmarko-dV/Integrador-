import React, { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

const Callback = () => {
  const { isLoading, error, isAuthenticated } = useAuth0();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      // Redirigir al dashboard después de un breve delay para mostrar el mensaje
      setTimeout(() => {
        navigate('/');
      }, 1500);
    }
  }, [isLoading, isAuthenticated, navigate]);

  if (isLoading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <p>Procesando login...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="loading">
        <div className="status-error">
          <strong>Error:</strong> {error.message}
        </div>
      </div>
    );
  }

  return (
    <div className="loading">
      <div className="status-success">
        <strong>¡Login exitoso!</strong> Redirigiendo al dashboard...
      </div>
    </div>
  );
};

export default Callback;
