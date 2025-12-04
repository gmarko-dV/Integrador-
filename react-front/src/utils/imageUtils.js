/**
 * Normaliza la URL de una imagen
 * Si la URL ya es completa (http/https), la devuelve tal cual
 * Si es relativa, le agrega el prefijo del servidor
 */
export const normalizeImageUrl = (url) => {
  if (!url) {
    return 'https://via.placeholder.com/300x200?text=Sin+Imagen';
  }

  // Si ya es una URL completa (http/https), usarla tal cual
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }

  // Si es una URL de Supabase Storage (empieza con /storage/), construir la URL completa
  if (url.includes('supabase.co/storage')) {
    return url;
  }

  // Si es relativa (empieza con /), agregar el servidor de Spring Boot
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`;
  }

  // Si no empieza con /, asumir que es relativa
  return `http://localhost:8080/${url}`;
};

