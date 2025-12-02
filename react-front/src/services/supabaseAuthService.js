import { supabase } from '../config/supabase';

// ========================================
// SERVICIO DE AUTENTICACIÓN CON SUPABASE
// ========================================

export const authService = {
  // ========================================
  // REGISTRO E INICIO DE SESIÓN
  // ========================================

  // Registrar usuario con email y contraseña
  async signUp(email, password, metadata = {}) {
    const { data, error } = await supabase.auth.signUp({
      email,
      password,
      options: {
        data: metadata, // nombre, apellido, etc.
      },
    });

    if (error) throw error;
    return data;
  },

  // Iniciar sesión con email y contraseña
  async signIn(email, password) {
    const { data, error } = await supabase.auth.signInWithPassword({
      email,
      password,
    });

    if (error) throw error;
    return data;
  },

  // Iniciar sesión con Google
  async signInWithGoogle() {
    const { data, error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: window.location.origin + '/callback',
      },
    });

    if (error) throw error;
    return data;
  },

  // Iniciar sesión con GitHub
  async signInWithGitHub() {
    const { data, error } = await supabase.auth.signInWithOAuth({
      provider: 'github',
      options: {
        redirectTo: window.location.origin + '/callback',
      },
    });

    if (error) throw error;
    return data;
  },

  // Cerrar sesión
  async signOut() {
    const { error } = await supabase.auth.signOut();
    if (error) throw error;
  },

  // ========================================
  // GESTIÓN DE SESIÓN
  // ========================================

  // Obtener usuario actual
  async getCurrentUser() {
    const { data: { user }, error } = await supabase.auth.getUser();
    if (error) throw error;
    return user;
  },

  // Obtener sesión actual
  async getSession() {
    const { data: { session }, error } = await supabase.auth.getSession();
    if (error) throw error;
    return session;
  },

  // Verificar si hay usuario autenticado
  async isAuthenticated() {
    const session = await this.getSession();
    return session !== null;
  },

  // ========================================
  // RECUPERACIÓN DE CONTRASEÑA
  // ========================================

  // Enviar email para resetear contraseña
  async resetPassword(email) {
    const { data, error } = await supabase.auth.resetPasswordForEmail(email, {
      redirectTo: window.location.origin + '/reset-password',
    });

    if (error) throw error;
    return data;
  },

  // Actualizar contraseña
  async updatePassword(newPassword) {
    const { data, error } = await supabase.auth.updateUser({
      password: newPassword,
    });

    if (error) throw error;
    return data;
  },

  // ========================================
  // ACTUALIZACIÓN DE PERFIL
  // ========================================

  // Actualizar datos del usuario
  async updateProfile(updates) {
    const { data, error } = await supabase.auth.updateUser({
      data: updates,
    });

    if (error) throw error;
    return data;
  },

  // ========================================
  // LISTENERS DE AUTENTICACIÓN
  // ========================================

  // Suscribirse a cambios de autenticación
  onAuthStateChange(callback) {
    return supabase.auth.onAuthStateChange((event, session) => {
      callback(event, session);
    });
  },
};

export default authService;

