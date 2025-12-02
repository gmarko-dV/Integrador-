package com.tecsup.checkauto.config

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    // Credenciales de Supabase
    const val SUPABASE_URL = "https://kkjjgvqqzxothhojvzss.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtrampndnFxenhvdGhob2p2enNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQyNTkyNjQsImV4cCI6MjA3OTgzNTI2NH0.DR-bCWKczVoYuXbAFS_LWewJEb41E84AvAOVd7T_8sA"
    
    // Cliente de Supabase
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}

