package com.integrador.service;

import com.integrador.dto.PlacaAPIResponse;
import com.integrador.entity.HistorialBusqueda;
import com.integrador.entity.Vehiculo;
import com.integrador.repository.HistorialBusquedaRepository;
import com.integrador.repository.VehiculoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlateSearchService {
    
    @Autowired
    private VehiculoRepository vehiculoRepository;
    
    @Autowired
    private HistorialBusquedaRepository historialBusquedaRepository;
    
    @Autowired
    private PlacaAPIService placaAPIService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> searchPlate(String plateNumber, String userId) {
        try {
            System.out.println("=== INICIANDO BÚSQUEDA DE PLACA: " + plateNumber + " ===");

            // 1. Consultar la API SOAP
            String jsonResponse = placaAPIService.consultarPlacaReal(plateNumber);

            System.out.println("JSON obtenido (primeros 500 chars): " + 
                    (jsonResponse != null && jsonResponse.length() > 500 ? 
                     jsonResponse.substring(0, 500) + "..." : jsonResponse));

            // Verificar si el JSON está vacío o es inválido
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new RuntimeException("La API devolvió una respuesta vacía");
            }

            // Verificar si el JSON es válido
            if (!jsonResponse.trim().startsWith("{") && !jsonResponse.trim().startsWith("[")) {
                System.err.println("JSON inválido recibido: " + jsonResponse);
                throw new RuntimeException("La API devolvió un formato de respuesta inválido");
            }

            // 2. Parsear el JSON a objeto
            PlacaAPIResponse apiResponse;
            try {
                apiResponse = objectMapper.readValue(jsonResponse, PlacaAPIResponse.class);
                System.out.println("Objeto parseado exitosamente. Propiedades: " + apiResponse.getProperties().keySet());
            } catch (Exception parseError) {
                System.err.println("Error parseando JSON: " + parseError.getMessage());
                System.err.println("JSON recibido: " + jsonResponse);
                throw new RuntimeException("Error al procesar la respuesta de la API: " + parseError.getMessage(), parseError);
            }

            // 3. Convertir a nuestro formato
            Map<String, Object> vehicleInfo = convertirAVehicleInfo(apiResponse, plateNumber);
            System.out.println("Datos convertidos: " + vehicleInfo);

            // 4. Intentar guardar en base de datos (no crítico si falla)
            try {
                Vehiculo vehicle = createVehicleFromApiResponse(plateNumber, apiResponse);
                vehiculoRepository.save(vehicle);
                System.out.println("Vehículo guardado: " + vehicle.getMarca() + " " + vehicle.getModelo());
            } catch (Exception dbError) {
                System.err.println("Error guardando en BD (no crítico): " + dbError.getMessage());
                // Continuar aunque falle el guardado
            }

            // 5. Intentar registrar en historial (no crítico si falla)
            try {
                saveSearchHistory(userId, plateNumber, jsonResponse);
            } catch (Exception historyError) {
                System.err.println("Error guardando historial (no crítico): " + historyError.getMessage());
                // Continuar aunque falle el historial
            }

            // 6. Verificar que tenemos datos mínimos
            if (vehicleInfo.isEmpty() || 
                (vehicleInfo.get("marca") == null && vehicleInfo.get("modelo") == null)) {
                System.err.println("Advertencia: Los datos del vehículo están vacíos o incompletos");
            }

            return vehicleInfo;

        } catch (RuntimeException e) {
            // Re-lanzar RuntimeException tal cual
            throw e;
        } catch (Exception e) {
            System.err.println("=== ERROR EN BÚSQUEDA ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            throw new RuntimeException("No se pudo obtener información para la placa " + plateNumber +
                    ". Posibles causas:\n" +
                    "1. La placa no existe en el sistema\n" +
                    "2. Problema temporal con la API\n" +
                    "3. Formato de placa no reconocido\n" +
                    "4. Error de conexión con la API", e);
        }
    }
    
    private Map<String, Object> convertirAVehicleInfo(PlacaAPIResponse apiResponse, String placa) {
        Map<String, Object> vehicleInfo = new HashMap<>();

        try {
            vehicleInfo.put("placa", placa.toUpperCase());
            
            // Obtener marca y modelo
            String marca = apiResponse.getCarMake();
            String modelo = apiResponse.getCarModel();
            
            // Si no se encontró, intentar con los métodos alternativos
            if (marca == null || marca.isEmpty()) {
                marca = apiResponse.getPropertyAsString("Make");
            }
            if (modelo == null || modelo.isEmpty()) {
                modelo = apiResponse.getPropertyAsString("Model");
            }
            
            vehicleInfo.put("marca", marca != null && !marca.isEmpty() ? marca : "No especificado");
            vehicleInfo.put("modelo", modelo != null && !modelo.isEmpty() ? modelo : "No especificado");
            
            // Año de registro
            String anioStr = apiResponse.getRegistrationYear();
            vehicleInfo.put("anio_registro_api", parsearAnio(anioStr));
            
            // Descripción
            String descripcion = apiResponse.getDescription();
            vehicleInfo.put("descripcion_api", descripcion != null && !descripcion.isEmpty() ? descripcion : "Sin descripción");

            // Nuevos campos específicos de Perú
            vehicleInfo.put("propietario", apiResponse.getOwner());
            vehicleInfo.put("vin", apiResponse.getVIN());
            vehicleInfo.put("image_url_api", apiResponse.getImageUrl());
            vehicleInfo.put("uso", apiResponse.getUse());
            vehicleInfo.put("delivery_point", apiResponse.getDeliveryPoint());
            vehicleInfo.put("fecha_registro_api", apiResponse.getDate());

            // Campos técnicos
            vehicleInfo.put("tamano_motor", apiResponse.getEngineSize());
            vehicleInfo.put("tipo_combustible", apiResponse.getFuelType());
            vehicleInfo.put("numero_asientos", apiResponse.getNumberOfSeats());

            System.out.println("Vehículo convertido: " + vehicleInfo.get("marca") + " " + vehicleInfo.get("modelo"));
            System.out.println("Propietario: " + vehicleInfo.get("propietario"));
            System.out.println("VIN: " + vehicleInfo.get("vin"));
            System.out.println("Todas las propiedades disponibles: " + apiResponse.getProperties().keySet());

        } catch (Exception e) {
            System.err.println("Error convirtiendo datos del vehículo: " + e.getMessage());
            e.printStackTrace();
            // Devolver al menos la placa
            vehicleInfo.put("placa", placa.toUpperCase());
            vehicleInfo.put("error", "Error al procesar algunos datos del vehículo");
        }

        return vehicleInfo;
    }

    private Integer parsearAnio(String anioStr) {
        try {
            if (anioStr != null && !anioStr.trim().isEmpty()) {
                // Extraer números del string (puede venir como "2012" o "Year: 2012")
                String numeros = anioStr.replaceAll("\\D", "");
                if (!numeros.isEmpty()) {
                    int anio = Integer.parseInt(numeros);
                    // Validar que sea un año razonable
                    if (anio >= 1900 && anio <= java.time.Year.now().getValue() + 1) {
                        return anio;
                    }
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Vehiculo createVehicleFromApiResponse(String plateNumber, PlacaAPIResponse apiResponse) {
        Vehiculo vehicle = new Vehiculo();
        vehicle.setPlaca(plateNumber.toUpperCase());
        
        // Extraer datos del JSON de la API
        vehicle.setDescripcionApi(apiResponse.getDescription());
        vehicle.setMarca(apiResponse.getCarMake());
        vehicle.setModelo(apiResponse.getCarModel());
        vehicle.setAnioRegistroApi(apiResponse.getRegistrationYear());
        vehicle.setVin(apiResponse.getVIN());
        vehicle.setUso(apiResponse.getUse());
        vehicle.setPropietario(apiResponse.getOwner());
        vehicle.setDeliveryPoint(apiResponse.getDeliveryPoint());
        vehicle.setImageUrlApi(apiResponse.getImageUrl());
        
        // Fecha de registro
        vehicle.setFechaRegistroApi(LocalDateTime.now());
        vehicle.setFechaActualizacionApi(LocalDateTime.now());
        
        // Guardar datos completos como JSON
        try {
            vehicle.setDatosApi(objectMapper.valueToTree(apiResponse.getProperties()));
        } catch (Exception e) {
            vehicle.setDatosApi(objectMapper.createObjectNode());
        }
        
        return vehicle;
    }
    
    private void saveSearchHistory(String userId, String plateNumber, String resultJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(resultJson);
            HistorialBusqueda history = new HistorialBusqueda(userId, plateNumber, jsonNode);
            historialBusquedaRepository.save(history);
        } catch (Exception e) {
            // Si hay error parseando JSON, crear un nodo vacío
            HistorialBusqueda history = new HistorialBusqueda(userId, plateNumber, objectMapper.createObjectNode());
            historialBusquedaRepository.save(history);
        }
    }
    
    public List<HistorialBusqueda> getSearchHistory(String userId) {
        return historialBusquedaRepository.findTop5ByIdUsuarioOrderByFechaConsultaDesc(userId);
    }
    
    public List<Vehiculo> getRecentVehicles() {
        return vehiculoRepository.findTop10ByOrderByFechaActualizacionApiDesc();
    }

    public String getRawApiResponse(String plateNumber) {
        try {
            return placaAPIService.consultarPlacaReal(plateNumber);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener respuesta cruda de la API: " + e.getMessage(), e);
        }
    }
}
