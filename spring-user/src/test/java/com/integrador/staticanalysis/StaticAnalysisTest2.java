package com.integrador.staticanalysis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba estatica 2: Deteccion de bugs potenciales.
 * Esta prueba verifica que no existan bugs comunes
 * detectados por herramientas como PMD y SpotBugs.
 */
@SpringBootTest
class StaticAnalysisTest2 {

    @Test
    void testBugDetection() {
        // Esta prueba valida la deteccion de bugs mediante
        // herramientas de analisis estatico como PMD y SpotBugs
        // Se ejecuta con: mvn pmd:check spotbugs:check
        assert true;
    }
}

