import React, { useState, useEffect } from 'react';
import { useAuth } from './AuthProvider';
import { useSearchParams, useNavigate } from 'react-router-dom';
import anuncioService from '../services/anuncioApiService';
import { setupAuthInterceptor } from '../services/apiService';
import { normalizeImageUrl } from '../utils/imageUtils';
import './PublicarAuto.css';

const PublicarAuto = () => {
  const { isAuthenticated, getIdTokenClaims, loginWithRedirect } = useAuth();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const anuncioIdEditar = searchParams.get('editar');
  const [esModoEdicion, setEsModoEdicion] = useState(false);
  const [cargandoDatos, setCargandoDatos] = useState(false);

  // Asegurar que el interceptor esté configurado
  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
    }
  }, [isAuthenticated, getIdTokenClaims]);

  // Cargar datos del anuncio si estamos en modo edición
  useEffect(() => {
    if (anuncioIdEditar && isAuthenticated) {
      setEsModoEdicion(true);
      setCargandoDatos(true);
      cargarAnuncioParaEditar(anuncioIdEditar);
    } else {
      setEsModoEdicion(false);
    }
  }, [anuncioIdEditar, isAuthenticated]);

  const cargarAnuncioParaEditar = async (id) => {
    try {
      setCargandoDatos(true);
      setError(null);
      console.log('Cargando anuncio para editar, ID:', id);
      const response = await anuncioService.obtenerAnuncioPorId(id);
      console.log('Respuesta del servidor:', response);
      
      if (response.success && response.anuncio) {
        const anuncio = response.anuncio;
        console.log('Anuncio cargado:', anuncio);
        
        // Convertir números a strings para los inputs
        setFormData({
          modelo: anuncio.modelo || '',
          anio: anuncio.anio ? String(anuncio.anio) : '',
          kilometraje: anuncio.kilometraje ? String(anuncio.kilometraje) : '',
          precio: anuncio.precio ? String(anuncio.precio) : '',
          descripcion: anuncio.descripcion || '',
          emailContacto: anuncio.emailContacto || '',
          telefonoContacto: anuncio.telefonoContacto || '',
          tipoVehiculo: anuncio.tipoVehiculo || '',
        });
        
        // Cargar imágenes existentes como previews (solo para mostrar, no para editar)
        if (anuncio.imagenes && anuncio.imagenes.length > 0) {
          // Mostrar primera imagen como preview si existe
          if (anuncio.imagenes[0] && anuncio.imagenes[0].urlImagen) {
            setPreview1(normalizeImageUrl(anuncio.imagenes[0].urlImagen));
          }
          // Mostrar segunda imagen como preview si existe
          if (anuncio.imagenes[1] && anuncio.imagenes[1].urlImagen) {
            setPreview2(normalizeImageUrl(anuncio.imagenes[1].urlImagen));
          }
        }
        
        console.log('Formulario cargado con datos del anuncio');
      } else {
        console.error('Respuesta sin éxito o sin anuncio:', response);
        setError('No se pudo cargar el anuncio para editar');
      }
    } catch (err) {
      console.error('Error al cargar anuncio para editar:', err);
      setError('Error al cargar el anuncio para editar: ' + (err.response?.data?.error || err.message));
    } finally {
      setCargandoDatos(false);
    }
  };
  const [formData, setFormData] = useState({
    modelo: '',
    anio: '',
    kilometraje: '',
    precio: '',
    descripcion: '',
    emailContacto: '',
    telefonoContacto: '',
    tipoVehiculo: '',
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
    if (!formData.tipoVehiculo) {
      setError('Debes seleccionar una categoría de vehículo');
      setLoading(false);
      return;
    }
    const emailContacto = formData.emailContacto?.trim() || '';
    const telefonoContacto = formData.telefonoContacto?.trim() || '';
    if (!emailContacto && !telefonoContacto) {
      setError('Debes proporcionar al menos un método de contacto (email o teléfono)');
      setLoading(false);
      return;
    }
    if (emailContacto && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailContacto)) {
      setError('El formato del email no es válido');
      setLoading(false);
      return;
    }
    if (!esModoEdicion && (!imagen1 || !imagen2)) {
      setError('Se requieren 2 imágenes para crear un nuevo anuncio');
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
        if (!token) {
          setError('No se pudo obtener el token de autenticación. Por favor, inicia sesión nuevamente.');
          setLoading(false);
          return;
        }
      } catch (tokenError) {
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
      formDataToSend.append('tipoVehiculo', formData.tipoVehiculo);
      const emailContacto = formData.emailContacto?.trim() || '';
      const telefonoContacto = formData.telefonoContacto?.trim() || '';
      if (emailContacto) {
        formDataToSend.append('emailContacto', emailContacto);
      }
      if (telefonoContacto) {
        formDataToSend.append('telefonoContacto', telefonoContacto);
      }
      // Solo agregar imágenes si se proporcionaron nuevas
      if (imagen1) {
        formDataToSend.append('imagen1', imagen1);
      }
      if (imagen2) {
        formDataToSend.append('imagen2', imagen2);
      }

      let response;
      if (esModoEdicion && anuncioIdEditar) {
        // Actualizar anuncio existente
        response = await anuncioService.actualizarAnuncio(anuncioIdEditar, formDataToSend);
      } else {
        // Crear nuevo anuncio
        if (!imagen1 || !imagen2) {
          setError('Se requieren 2 imágenes para crear un nuevo anuncio');
          setLoading(false);
          return;
        }
        response = await anuncioService.crearAnuncio(formDataToSend);
      }

      if (response.success) {
        setSuccess(true);
        if (esModoEdicion) {
          // Redirigir a mis anuncios después de editar
          setTimeout(() => {
            navigate('/?tab=anuncios');
          }, 2000);
        } else {
          // Limpiar formulario solo si es creación
          setFormData({
            modelo: '',
            anio: '',
            kilometraje: '',
            precio: '',
            descripcion: '',
            emailContacto: '',
            telefonoContacto: '',
            tipoVehiculo: '',
          });
          setImagen1(null);
          setImagen2(null);
          setPreview1(null);
          setPreview2(null);
          // Limpiar inputs de archivo
          const img1Input = document.getElementById('imagen1');
          const img2Input = document.getElementById('imagen2');
          if (img1Input) img1Input.value = '';
          if (img2Input) img2Input.value = '';
        }
      }
    } catch (err) {
      console.error('Error al crear anuncio:', err);
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

  if (cargandoDatos) {
    return (
      <div className="publicar-auto-container">
        <div className="publicar-auto-message">
          <p>Cargando datos del anuncio...</p>
        </div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="publicar-auto-form">
        <h2>{esModoEdicion ? 'Editar Anuncio' : 'Publicar Auto en Venta'}</h2>
        <p className="publicar-auto-subtitle">
          {esModoEdicion 
            ? 'Modifica los datos de tu vehículo. Las imágenes nuevas reemplazarán las actuales.'
            : 'Completa el formulario para publicar tu vehículo'}
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
            <p>{esModoEdicion ? '¡Anuncio actualizado exitosamente!' : '¡Anuncio publicado exitosamente!'}</p>
          </div>
        )}
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
            <label htmlFor="tipoVehiculo">Categoría de Vehículo *</label>
            <select
              id="tipoVehiculo"
              name="tipoVehiculo"
              value={formData.tipoVehiculo}
              onChange={handleInputChange}
              required
            >
              <option value="">Selecciona una categoría</option>
              <option value="Hatchback">Hatchback</option>
              <option value="Sedan">Sedan</option>
              <option value="Coupé">Coupé</option>
              <option value="SUV">SUV</option>
              <option value="Station Wagon">Station Wagon</option>
              <option value="Deportivo">Deportivo</option>
            </select>
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
            <label htmlFor="emailContacto">Email de Contacto</label>
            <input
              type="email"
              id="emailContacto"
              name="emailContacto"
              value={formData.emailContacto}
              onChange={handleInputChange}
              placeholder="Ej: vendedor@ejemplo.com"
            />
          </div>

          <div className="form-group">
            <label htmlFor="telefonoContacto">Teléfono de Contacto</label>
            <input
              type="tel"
              id="telefonoContacto"
              name="telefonoContacto"
              value={formData.telefonoContacto}
              onChange={handleInputChange}
              placeholder="Ej: +51 987 654 321"
            />
          </div>

          <div className="form-group">
            <label>Imágenes {esModoEdicion ? '(opcionales - las nuevas reemplazarán las actuales)' : '* (2 imágenes requeridas)'}</label>
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
                  required={!esModoEdicion}
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
                  required={!esModoEdicion}
                  style={{ display: 'none' }}
                />
              </div>
            </div>
          </div>

          <button type="submit" className="submit-button" disabled={loading}>
            {loading 
              ? (esModoEdicion ? 'Actualizando...' : 'Publicando...') 
              : (esModoEdicion ? 'Actualizar Anuncio' : 'Publicar Anuncio')}
          </button>
    </form>
  );
};

export default PublicarAuto;

