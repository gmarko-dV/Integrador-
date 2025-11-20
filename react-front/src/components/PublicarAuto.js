import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import anuncioService from '../services/anuncioApiService';
import { setupAuthInterceptor } from '../services/apiService';
import './PublicarAuto.css';

const PublicarAuto = () => {
  const { isAuthenticated, getIdTokenClaims, loginWithRedirect } = useAuth0();

  // Asegurar que el interceptor esté configurado
  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
    }
  }, [isAuthenticated, getIdTokenClaims]);
  const [formData, setFormData] = useState({
    modelo: '',
    anio: '',
    kilometraje: '',
    precio: '',
    descripcion: '',
  });
  const [imagen1, setImagen1] = useState(null);
  const [imagen2, setImagen2] = useState(null);
  const [preview1, setPreview1] = useState(null);
  const [preview2, setPreview2] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleImageChange = (e, imageNumber) => {
    const file = e.target.files[0];
    if (file) {
      if (imageNumber === 1) {
        setImagen1(file);
        const reader = new FileReader();
        reader.onloadend = () => {
          setPreview1(reader.result);
        };
        reader.readAsDataURL(file);
      } else {
        setImagen2(file);
        const reader = new FileReader();
        reader.onloadend = () => {
          setPreview2(reader.result);
        };
        reader.readAsDataURL(file);
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    // Validaciones
    if (!formData.modelo.trim()) {
      setError('El modelo es requerido');
      setLoading(false);
      return;
    }
    if (!formData.anio || formData.anio < 1900 || formData.anio > 2100) {
      setError('El año debe ser válido (1900-2100)');
      setLoading(false);
      return;
    }
    if (!formData.kilometraje || formData.kilometraje < 0) {
      setError('El kilometraje debe ser mayor o igual a 0');
      setLoading(false);
      return;
    }
    if (!formData.precio || formData.precio <= 0) {
      setError('El precio debe ser mayor a 0');
      setLoading(false);
      return;
    }
    if (!formData.descripcion.trim()) {
      setError('La descripción es requerida');
      setLoading(false);
      return;
    }
    if (!imagen1 || !imagen2) {
      setError('Se requieren 2 imágenes');
      setLoading(false);
      return;
    }

    try {
      // Verificar que tenemos el token antes de hacer la petición
      if (!getIdTokenClaims) {
        setError('No se puede obtener el token de autenticación. Por favor, recarga la página e inicia sesión nuevamente.');
        setLoading(false);
        return;
      }
      
      try {
        // Obtener ID token (siempre JWS, no JWE)
        const claims = await getIdTokenClaims();
        const token = claims?.__raw;
        console.log('ID Token obtenido correctamente:', token ? 'Sí (longitud: ' + token.length + ')' : 'No');
        if (!token) {
          setError('No se pudo obtener el token de autenticación. Por favor, inicia sesión nuevamente.');
          setLoading(false);
          return;
        }
      } catch (tokenError) {
        console.error('Error obteniendo ID token antes de la petición:', tokenError);
        console.error('Detalles del error:', {
          message: tokenError.message,
          error: tokenError.error,
          error_description: tokenError.error_description
        });
        
        // Mensaje más específico según el tipo de error
        let errorMessage = 'Error de autenticación. ';
        if (tokenError.error === 'login_required') {
          errorMessage += 'Tu sesión ha expirado.';
        } else if (tokenError.error === 'consent_required') {
          errorMessage += 'Se requiere tu consentimiento.';
        } else {
          errorMessage += 'No se pudo obtener el token de autenticación.';
        }
        
        setError(errorMessage);
        setLoading(false);
        
        // Si el error es de autenticación, ofrecer re-autenticarse
        if (tokenError.error === 'login_required' || tokenError.error === 'consent_required') {
          // Opcional: redirigir automáticamente después de 2 segundos
          setTimeout(() => {
            if (loginWithRedirect) {
              loginWithRedirect({
                authorizationParams: {
                  scope: 'openid profile email offline_access'
                }
              });
            }
          }, 2000);
        }
        return;
      }
      
      // Crear FormData
      const formDataToSend = new FormData();
      formDataToSend.append('modelo', formData.modelo);
      formDataToSend.append('anio', formData.anio);
      formDataToSend.append('kilometraje', formData.kilometraje);
      formDataToSend.append('precio', formData.precio);
      formDataToSend.append('descripcion', formData.descripcion);
      formDataToSend.append('imagen1', imagen1);
      formDataToSend.append('imagen2', imagen2);

      console.log('Enviando petición para crear anuncio...');
      const response = await anuncioService.crearAnuncio(formDataToSend);
      console.log('Respuesta recibida:', response);

      if (response.success) {
        setSuccess(true);
        // Limpiar formulario
        setFormData({
          modelo: '',
          anio: '',
          kilometraje: '',
          precio: '',
          descripcion: '',
        });
        setImagen1(null);
        setImagen2(null);
        setPreview1(null);
        setPreview2(null);
        // Limpiar inputs de archivo
        document.getElementById('imagen1').value = '';
        document.getElementById('imagen2').value = '';
      }
    } catch (err) {
      console.error('Error completo:', err);
      console.error('Error response:', err.response);
      const errorMessage = err.response?.data?.error || 
                          err.message || 
                          'Error al crear el anuncio. Por favor, intenta nuevamente.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="publicar-auto-container">
        <div className="publicar-auto-message">
          <p>Debes iniciar sesión para publicar un auto.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="publicar-auto-container">
      <div className="publicar-auto-card">
        <h2>Publicar Auto en Venta</h2>
        <p className="publicar-auto-subtitle">
          Completa el formulario para publicar tu vehículo
        </p>

        {error && (
          <div className="publicar-auto-error">
            <p>{error}</p>
            {error.includes('autenticación') && loginWithRedirect && (
              <button
                type="button"
                onClick={() => {
                  loginWithRedirect({
                    authorizationParams: {
                      // No usar audience si no hay un API configurado
                      // audience: 'q4z3HBJ8q0yVsUGCI9zyXskGA26Kus4b',
                      scope: 'openid profile email offline_access'
                    }
                  });
                }}
                className="re-auth-button"
                style={{
                  marginTop: '10px',
                  padding: '8px 16px',
                  backgroundColor: '#4a90e2',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Iniciar Sesión Nuevamente
              </button>
            )}
          </div>
        )}

        {success && (
          <div className="publicar-auto-success">
            <p>¡Anuncio publicado exitosamente!</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="publicar-auto-form">
          <div className="form-group">
            <label htmlFor="modelo">Modelo *</label>
            <input
              type="text"
              id="modelo"
              name="modelo"
              value={formData.modelo}
              onChange={handleInputChange}
              placeholder="Ej: Toyota Corolla"
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="anio">Año *</label>
              <input
                type="number"
                id="anio"
                name="anio"
                value={formData.anio}
                onChange={handleInputChange}
                placeholder="Ej: 2020"
                min="1900"
                max="2100"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="kilometraje">Kilometraje *</label>
              <input
                type="number"
                id="kilometraje"
                name="kilometraje"
                value={formData.kilometraje}
                onChange={handleInputChange}
                placeholder="Ej: 50000"
                min="0"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="precio">Precio (S/) *</label>
            <input
              type="number"
              id="precio"
              name="precio"
              value={formData.precio}
              onChange={handleInputChange}
              placeholder="Ej: 35000"
              min="0"
              step="0.01"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="descripcion">Descripción *</label>
            <textarea
              id="descripcion"
              name="descripcion"
              value={formData.descripcion}
              onChange={handleInputChange}
              placeholder="Describe tu vehículo..."
              rows="4"
              required
            />
          </div>

          <div className="form-group">
            <label>Imágenes * (2 imágenes requeridas)</label>
            <div className="imagenes-container">
              <div className="imagen-input">
                <label htmlFor="imagen1" className="imagen-label">
                  {preview1 ? (
                    <img src={preview1} alt="Preview 1" className="imagen-preview" />
                  ) : (
                    <div className="imagen-placeholder">
                      <span>📷</span>
                      <span>Imagen 1</span>
                    </div>
                  )}
                </label>
                <input
                  type="file"
                  id="imagen1"
                  accept="image/*"
                  onChange={(e) => handleImageChange(e, 1)}
                  required
                  style={{ display: 'none' }}
                />
              </div>

              <div className="imagen-input">
                <label htmlFor="imagen2" className="imagen-label">
                  {preview2 ? (
                    <img src={preview2} alt="Preview 2" className="imagen-preview" />
                  ) : (
                    <div className="imagen-placeholder">
                      <span>📷</span>
                      <span>Imagen 2</span>
                    </div>
                  )}
                </label>
                <input
                  type="file"
                  id="imagen2"
                  accept="image/*"
                  onChange={(e) => handleImageChange(e, 2)}
                  required
                  style={{ display: 'none' }}
                />
              </div>
            </div>
          </div>

          <button type="submit" className="submit-button" disabled={loading}>
            {loading ? 'Publicando...' : 'Publicar Anuncio'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default PublicarAuto;

