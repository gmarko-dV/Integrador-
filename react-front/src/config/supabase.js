import { createClient } from '@supabase/supabase-js';

// Configuración de Supabase
const SUPABASE_URL = 'https://kkjjgvqqzxothhojvzss.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtrampndnFxenhvdGhob2p2enNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQyNTkyNjQsImV4cCI6MjA3OTgzNTI2NH0.DR-bCWKczVoYuXbAFS_LWewJEb41E84AvAOVd7T_8sA';

// Crear cliente de Supabase
export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// Exportar configuración para uso en otros lugares
export const SUPABASE_CONFIG = {
  url: SUPABASE_URL,
  anonKey: SUPABASE_ANON_KEY,
};

export default supabase;

