# Guía de Pruebas Unitarias

Este documento explica cómo ejecutar y crear pruebas unitarias para el proyecto Spring Boot.

## Ejecutar Pruebas

### Ejecutar todas las pruebas
```bash
mvn test
```

### Ejecutar una clase de prueba específica
```bash
mvn test -Dtest=PlateSearchServiceTest
```

### Ejecutar un método de prueba específico
```bash
mvn test -Dtest=PlateSearchServiceTest#testSearchPlate_Success
```

### Ejecutar pruebas desde IDE
- **IntelliJ IDEA**: Click derecho en la clase de prueba → "Run 'ClassNameTest'"
- **Eclipse**: Click derecho en la clase de prueba → "Run As" → "JUnit Test"
- **VS Code**: Usar la extensión "Java Test Runner"

## Estructura de Pruebas

Las pruebas están organizadas en el directorio `src/test/java/com/integrador/`:

```
src/test/java/com/integrador/
├── service/
│   ├── PlateSearchServiceTest.java
│   └── AnuncioServiceTest.java
└── controller/
    └── PlateSearchControllerTest.java
```

## Ejemplos de Pruebas Creadas

### 1. PlateSearchServiceTest
Pruebas para el servicio de búsqueda de placas:
- ✅ Búsqueda exitosa de placa
- ✅ Manejo de respuestas vacías
- ✅ Manejo de excepciones de API
- ✅ Conversión de placa a mayúsculas
- ✅ Valores por defecto cuando campos son null
- ✅ Obtención de historial de búsquedas
- ✅ Obtención de vehículos recientes

### 2. AnuncioServiceTest
Pruebas para el servicio de anuncios:
- ✅ Creación exitosa de anuncio
- ✅ Validación de modelo (vacío/null)
- ✅ Validación de año (inválido)
- ✅ Validación de kilometraje (negativo)
- ✅ Validación de precio (cero/negativo)
- ✅ Validación de descripción
- ✅ Validación de imágenes (cantidad)
- ✅ Generación automática de título

### 3. PlateSearchControllerTest
Pruebas para el controlador REST:
- ✅ Búsqueda exitosa de placa
- ✅ Error 400 cuando placa está vacía
- ✅ Error 500 cuando servicio falla

## Crear Nuevas Pruebas

### Ejemplo: Prueba de Servicio

```java
package com.integrador.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para MiServicio")
class MiServicioTest {

    @Mock
    private MiRepository miRepository;

    @InjectMocks
    private MiServicio miServicio;

    @BeforeEach
    void setUp() {
        // Configuración inicial
    }

    @Test
    @DisplayName("Debería hacer algo exitosamente")
    void testMetodo_Success() {
        // Arrange (Preparar)
        when(miRepository.findById(1L)).thenReturn(Optional.of(new MiEntidad()));

        // Act (Actuar)
        MiEntidad result = miServicio.obtenerPorId(1L);

        // Assert (Verificar)
        assertNotNull(result);
        verify(miRepository, times(1)).findById(1L);
    }
}
```

### Ejemplo: Prueba de Controlador

```java
package com.integrador.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MiController.class)
class MiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MiServicio miServicio;

    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.campo").value("valor"));
    }
}
```

## Anotaciones Importantes

- `@ExtendWith(MockitoExtension.class)`: Habilita Mockito para mocks
- `@Mock`: Crea un mock de una dependencia
- `@InjectMocks`: Crea instancia e inyecta los mocks
- `@BeforeEach`: Se ejecuta antes de cada prueba
- `@DisplayName`: Nombre descriptivo para la prueba
- `@WebMvcTest`: Prueba solo la capa web (controladores)
- `@SpringBootTest`: Prueba completa de Spring Boot

## Buenas Pruebas

1. **Nombres descriptivos**: Usa `@DisplayName` para describir qué prueba
2. **Arrange-Act-Assert**: Organiza tu código en estas 3 secciones
3. **Una aserción por prueba**: Idealmente, prueba una cosa a la vez
4. **Mocks apropiados**: Mockea dependencias externas (BD, APIs, etc.)
5. **Casos límite**: Prueba casos exitosos Y casos de error

## Cobertura de Código

Para ver la cobertura de código:

```bash
mvn test jacoco:report
```

El reporte estará en: `target/site/jacoco/index.html`

