package com.integrador.service;

import com.integrador.dto.AnuncioRequest;
import com.integrador.entity.Anuncio;
import com.integrador.entity.Imagen;
import com.integrador.repository.AnuncioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AnuncioService {
    
    @Autowired
    private AnuncioRepository anuncioRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public Anuncio crearAnuncio(String idUsuario, AnuncioRequest request, List<MultipartFile> imagenes) {
        // Validaciones
        if (request.getModelo() == null || request.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo es requerido");
        }
        if (request.getAnio() == null || request.getAnio() < 1900 || request.getAnio() > 2100) {
            throw new IllegalArgumentException("El año debe ser válido");
        }
        if (request.getKilometraje() == null || request.getKilometraje() < 0) {
            throw new IllegalArgumentException("El kilometraje debe ser mayor o igual a 0");
        }
        if (request.getPrecio() == null || request.getPrecio().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es requerida");
        }
        if (imagenes == null || imagenes.size() < 2) {
            throw new IllegalArgumentException("Se requieren al menos 2 imágenes");
        }
        if (imagenes.size() > 2) {
            throw new IllegalArgumentException("Solo se permiten 2 imágenes");
        }
        
        // Crear el anuncio
        Anuncio anuncio = new Anuncio();
        anuncio.setIdUsuario(idUsuario);
        anuncio.setModelo(request.getModelo());
        anuncio.setAnio(request.getAnio());
        anuncio.setKilometraje(request.getKilometraje());
        anuncio.setPrecio(request.getPrecio());
        anuncio.setDescripcion(request.getDescripcion());
        anuncio.setTipoVehiculo(request.getTipoVehiculo());
        anuncio.setEmailContacto(request.getEmailContacto());
        anuncio.setTelefonoContacto(request.getTelefonoContacto());
        // Generar título automáticamente: "Modelo Año"
        String titulo = request.getModelo() + " " + request.getAnio();
        anuncio.setTitulo(titulo);
        anuncio.setActivo(true);
        
        // Guardar el anuncio primero para obtener el ID
        anuncio = anuncioRepository.save(anuncio);
        
        // Crear directorio de uploads si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Error al crear directorio de uploads", e);
            }
        }
        
        // Guardar las imágenes usando addImagen() para mantener la referencia de la colección
        for (int i = 0; i < imagenes.size(); i++) {
            MultipartFile archivo = imagenes.get(i);
            if (archivo != null && !archivo.isEmpty()) {
                try {
                    // Generar nombre único para el archivo
                    String extension = obtenerExtension(archivo.getOriginalFilename());
                    String nombreArchivo = UUID.randomUUID().toString() + extension;
                    Path rutaArchivo = uploadPath.resolve(nombreArchivo);
                    
                    // Guardar el archivo
                    Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Crear entidad Imagen
                    Imagen imagen = new Imagen();
                    imagen.setUrlImagen("/uploads/" + nombreArchivo);
                    imagen.setNombreArchivo(archivo.getOriginalFilename());
                    imagen.setTipoArchivo(archivo.getContentType());
                    imagen.setTamanoArchivo(archivo.getSize());
                    imagen.setOrden(i + 1);
                    
                    // Usar addImagen() en lugar de setImagenes() para mantener la referencia
                    anuncio.addImagen(imagen);
                } catch (IOException e) {
                    throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
                }
            }
        }
        
        // Guardar el anuncio con las imágenes
        return anuncioRepository.save(anuncio);
    }
    
    public List<Anuncio> obtenerAnunciosPorUsuario(String idUsuario) {
        System.out.println("=== SERVICIO: obtenerAnunciosPorUsuario ===");
        System.out.println("Buscando anuncios para userId: '" + idUsuario + "'");
        System.out.println("Longitud del userId: " + (idUsuario != null ? idUsuario.length() : 0));
        System.out.println("Tipo del userId: " + (idUsuario != null ? idUsuario.getClass().getName() : "null"));
        
        // Limpiar espacios en blanco si los hay y crear variable final para usar en lambdas
        final String userIdFinal = (idUsuario != null) ? idUsuario.trim() : null;
        
        if (userIdFinal == null || userIdFinal.isEmpty()) {
            System.out.println("ERROR: userId es null o vacío");
            return new java.util.ArrayList<>();
        }
        
        System.out.println("🔍 Buscando anuncios con userIdFinal: '" + userIdFinal + "' (longitud: " + userIdFinal.length() + ")");
        
        // Obtener todos los anuncios primero para debug
        List<Anuncio> todosLosAnuncios = anuncioRepository.findAll();
        System.out.println("📊 Total de anuncios en BD: " + todosLosAnuncios.size());
        todosLosAnuncios.forEach(a -> {
            String idUsuarioBD = a.getIdUsuario();
            boolean coincide = userIdFinal.equals(idUsuarioBD);
            System.out.println("  - Anuncio ID: " + a.getIdAnuncio() + 
                             ", idUsuario en BD: '" + idUsuarioBD + "'" +
                             " (longitud: " + (idUsuarioBD != null ? idUsuarioBD.length() : 0) + ")" +
                             ", Coincide: " + coincide);
        });
        
        List<Anuncio> anuncios = anuncioRepository.findByIdUsuario(userIdFinal);
        
        System.out.println("Anuncios encontrados en repositorio: " + anuncios.size());
        
        anuncios.forEach(anuncio -> {
            System.out.println("  ✅ Anuncio ID: " + anuncio.getIdAnuncio() + 
                             ", Usuario guardado: '" + anuncio.getIdUsuario() + "'" +
                             ", Coincide: " + userIdFinal.equals(anuncio.getIdUsuario()));
        });
        
        return anuncios;
    }
    
    public List<Anuncio> obtenerTodosLosAnunciosActivos() {
        System.out.println("=== SERVICIO: obtenerTodosLosAnunciosActivos ===");
        List<Anuncio> anuncios = anuncioRepository.findAllActivos();
        System.out.println("Anuncios encontrados en BD: " + anuncios.size());
        anuncios.forEach(anuncio -> {
            System.out.println("  - ID: " + anuncio.getIdAnuncio() + 
                             ", Modelo: " + anuncio.getModelo() + 
                             ", Activo: " + anuncio.getActivo() +
                             ", Imágenes: " + (anuncio.getImagenes() != null ? anuncio.getImagenes().size() : 0));
        });
        return anuncios;
    }
    
    @Transactional(readOnly = true)
    public Anuncio obtenerAnuncioPorId(Long id) {
        // Usar el método que carga las imágenes con JOIN FETCH para evitar problemas de lazy loading
        return anuncioRepository.findByIdWithImagenes(id)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
    }
    
    @Transactional
    public Anuncio actualizarAnuncio(Long idAnuncio, String idUsuario, AnuncioRequest request, List<MultipartFile> imagenes) {
        // Obtener el anuncio existente con sus imágenes cargadas
        Anuncio anuncio = anuncioRepository.findByIdWithImagenes(idAnuncio)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
        
        // Verificar que el usuario sea el dueño del anuncio
        if (!anuncio.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("No tienes permiso para editar este anuncio");
        }
        
        // Validaciones
        if (request.getModelo() == null || request.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo es requerido");
        }
        if (request.getAnio() == null || request.getAnio() < 1900 || request.getAnio() > 2100) {
            throw new IllegalArgumentException("El año debe ser válido");
        }
        if (request.getKilometraje() == null || request.getKilometraje() < 0) {
            throw new IllegalArgumentException("El kilometraje debe ser mayor o igual a 0");
        }
        if (request.getPrecio() == null || request.getPrecio().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es requerida");
        }
        
        // Actualizar los campos del anuncio
        anuncio.setModelo(request.getModelo());
        anuncio.setAnio(request.getAnio());
        anuncio.setKilometraje(request.getKilometraje());
        anuncio.setPrecio(request.getPrecio());
        anuncio.setDescripcion(request.getDescripcion());
        anuncio.setTipoVehiculo(request.getTipoVehiculo());
        anuncio.setEmailContacto(request.getEmailContacto());
        anuncio.setTelefonoContacto(request.getTelefonoContacto());
        // Actualizar título automáticamente
        String titulo = request.getModelo() + " " + request.getAnio();
        anuncio.setTitulo(titulo);
        
        // Si se proporcionan nuevas imágenes, reemplazar las existentes
        if (imagenes != null && !imagenes.isEmpty()) {
            // Eliminar imágenes antiguas del sistema de archivos
            List<Imagen> imagenesAntiguas = anuncio.getImagenes();
            if (imagenesAntiguas != null && !imagenesAntiguas.isEmpty()) {
                for (Imagen imagen : imagenesAntiguas) {
                    try {
                        String urlImagen = imagen.getUrlImagen();
                        if (urlImagen != null && urlImagen.startsWith("/uploads/")) {
                            String nombreArchivo = urlImagen.replace("/uploads/", "");
                            Path rutaImagen = Paths.get(uploadDir, nombreArchivo);
                            if (Files.exists(rutaImagen)) {
                                Files.delete(rutaImagen);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error al eliminar imagen antigua: " + e.getMessage());
                    }
                }
                // Limpiar la colección de imágenes
                anuncio.getImagenes().clear();
            }
            
            // Crear directorio de uploads si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                } catch (IOException e) {
                    throw new RuntimeException("Error al crear directorio de uploads", e);
                }
            }
            
            // Guardar las nuevas imágenes
            for (int i = 0; i < imagenes.size(); i++) {
                MultipartFile archivo = imagenes.get(i);
                if (archivo != null && !archivo.isEmpty()) {
                    try {
                        // Generar nombre único para el archivo
                        String extension = obtenerExtension(archivo.getOriginalFilename());
                        String nombreArchivo = UUID.randomUUID().toString() + extension;
                        Path rutaArchivo = uploadPath.resolve(nombreArchivo);
                        
                        // Guardar el archivo
                        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Crear entidad Imagen
                        Imagen imagen = new Imagen();
                        imagen.setUrlImagen("/uploads/" + nombreArchivo);
                        imagen.setNombreArchivo(archivo.getOriginalFilename());
                        imagen.setTipoArchivo(archivo.getContentType());
                        imagen.setTamanoArchivo(archivo.getSize());
                        imagen.setOrden(i + 1);
                        
                        // Agregar la imagen al anuncio
                        anuncio.addImagen(imagen);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        // Guardar el anuncio actualizado
        Anuncio anuncioGuardado = anuncioRepository.save(anuncio);
        
        // Recargar el anuncio con las imágenes para evitar problemas de lazy loading en la serialización
        return anuncioRepository.findByIdWithImagenes(idAnuncio)
            .orElse(anuncioGuardado);
    }
    
    @Transactional
    public void eliminarAnuncio(Long idAnuncio, String idUsuario) {
        Anuncio anuncio = anuncioRepository.findById(idAnuncio)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
        
        // Verificar que el usuario sea el dueño del anuncio
        if (!anuncio.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este anuncio");
        }
        
        // Cargar las imágenes explícitamente dentro de la transacción
        // Esto fuerza a Hibernate a cargar la colección lazy antes de que se cierre la sesión
        List<Imagen> imagenes = anuncio.getImagenes();
        if (imagenes != null && !imagenes.isEmpty()) {
            // Forzar la inicialización de la colección accediendo a su tamaño
            int cantidadImagenes = imagenes.size();
            System.out.println("Eliminando " + cantidadImagenes + " imágenes del anuncio " + idAnuncio);
            
            // Eliminar las imágenes físicas del sistema de archivos
            for (Imagen imagen : imagenes) {
                try {
                    String urlImagen = imagen.getUrlImagen();
                    if (urlImagen != null && urlImagen.startsWith("/uploads/")) {
                        String nombreArchivo = urlImagen.replace("/uploads/", "");
                        Path rutaImagen = Paths.get(uploadDir, nombreArchivo);
                        if (Files.exists(rutaImagen)) {
                            Files.delete(rutaImagen);
                            System.out.println("Imagen física eliminada: " + nombreArchivo);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error al eliminar imagen física: " + e.getMessage());
                    // Continuar aunque falle la eliminación de una imagen
                }
            }
        }
        
        // Eliminar el anuncio (las imágenes se eliminan automáticamente por cascade)
        anuncioRepository.delete(anuncio);
        System.out.println("Anuncio " + idAnuncio + " eliminado exitosamente");
    }
    
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            return "";
        }
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        if (ultimoPunto == -1) {
            return "";
        }
        return nombreArchivo.substring(ultimoPunto);
    }
}

