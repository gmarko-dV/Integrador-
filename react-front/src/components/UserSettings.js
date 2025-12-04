import React, { useState, useEffect } from 'react';
import { useAuth } from './AuthProvider';
import { authService } from '../services/apiService';
import { setupAuthInterceptor } from '../services/apiService';
import './UserSettings.css';

const UserSettings = ({ onClose }) => {
  const { user, isAuthenticated, getIdTokenClaims, loginWithRedirect } = useAuth();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    fullName: ''
  });

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Asegurar que el interceptor esté configurado antes de hacer la petición
      if (isAuthenticated && getIdTokenClaims) {
        setupAuthInterceptor(getIdTokenClaims);
      }
      
      // Obtener perfil desde Django
      const profile = await authService.getUserProfile();
      
      // Si no hay first_name y last_name, intentar obtener desde Supabase
      let firstName = profile.first_name || '';
      let lastName = profile.last_name || '';
      
      if (!firstName && !lastName && user?.name) {
        // Dividir el nombre completo
        const nameParts = user.name.split(' ', 2);
        firstName = nameParts[0] || '';
        lastName = nameParts.slice(1).join(' ') || '';
      }
      
      setFormData({
        firstName: firstName,
        lastName: lastName,
        email: profile.email || user?.email || '',
        fullName: user?.name || `${firstName} ${lastName}`.trim() || ''
      });
    } catch (err) {
      console.error('Error cargando perfil:', err);
      
      // Manejar diferentes tipos de errores
      if (err.code === 'ERR_NETWORK' || err.message === 'Network Error') {
        setError('No se pudo conectar con el servidor. Por favor, verifica que el backend esté corriendo.');
      } else if (err.response?.status === 401) {
        setError('Tu sesión ha expirado. Por favor, inicia sesión nuevamente.');
      } else {
        setError('Error al cargar el perfil del usuario');
      }
      
      // Usar datos de Supabase como fallback
      if (user?.name) {
        const nameParts = user.name.split(' ', 2);
        setFormData({
          firstName: nameParts[0] || '',
          lastName: nameParts.slice(1).join(' ') || '',
          email: user.email || '',
          fullName: user.name || ''
        });
      } else if (user?.email) {
        setFormData({
          firstName: '',
          lastName: '',
          email: user.email || '',
          fullName: ''
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // Cargar perfil cuando el componente se monta o cuando cambia la autenticación
  useEffect(() => {
    if (isAuthenticated && getIdTokenClaims) {
      setupAuthInterceptor(getIdTokenClaims);
      loadUserProfile();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, getIdTokenClaims]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };


  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);

    // Validar que los campos no estén vacíos
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      setError('Por favor, completa todos los campos requeridos');
      setSaving(false);
      return;
    }

    try {
      // Asegurar que el interceptor esté configurado antes de hacer la petición
      if (isAuthenticated && getIdTokenClaims) {
        setupAuthInterceptor(getIdTokenClaims);
      }

      const response = await authService.updateProfile(
        formData.firstName.trim(),
        formData.lastName.trim()
      );

      if (response.success) {
        setSuccess('Perfil actualizado correctamente');
        
        // Actualizar el nombre en el formulario con la respuesta del servidor
        if (response.user) {
          setFormData(prev => ({
            ...prev,
            firstName: response.user.first_name || prev.firstName,
            lastName: response.user.last_name || prev.lastName,
            fullName: `${response.user.first_name || ''} ${response.user.last_name || ''}`.trim()
          }));
        }
        
        // Construir el nombre completo
        const fullName = `${response.user?.first_name || ''} ${response.user?.last_name || ''}`.trim();
        
        // Disparar evento para actualizar el perfil en otros componentes inmediatamente
        const updateEvent = new CustomEvent('profileUpdated', { 
          detail: { 
            firstName: response.user?.first_name,
            lastName: response.user?.last_name,
            fullName: fullName,
            user: response.user
          },
          bubbles: true,
          cancelable: true
        });
        
        // Disparar inmediatamente en window y document
        window.dispatchEvent(updateEvent);
        document.dispatchEvent(updateEvent);
        
        // Disparar nuevamente después de un pequeño delay para asegurar que se capture
        setTimeout(() => {
          window.dispatchEvent(updateEvent);
          document.dispatchEvent(updateEvent);
        }, 300);
        
        // Disparar una tercera vez después de más tiempo para asegurar que se capture
        setTimeout(() => {
          window.dispatchEvent(updateEvent);
          document.dispatchEvent(updateEvent);
        }, 800);
      } else {
        setError(response.error || 'Error al actualizar el perfil');
      }
    } catch (err) {
      console.error('Error actualizando perfil:', err);
      
      // Manejar diferentes tipos de errores
      if (err.code === 'ERR_NETWORK' || err.message === 'Network Error') {
        setError('No se pudo conectar con el servidor. Por favor, verifica que el backend esté corriendo.');
      } else if (err.response?.status === 401) {
        setError('Tu sesión ha expirado. Por favor, inicia sesión nuevamente.');
      } else if (err.response?.status === 403) {
        setError('No tienes permisos para realizar esta acción.');
      } else if (err.response?.data?.error) {
        setError(err.response.data.error);
      } else {
        setError('Error al actualizar el perfil. Por favor, intenta nuevamente.');
      }
    } finally {
      setSaving(false);
    }
  };

  const handleChangePassword = async () => {
    try {
      // Usar resetPassword de Supabase para enviar email de cambio de contraseña
      const { resetPassword } = await import('./AuthProvider').then(m => ({ resetPassword: m.useAuth }));
      // Redirigir a la página de recuperación de contraseña
      window.location.href = '/login?reset=true';
    } catch (err) {
      console.error('Error al redirigir para cambio de contraseña:', err);
      setError('No se pudo redirigir para cambiar la contraseña. Por favor, intenta nuevamente.');
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading) {
    return (
      <div className="user-settings-page">
        <div className="user-settings-container">
          <div className="user-settings-loading">
            <p>Cargando perfil...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="user-settings-page">
      <div className="user-settings-container">
        <div className="user-settings-header">
          <h2>Configuración de Perfil</h2>
          {onClose && (
            <button className="user-settings-close" onClick={onClose}>
              ← Volver
            </button>
          )}
        </div>

        <div className="user-settings-content">
          {error && (
            <div className="user-settings-alert user-settings-error">
              {error}
            </div>
          )}

          {success && (
            <div className="user-settings-alert user-settings-success">
              {success}
            </div>
          )}

          {/* Sección de Información Personal */}
          <section className="user-settings-section">
            <h3>Información Personal</h3>
            <form onSubmit={handleUpdateProfile}>
              <div className="user-settings-form-group">
                <label htmlFor="firstName">Nombre *</label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  placeholder="Ingresa tu nombre"
                  required
                  autoComplete="given-name"
                />
              </div>

              <div className="user-settings-form-group">
                <label htmlFor="lastName">Apellidos *</label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  placeholder="Ingresa tus apellidos"
                  required
                  autoComplete="family-name"
                />
              </div>

              <div className="user-settings-form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  disabled
                  className="user-settings-disabled"
                />
                <small className="user-settings-help-text">
                  El email no se puede cambiar desde aquí.
                </small>
              </div>

              <div className="user-settings-form-actions">
                <button 
                  type="submit" 
                  className="user-settings-btn user-settings-btn-primary"
                  disabled={saving}
                >
                  {saving ? 'Guardando...' : 'Guardar Cambios'}
                </button>
                {onClose && (
                  <button 
                    type="button" 
                    className="user-settings-btn user-settings-btn-secondary"
                    onClick={onClose}
                  >
                    Cancelar
                  </button>
                )}
              </div>
            </form>
          </section>

          {/* Sección de Cambio de Contraseña */}
          <section className="user-settings-section">
            <h3>Cambiar Contraseña</h3>
            <div className="user-settings-password-info">
              <p>Para cambiar tu contraseña, serás redirigido a la página de login donde podrás solicitar un enlace de recuperación.</p>
            </div>
            <div className="user-settings-form-actions">
              <button 
                type="button" 
                className="user-settings-btn user-settings-btn-primary"
                onClick={handleChangePassword}
              >
                Cambiar Contraseña
              </button>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default UserSettings;

