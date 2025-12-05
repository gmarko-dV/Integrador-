package com.integrador.service;

import com.integrador.entity.Anuncio;
import com.integrador.entity.Notificacion;
import com.integrador.repository.AnuncioRepository;
import com.integrador.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {
    
    @Autowired
    private NotificacionRepository notificacionRepository;
    
    @Autowired
    private AnuncioRepository anuncioRepository;
    
    public Notificacion crearNotificacion(String idVendedor, String idComprador, Long idAnuncio, String mensaje, String nombreComprador, String emailComprador) {
        // Verificar que el anuncio existe
        Anuncio anuncio = anuncioRepository.findById(idAnuncio)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
        
        // Verificar que el vendedor es el dueño del anuncio
        if (!anuncio.getIdUsuario().equals(idVendedor)) {
            throw new IllegalArgumentException("El usuario no es el vendedor de este anuncio");
        }
        
        // Crear la notificación con título
        String titulo = "Interés en tu anuncio: " + (anuncio.getTitulo() != null ? anuncio.getTitulo() : anuncio.getModelo() + " " + anuncio.getAnio());
        Notificacion notificacion = new Notificacion(idVendedor, idComprador, idAnuncio, titulo, mensaje);
        
        // Establecer información del comprador
        notificacion.setNombreComprador(nombreComprador);
        notificacion.setEmailComprador(emailComprador);
        
        // Asegurar explícitamente que la notificación esté como no leída
        notificacion.setLeida(false);
        notificacion.setLeido(false);
        
        // Establecer idUsuario - convertir el hash del idVendedor a Integer
        // Como id_usuario es NOT NULL en la BD y es Integer, usamos un hash del string
        int idUsuarioHash = Math.abs(idVendedor.hashCode());
        // Asegurar que esté en el rango válido de Integer
        if (idUsuarioHash < 0) {
            idUsuarioHash = Math.abs(idUsuarioHash);
        }
        notificacion.setIdUsuario(idUsuarioHash);
        
        System.out.println("=== CREANDO NOTIFICACIÓN ===");
        System.out.println("ID Vendedor: " + idVendedor);
        System.out.println("ID Comprador: " + idComprador);
        System.out.println("Nombre Comprador: " + nombreComprador);
        System.out.println("Email Comprador: " + emailComprador);
        System.out.println("ID Anuncio: " + idAnuncio);
        System.out.println("Título: " + titulo);
        System.out.println("Mensaje: " + mensaje);
        
        Notificacion guardada = notificacionRepository.save(notificacion);
        
        System.out.println("Notificación guardada con ID: " + guardada.getIdNotificacion());
        System.out.println("ID Vendedor guardado: " + guardada.getIdVendedor());
        System.out.println("Leida: " + guardada.getLeida() + ", Leido: " + guardada.getLeido());
        
        return guardada;
    }
    
    public List<Notificacion> obtenerNotificacionesPorVendedor(String idVendedor) {
        System.out.println("=== OBTENIENDO NOTIFICACIONES POR VENDEDOR (Service) ===");
        System.out.println("ID Vendedor: " + idVendedor);
        
        List<Notificacion> todas = notificacionRepository.findAll();
        System.out.println("Total de notificaciones en BD: " + todas.size());
        for (Notificacion n : todas) {
            System.out.println("  - ID: " + n.getIdNotificacion() + ", Vendedor: " + n.getIdVendedor() + 
                ", Comprador: " + n.getIdComprador() + ", Anuncio: " + n.getIdAnuncio());
        }
        
        List<Notificacion> resultado = notificacionRepository.findByIdVendedorOrderByFechaCreacionDesc(idVendedor);
        System.out.println("Notificaciones encontradas para vendedor " + idVendedor + ": " + resultado.size());
        
        return resultado;
    }
    
    public List<Notificacion> obtenerNotificacionesNoLeidasPorVendedor(String idVendedor) {
        System.out.println("=== OBTENIENDO NOTIFICACIONES NO LEÍDAS ===");
        System.out.println("ID Vendedor: " + idVendedor);
        
        try {
            // Obtener todas las notificaciones del vendedor y filtrar las no leídas
            List<Notificacion> todas = notificacionRepository.findByIdVendedorOrderByFechaCreacionDesc(idVendedor);
            System.out.println("Total de notificaciones del vendedor: " + todas.size());
            
            // Filtrar las no leídas manualmente
            // Una notificación se considera no leída si leida es null/false O leido es null/false
            List<Notificacion> noLeidas = todas.stream()
                .filter(n -> {
                    Boolean leida = n.getLeida();
                    Boolean leido = n.getLeido();
                    // Considerar no leída si cualquiera de los dos campos es null o false
                    return (leida == null || !leida) || (leido == null || !leido);
                })
                .collect(Collectors.toList());
            
            System.out.println("Notificaciones no leídas encontradas: " + noLeidas.size());
            for (Notificacion n : noLeidas) {
                System.out.println("  - ID: " + n.getIdNotificacion() + ", Vendedor: " + n.getIdVendedor() + 
                    ", Leida: " + n.getLeida() + ", Leido: " + n.getLeido() + ", Mensaje: " + 
                    (n.getMensaje() != null ? n.getMensaje().substring(0, Math.min(50, n.getMensaje().length())) : "null"));
            }
            return noLeidas;
        } catch (Exception e) {
            System.err.println("ERROR al obtener notificaciones: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Long contarNotificacionesNoLeidas(String idVendedor) {
        System.out.println("=== CONTANDO NOTIFICACIONES NO LEÍDAS ===");
        System.out.println("ID Vendedor: " + idVendedor);
        
        try {
            // Contar manualmente para evitar problemas con el query
            List<Notificacion> todas = notificacionRepository.findByIdVendedorOrderByFechaCreacionDesc(idVendedor);
            long count = todas.stream()
                .filter(n -> {
                    Boolean leida = n.getLeida();
                    Boolean leido = n.getLeido();
                    return (leida == null || !leida) || (leido == null || !leido);
                })
                .count();
            
            System.out.println("Cantidad no leídas: " + count);
            return count;
        } catch (Exception e) {
            System.err.println("ERROR al contar notificaciones: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    public Notificacion marcarComoLeida(Long idNotificacion, String idVendedor) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
            .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
        
        // Verificar que el vendedor es el dueño de la notificación
        if (!notificacion.getIdVendedor().equals(idVendedor)) {
            throw new IllegalArgumentException("No tienes permiso para marcar esta notificación como leída");
        }
        
        notificacion.setLeida(true);
        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }
    
    public void marcarTodasComoLeidas(String idVendedor) {
        List<Notificacion> notificaciones = notificacionRepository.findByIdVendedorAndLeidaFalseOrderByFechaCreacionDesc(idVendedor);
        for (Notificacion notificacion : notificaciones) {
            notificacion.setLeida(true);
            notificacion.setLeido(true);
        }
        notificacionRepository.saveAll(notificaciones);
    }
}

