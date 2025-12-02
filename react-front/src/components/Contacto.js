import React, { useState } from 'react';
import './Contacto.css';

const Contacto = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    correo: '',
    telefono: '',
    mensaje: ''
  });
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    // Validaciones
    if (!formData.nombre.trim()) {
      setError('El nombre es requerido');
      setLoading(false);
      return;
    }
    if (!formData.correo.trim()) {
      setError('El correo electrónico es requerido');
      setLoading(false);
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) {
      setError('El formato del correo electrónico no es válido');
      setLoading(false);
      return;
    }
    if (!formData.mensaje.trim()) {
      setError('El mensaje es requerido');
      setLoading(false);
      return;
    }

    try {
      // Aquí puedes agregar la llamada a tu API cuando esté lista
      // Por ahora, simulamos el envío
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setSuccess(true);
      setFormData({
        nombre: '',
        correo: '',
        telefono: '',
        mensaje: ''
      });
      
      // Ocultar mensaje de éxito después de 5 segundos
      setTimeout(() => {
        setSuccess(false);
      }, 5000);
    } catch (err) {
      setError('Error al enviar el mensaje. Por favor, intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="contacto-container">
      {/* Header Section */}
      <div className="contacto-header">
        <div className="contacto-header-overlay">
          <h1 className="contacto-title">Contacto</h1>
        </div>
      </div>

      {/* Main Content */}
      <div className="contacto-content">
        <div className="contacto-cards-container">
          {/* Left Card - Contact Information */}
          <div className="contacto-info-card">
            <h2 className="contacto-card-title">Datos de Contacto</h2>
            <div className="contacto-info-section">
              <p className="contacto-info-label">Correo Electrónico</p>
              <div className="contacto-email-item">
                <span className="contacto-email-icon">✉</span>
                <div className="contacto-email-details">
                  <p className="contacto-email-category">Informaciones</p>
                  <a href="mailto:info@checkauto.pe" className="contacto-email-link">
                    info@checkauto.pe
                  </a>
                </div>
              </div>
              <div className="contacto-email-item">
                <span className="contacto-email-icon">✉</span>
                <div className="contacto-email-details">
                  <p className="contacto-email-category">Soporte</p>
                  <a href="mailto:soporte@checkauto.pe" className="contacto-email-link">
                    soporte@checkauto.pe
                  </a>
                </div>
              </div>
              <div className="contacto-email-item">
                <span className="contacto-email-icon">✉</span>
                <div className="contacto-email-details">
                  <p className="contacto-email-category">Publicidad & Marketing</p>
                  <a href="mailto:publicidad@checkauto.pe" className="contacto-email-link">
                    publicidad@checkauto.pe
                  </a>
                </div>
              </div>
            </div>
          </div>

          {/* Right Card - Contact Form */}
          <div className="contacto-form-card">
            <h2 className="contacto-card-title">Escríbenos un mensaje...</h2>
            <p className="contacto-form-description">
              Si tienes alguna duda sobre nuestros servicios o necesitas ayuda para publicar tu anuncio, 
              no dudes en escribirnos y nos pondremos en contacto contigo a la brevedad posible.
            </p>
            
            <form onSubmit={handleSubmit} className="contacto-form">
              {error && (
                <div className="contacto-error-message">
                  {error}
                </div>
              )}
              
              {success && (
                <div className="contacto-success-message">
                  ¡Mensaje enviado exitosamente! Nos pondremos en contacto contigo pronto.
                </div>
              )}

              <div className="contacto-form-group">
                <label htmlFor="nombre" className="contacto-form-label">
                  Nombre <span className="contacto-required">*</span>
                </label>
                <input
                  type="text"
                  id="nombre"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  className="contacto-form-input"
                  required
                />
              </div>

              <div className="contacto-form-row">
                <div className="contacto-form-group contacto-form-group-half">
                  <label htmlFor="correo" className="contacto-form-label">
                    Correo Electrónico <span className="contacto-required">*</span>
                  </label>
                  <input
                    type="email"
                    id="correo"
                    name="correo"
                    value={formData.correo}
                    onChange={handleInputChange}
                    className="contacto-form-input"
                    required
                  />
                </div>

                <div className="contacto-form-group contacto-form-group-half">
                  <label htmlFor="telefono" className="contacto-form-label">
                    Teléfono
                  </label>
                  <input
                    type="tel"
                    id="telefono"
                    name="telefono"
                    value={formData.telefono}
                    onChange={handleInputChange}
                    className="contacto-form-input"
                  />
                </div>
              </div>

              <div className="contacto-form-group">
                <label htmlFor="mensaje" className="contacto-form-label">
                  Mensaje <span className="contacto-required">*</span>
                </label>
                <textarea
                  id="mensaje"
                  name="mensaje"
                  value={formData.mensaje}
                  onChange={handleInputChange}
                  className="contacto-form-textarea"
                  rows="6"
                  required
                />
              </div>

              <button
                type="submit"
                className="contacto-form-submit"
                disabled={loading}
              >
                {loading ? 'Enviando...' : 'Enviar Mensaje'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Contacto;

