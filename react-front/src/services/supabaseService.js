import { supabase } from '../config/supabase';

// ========================================
// SERVICIO DE ANUNCIOS
// ========================================
export const anunciosService = {
  // Obtener todos los anuncios activos
  async getAll(filters = {}) {
    let query = supabase
      .from('anuncios')
      .select(`
        *,
        imagenes (*)
      `)
      .eq('activo', true)
      .order('fecha_creacion', { ascending: false });

    // Aplicar filtros opcionales
    if (filters.tipo_vehiculo) {
      query = query.eq('tipo_vehiculo', filters.tipo_vehiculo);
    }
    if (filters.precio_min) {
      query = query.gte('precio', filters.precio_min);
    }
    if (filters.precio_max) {
      query = query.lte('precio', filters.precio_max);
    }
    if (filters.anio_min) {
      query = query.gte('anio', filters.anio_min);
    }
    if (filters.limit) {
      query = query.limit(filters.limit);
    }

    const { data, error } = await query;
    if (error) throw error;
    return data;
  },

  // Obtener un anuncio por ID
  async getById(id) {
    const { data, error } = await supabase
      .from('anuncios')
      .select(`
        *,
        imagenes (*)
      `)
      .eq('id_anuncio', id)
      .single();

    if (error) throw error;
    return data;
  },

  // Obtener anuncios de un usuario específico
  async getByUserId(userId) {
    const { data, error } = await supabase
      .from('anuncios')
      .select(`
        *,
        imagenes (*)
      `)
      .eq('id_usuario', userId)
      .order('fecha_creacion', { ascending: false });

    if (error) throw error;
    return data;
  },

  // Crear nuevo anuncio
  async create(anuncioData) {
    const { data, error } = await supabase
      .from('anuncios')
      .insert([anuncioData])
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Actualizar anuncio
  async update(id, anuncioData) {
    const { data, error } = await supabase
      .from('anuncios')
      .update({
        ...anuncioData,
        fecha_actualizacion: new Date().toISOString()
      })
      .eq('id_anuncio', id)
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Eliminar anuncio (soft delete)
  async delete(id) {
    const { data, error } = await supabase
      .from('anuncios')
      .update({ activo: false })
      .eq('id_anuncio', id)
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Eliminar anuncio permanentemente
  async hardDelete(id) {
    const { error } = await supabase
      .from('anuncios')
      .delete()
      .eq('id_anuncio', id);

    if (error) throw error;
    return true;
  }
};

// ========================================
// SERVICIO DE IMÁGENES
// ========================================
export const imagenesService = {
  // Agregar imagen a un anuncio
  async add(imagenData) {
    const { data, error } = await supabase
      .from('imagenes')
      .insert([imagenData])
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Obtener imágenes de un anuncio
  async getByAnuncioId(anuncioId) {
    const { data, error } = await supabase
      .from('imagenes')
      .select('*')
      .eq('id_anuncio', anuncioId)
      .order('orden', { ascending: true });

    if (error) throw error;
    return data;
  },

  // Eliminar imagen
  async delete(id) {
    const { error } = await supabase
      .from('imagenes')
      .delete()
      .eq('id_imagen', id);

    if (error) throw error;
    return true;
  },

  // Subir imagen al storage de Supabase
  async uploadToStorage(file, fileName) {
    const { data, error } = await supabase.storage
      .from('anuncios-images')
      .upload(fileName, file, {
        cacheControl: '3600',
        upsert: false
      });

    if (error) throw error;
    
    // Obtener URL pública
    const { data: urlData } = supabase.storage
      .from('anuncios-images')
      .getPublicUrl(fileName);

    return urlData.publicUrl;
  }
};

// ========================================
// SERVICIO DE VEHÍCULOS
// ========================================
export const vehiculosService = {
  // Obtener vehículo por placa
  async getByPlaca(placa) {
    const { data, error } = await supabase
      .from('vehiculos')
      .select('*')
      .eq('placa', placa)
      .single();

    if (error && error.code !== 'PGRST116') throw error; // PGRST116 = no rows
    return data;
  },

  // Crear o actualizar vehículo
  async upsert(vehiculoData) {
    const { data, error } = await supabase
      .from('vehiculos')
      .upsert([vehiculoData], { onConflict: 'placa' })
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Obtener todos los vehículos
  async getAll() {
    const { data, error } = await supabase
      .from('vehiculos')
      .select('*')
      .order('fecha_actualizacion_api', { ascending: false });

    if (error) throw error;
    return data;
  }
};

// ========================================
// SERVICIO DE NOTIFICACIONES
// ========================================
export const notificacionesService = {
  // Obtener notificaciones de un usuario (como vendedor)
  async getByVendedor(vendedorId) {
    const { data, error } = await supabase
      .from('notificaciones')
      .select(`
        *,
        anuncios:id_anuncio (titulo, modelo, precio)
      `)
      .eq('id_vendedor', vendedorId)
      .order('fecha_creacion', { ascending: false });

    if (error) throw error;
    return data;
  },

  // Obtener notificaciones no leídas
  async getUnread(vendedorId) {
    const { data, error } = await supabase
      .from('notificaciones')
      .select('*')
      .eq('id_vendedor', vendedorId)
      .eq('leido', false)
      .order('fecha_creacion', { ascending: false });

    if (error) throw error;
    return data;
  },

  // Contar notificaciones no leídas
  async countUnread(vendedorId) {
    const { count, error } = await supabase
      .from('notificaciones')
      .select('*', { count: 'exact', head: true })
      .eq('id_vendedor', vendedorId)
      .eq('leido', false);

    if (error) throw error;
    return count;
  },

  // Crear notificación (cuando alguien muestra interés)
  async create(notificacionData) {
    const { data, error } = await supabase
      .from('notificaciones')
      .insert([{
        ...notificacionData,
        tipo: notificacionData.tipo || 'interes'
      }])
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Marcar como leída
  async markAsRead(id) {
    const { data, error } = await supabase
      .from('notificaciones')
      .update({ leido: true, leida: true })
      .eq('id_notificacion', id)
      .select()
      .single();

    if (error) throw error;
    return data;
  },

  // Marcar todas como leídas
  async markAllAsRead(vendedorId) {
    const { data, error } = await supabase
      .from('notificaciones')
      .update({ leido: true, leida: true })
      .eq('id_vendedor', vendedorId)
      .eq('leido', false)
      .select();

    if (error) throw error;
    return data;
  },

  // Suscribirse a notificaciones en tiempo real
  subscribeToNewNotifications(vendedorId, callback) {
    return supabase
      .channel('notificaciones-channel')
      .on(
        'postgres_changes',
        {
          event: 'INSERT',
          schema: 'public',
          table: 'notificaciones',
          filter: `id_vendedor=eq.${vendedorId}`
        },
        (payload) => callback(payload.new)
      )
      .subscribe();
  }
};

// ========================================
// SERVICIO DE HISTORIAL DE BÚSQUEDA
// ========================================
export const historialService = {
  // Obtener historial de un usuario
  async getByUserId(userId, limit = 10) {
    const { data, error } = await supabase
      .from('historial_busqueda')
      .select('*')
      .eq('id_usuario', userId)
      .order('fecha_consulta', { ascending: false })
      .limit(limit);

    if (error) throw error;
    return data;
  },

  // Agregar búsqueda al historial
  async add(historialData) {
    const { data, error } = await supabase
      .from('historial_busqueda')
      .insert([historialData])
      .select()
      .single();

    if (error) throw error;
    return data;
  }
};

// Exportar todos los servicios
export default {
  anuncios: anunciosService,
  imagenes: imagenesService,
  vehiculos: vehiculosService,
  notificaciones: notificacionesService,
  historial: historialService
};

