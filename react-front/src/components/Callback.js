import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const Callback = () => {
  const { isLoading, error } = useAuth0();

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
