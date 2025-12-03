package com.tecsup.checkauto.config

import android.content.Context
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.Auth

object SupabaseConfig {
    // Credenciales de Supabase
    // Puedes usar la nueva clave publishable (sb_publishable_xxx) o la legacy anon key
    const val SUPABASE_URL = "https://kkjjgvqqzxothhojvzss.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtrampndnFxenhvdGhob2p2enNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQyNTkyNjQsImV4cCI6MjA3OTgzNTI2NH0.DR-bCWKczVoYuXbAFS_LWewJEb41E84AvAOVd7T_8sA"
    // Alternativa: const val SUPABASE_ANON_KEY = "sb_publishable_ukj4e_F-n9Sz6PHCwP38kw_WKmb0Ssr"
    
    // Bucket de Storage para imágenes de anuncios
    const val STORAGE_BUCKET_ANUNCIOS = "anuncios"
    
    // Cliente de Supabase (se inicializa con contexto de Android para persistencia de sesión)
    private var _client: io.github.jan.supabase.SupabaseClient? = null
    
    fun initialize(context: Context) {
        if (_client == null) {
            _client = createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
                install(Realtime)
                install(Storage)
                install(Auth) {
                    // Configurar persistencia de sesión usando el contexto de Android
                    // El SDK de Supabase guardará automáticamente la sesión en SharedPreferences
                }
            }
        }
    }
    
    val client: io.github.jan.supabase.SupabaseClient
        get() {
            if (_client == null) {
                // Si no se ha inicializado, crear cliente sin contexto (fallback)
                // Esto no guardará la sesión, pero permitirá que la app funcione
                _client = createSupabaseClient(
                    supabaseUrl = SUPABASE_URL,
                    supabaseKey = SUPABASE_ANON_KEY
                ) {
                    install(Postgrest)
                    install(Realtime)
                    install(Storage)
                    install(Auth)
                }
            }
            return _client!!
        }
}

