package com.integrador.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrador.dto.PlacaAPIResponse;
import com.integrador.entity.Vehiculo;
import com.integrador.repository.HistorialBusquedaRepository;
import com.integrador.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlateSearchServiceTest1 {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private HistorialBusquedaRepository historialBusquedaRepository;

    @Mock
    private PlacaAPIService placaAPIService;

    @InjectMocks
    private PlateSearchService plateSearchService;

    private ObjectMapper objectMapper;
    private String testPlateNumber;
    private String testUserId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testPlateNumber = "ABC123";
        testUserId = "auth0|123456";
    }

    @Test
    void testSearchPlate_Success() throws Exception {
        // Preparar datos
        PlacaAPIResponse mockResponse = new PlacaAPIResponse();
        mockResponse.setAdditionalProperty("CarMake", "Toyota");
        mockResponse.setAdditionalProperty("CarModel", "Corolla");
        mockResponse.setAdditionalProperty("RegistrationYear", "2020");
        
        String jsonResponse = objectMapper.writeValueAsString(mockResponse);
        
        when(placaAPIService.consultarPlacaReal(testPlateNumber)).thenReturn(jsonResponse);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historialBusquedaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar
        Map<String, Object> result = plateSearchService.searchPlate(testPlateNumber, testUserId);

        // Verificar
        assertNotNull(result);
        assertEquals("ABC123", result.get("placa"));
        assertEquals("Toyota", result.get("marca"));
        assertEquals("Corolla", result.get("modelo"));
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }
}

