import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { signIn, signUp, signInWithGoogle, isAuthenticated, isLoading } = useAuth();
  
  const [isLoginMode, setIsLoginMode] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [nombre, setNombre] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  // Redirigir si ya está autenticado
  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      navigate('/');
    }
  }, [isAuthenticated, isLoading, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      if (isLoginMode) {
        // Iniciar sesión
        await signIn(email, password);
        navigate('/');
      } else {
        // Registrarse
        if (password !== confirmPassword) {
          setError('Las contraseñas no coinciden');
          setLoading(false);
          return;
        }

        if (password.length < 6) {
          setError('La contraseña debe tener al menos 6 caracteres');
          setLoading(false);
          return;
        }

        await signUp(email, password, { nombre });
        setMessage('¡Registro exitoso! Revisa tu email para confirmar tu cuenta.');
        setIsLoginMode(true);
      }
    } catch (err) {
      console.error('Error de autenticación:', err);
      if (err.message.includes('Invalid login credentials')) {
        setError('Email o contraseña incorrectos');
      } else if (err.message.includes('User already registered')) {
        setError('Este email ya está registrado. Intenta iniciar sesión.');
      } else {
        setError(err.message || 'Error de autenticación');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    setError('');
    setLoading(true);
    try {
      await signInWithGoogle();
    } catch (err) {
      console.error('Error con Google:', err);
      setError(err.message || 'Error al iniciar sesión con Google');
      setLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="login-page">
        <div className="login-loading">Cargando...</div>
      </div>
    );
  }

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <h1>CheckAuto</h1>
            <p>{isLoginMode ? 'Inicia sesión en tu cuenta' : 'Crea una nueva cuenta'}</p>
          </div>

          {error && <div className="login-error">{error}</div>}
          {message && <div className="login-success">{message}</div>}

          <form onSubmit={handleSubmit} className="login-form">
            {!isLoginMode && (
              <div className="form-group">
                <label htmlFor="nombre">Nombre completo</label>
                <input
                  type="text"
                  id="nombre"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder="Tu nombre"
                  required={!isLoginMode}
                />
              </div>
            )}

            <div className="form-group">
              <label htmlFor="email">Correo electrónico</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="tu@email.com"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">Contraseña</label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>

            {!isLoginMode && (
              <div className="form-group">
                <label htmlFor="confirmPassword">Confirmar contraseña</label>
                <input
                  type="password"
                  id="confirmPassword"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  required={!isLoginMode}
                />
              </div>
            )}

            <button 
              type="submit" 
              className="login-submit-btn"
              disabled={loading}
            >
              {loading ? 'Cargando...' : (isLoginMode ? 'Iniciar Sesión' : 'Registrarse')}
            </button>
          </form>

          {/* Google login deshabilitado - necesita configurar credenciales en Supabase */}
          {/* 
          <div className="login-divider">
            <span>o continúa con</span>
          </div>

          <div className="social-login-buttons">
            <button 
              onClick={handleGoogleLogin} 
              className="google-login-btn"
              disabled={loading}
            >
              Google
            </button>
          </div>
          */}

          <div className="login-toggle">
            {isLoginMode ? (
              <p>
                ¿No tienes cuenta?{' '}
                <button type="button" onClick={() => { setIsLoginMode(false); setError(''); setMessage(''); }}>
                  Regístrate
                </button>
              </p>
            ) : (
              <p>
                ¿Ya tienes cuenta?{' '}
                <button type="button" onClick={() => { setIsLoginMode(true); setError(''); setMessage(''); }}>
                  Inicia sesión
                </button>
              </p>
            )}
          </div>

          <div className="login-back">
            <button type="button" onClick={() => navigate('/')}>
              Volver al inicio
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;

