package com.tecsup.checkauto.service

import com.tecsup.checkauto.config.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Servicio de autenticación con Supabase
 */
object SupabaseAuthService {
    private val client = SupabaseConfig.client
    
    // ========================================
    // REGISTRO E INICIO DE SESIÓN
    // ========================================
    
    /**
     * Registrar usuario con email y contraseña
     */
    suspend fun signUp(
        email: String, 
        password: String,
        nombre: String? = null
    ): UserInfo? {
        // Nota: redirectTo se configura en Supabase Dashboard
        // La URL de redirección se establece en Authentication → URL Configuration
        // La app móvil interceptará automáticamente la URL de la web mediante App Links
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            if (nombre != null) {
                this.data = buildJsonObject {
                    put("nombre", nombre)
                }
            }
        }
        return client.auth.currentUserOrNull()
    }
    
    /**
     * Iniciar sesión con email y contraseña
     */
    suspend fun signIn(email: String, password: String): UserInfo? {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return client.auth.currentUserOrNull()
    }
    
    /**
     * Cerrar sesión
     */
    suspend fun signOut() {
        client.auth.signOut()
    }
    
    // ========================================
    // GESTIÓN DE SESIÓN
    // ========================================
    
    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): UserInfo? {
        return client.auth.currentUserOrNull()
    }
    
    /**
     * Obtener sesión actual
     */
    fun getCurrentSession(): UserSession? {
        return client.auth.currentSessionOrNull()
    }
    
    /**
     * Verificar si hay usuario autenticado
     */
    fun isAuthenticated(): Boolean {
        return client.auth.currentUserOrNull() != null
    }
    
    /**
     * Obtener ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }
    
    /**
     * Obtener email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return client.auth.currentUserOrNull()?.email
    }
    
    // ========================================
    // RECUPERACIÓN DE CONTRASEÑA
    // ========================================
    
    /**
     * Enviar email para resetear contraseña
     */
    suspend fun resetPassword(email: String) {
        client.auth.resetPasswordForEmail(email)
    }
    
    /**
     * Actualizar contraseña
     */
    suspend fun updatePassword(newPassword: String) {
        client.auth.updateUser {
            password = newPassword
        }
    }
    
    // ========================================
    // ACTUALIZACIÓN DE PERFIL
    // ========================================
    
    /**
     * Actualizar datos del usuario
     */
    suspend fun updateProfile(nombre: String? = null) {
        client.auth.updateUser {
            if (nombre != null) {
                data = buildJsonObject {
                    put("nombre", nombre)
                }
            }
        }
    }
    
    // ========================================
    // LISTENERS
    // ========================================
    
    /**
     * Observar cambios en el estado de autenticación
     */
    fun sessionStatus() = client.auth.sessionStatus
}

