import React, { createContext, useContext, useEffect, useState } from 'react';
import { supabase } from '../config/supabase';

// Crear contexto de autenticación
const AuthContext = createContext({});

// Hook para usar el contexto de autenticación
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
};

// Provider de autenticación con Supabase
const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [session, setSession] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Obtener sesión inicial
    const getInitialSession = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession();
        setSession(session);
        setUser(session?.user ?? null);
        setIsAuthenticated(!!session?.user);
      } catch (error) {
        console.error('Error obteniendo sesión:', error);
      } finally {
        setIsLoading(false);
      }
    };

    getInitialSession();

    // Escuchar cambios de autenticación
    const { data: { subscription } } = supabase.auth.onAuthStateChange(
      async (event, session) => {
        setSession(session);
        setUser(session?.user ?? null);
        setIsAuthenticated(!!session?.user);
        setIsLoading(false);
      }
    );

    // Cleanup
    return () => {
      subscription?.unsubscribe();
    };
  }, []);

  // Función para login con redirect
  const loginWithRedirect = async (options = {}) => {
    try {
      // Por defecto, ir al login con email/password
      // Si quieres Google, puedes pasar { provider: 'google' }
      if (options?.provider === 'google') {
        const { error } = await supabase.auth.signInWithOAuth({
          provider: 'google',
          options: {
            redirectTo: window.location.origin + '/callback',
          },
        });
        if (error) throw error;
      } else {
        // Redirigir a página de login
        window.location.href = '/login';
      }
    } catch (error) {
      console.error('Error en login:', error);
      throw error;
    }
  };

  // Función para login con email/password
  const signIn = async (email, password) => {
    const { data, error } = await supabase.auth.signInWithPassword({
      email,
      password,
    });
    if (error) throw error;
    return data;
  };

  // Función para registro
  const signUp = async (email, password, metadata = {}) => {
    const { data, error } = await supabase.auth.signUp({
      email,
      password,
      options: {
        data: metadata,
      },
    });
    if (error) throw error;
    return data;
  };

  // Función para login con Google
  const signInWithGoogle = async () => {
    const { data, error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: window.location.origin + '/callback',
      },
    });
    if (error) throw error;
    return data;
  };

  // Función para logout
  const logout = async (options = {}) => {
    try {
      await supabase.auth.signOut();
      setUser(null);
      setSession(null);
      setIsAuthenticated(false);
      
      // Redirigir si se especifica
      if (options?.logoutParams?.returnTo) {
        window.location.href = options.logoutParams.returnTo;
      } else {
        window.location.href = '/';
      }
    } catch (error) {
      console.error('Error en logout:', error);
    }
  };

  // Función para obtener token (compatibilidad con código existente)
  const getIdTokenClaims = async () => {
    const { data: { session } } = await supabase.auth.getSession();
    if (session?.access_token && session?.user) {
      // Decodificar el JWT para obtener el 'sub' real
      // El access_token de Supabase es un JWT que contiene el 'sub' en el payload
      try {
        const tokenParts = session.access_token.split('.');
        if (tokenParts.length === 3) {
          // Decodificar el payload (segunda parte del JWT)
          const payload = JSON.parse(atob(tokenParts[1]));
          
          return {
            __raw: session.access_token,
            sub: payload.sub || session.user.id, // Usar 'sub' del JWT o fallback a user.id
            email: payload.email || session.user.email,
            ...session.user,
            ...payload // Incluir todos los claims del JWT
          };
        }
      } catch (error) {
        console.error('Error decodificando JWT:', error);
      }
      
      // Fallback: usar user.id como sub
      return {
        __raw: session.access_token,
        sub: session.user.id,
        email: session.user.email,
        ...session.user
      };
    }
    return null;
  };

  // Función para obtener el access token
  const getAccessToken = async () => {
    const { data: { session } } = await supabase.auth.getSession();
    return session?.access_token;
  };

  // Función para reset password
  const resetPassword = async (email) => {
    const { error } = await supabase.auth.resetPasswordForEmail(email, {
      redirectTo: window.location.origin + '/reset-password',
    });
    if (error) throw error;
  };

  // Obtener usuario con formato compatible
  const getUser = () => {
    if (!user) return null;
    return {
      sub: user.id,
      email: user.email,
      name: user.user_metadata?.nombre || user.user_metadata?.full_name || user.email?.split('@')[0],
      picture: user.user_metadata?.avatar_url || null,
      email_verified: user.email_confirmed_at != null,
      ...user.user_metadata
    };
  };

  const value = {
    // Estados
    user: getUser(),
    session,
    isLoading,
    isAuthenticated,
    
    // Funciones principales
    loginWithRedirect,
    logout,
    getIdTokenClaims,
    getAccessToken,
    
    // Funciones adicionales de Supabase
    signIn,
    signUp,
    signInWithGoogle,
    resetPassword,
    
    // Supabase client para acceso directo si es necesario
    supabase,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
