package com.integrador.service;

import com.integrador.entity.Vehiculo;
import com.integrador.repository.HistorialBusquedaRepository;
import com.integrador.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlateSearchServiceTest2 {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private HistorialBusquedaRepository historialBusquedaRepository;

    @Mock
    private PlacaAPIService placaAPIService;

    @InjectMocks
    private PlateSearchService plateSearchService;

    private String testPlateNumber;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testPlateNumber = "XYZ789";
        testUserId = "auth0|789012";
    }

    @Test
    void testSearchPlate_EmptyResponse() {
        // Preparar
        when(placaAPIService.consultarPlacaReal(testPlateNumber)).thenReturn("");

        // Ejecutar y Verificar
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            plateSearchService.searchPlate(testPlateNumber, testUserId);
        });

        // Verificar que se lanzó excepción y no se guardó vehículo
        assertNotNull(exception);
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }
}

