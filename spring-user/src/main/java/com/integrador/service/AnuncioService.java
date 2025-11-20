package com.integrador.service;

import com.integrador.dto.AnuncioRequest;
import com.integrador.entity.Anuncio;
import com.integrador.entity.Imagen;
import com.integrador.repository.AnuncioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
        return anuncioRepository.findByIdUsuario(idUsuario);
    }
    
    public List<Anuncio> obtenerTodosLosAnunciosActivos() {
        return anuncioRepository.findAllActivos();
    }
    
    public Anuncio obtenerAnuncioPorId(Long id) {
        return anuncioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
    }
    
    public void eliminarAnuncio(Long idAnuncio, String idUsuario) {
        Anuncio anuncio = anuncioRepository.findById(idAnuncio)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
        
        // Verificar que el usuario sea el dueño del anuncio
        if (!anuncio.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este anuncio");
        }
        
        // Eliminar las imágenes físicas del sistema de archivos
        if (anuncio.getImagenes() != null) {
            for (Imagen imagen : anuncio.getImagenes()) {
                try {
                    Path rutaImagen = Paths.get(uploadDir, imagen.getUrlImagen().replace("/uploads/", ""));
                    if (Files.exists(rutaImagen)) {
                        Files.delete(rutaImagen);
                    }
                } catch (IOException e) {
                    System.err.println("Error al eliminar imagen: " + e.getMessage());
                    // Continuar aunque falle la eliminación de una imagen
                }
            }
        }
        
        // Eliminar el anuncio (las imágenes se eliminan automáticamente por cascade)
        anuncioRepository.delete(anuncio);
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

