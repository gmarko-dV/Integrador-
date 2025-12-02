package com.tecsup.checkauto.config

object ApiConfig {
    // URL del backend Spring Boot
    const val BASE_URL = "http://10.0.2.2:8080/api/" // 10.0.2.2 es el localhost del emulador Android
    
    // API de placas de Perú (SOAP) - llamada directa sin backend
    const val PLACA_API_URL = "https://www.placaapi.pe/api/reg.asmx"
    const val PLACA_API_USERNAME = "jhoncito" // API key/username para la API de placas
    
    // Para dispositivos físicos, usar la IP de tu máquina en lugar de 10.0.2.2
    // Ejemplo: const val BASE_URL = "http://192.168.1.X:8080/api/"
}
