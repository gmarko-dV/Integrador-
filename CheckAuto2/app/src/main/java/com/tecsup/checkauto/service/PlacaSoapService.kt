package com.tecsup.checkauto.service

import android.util.Log
import com.tecsup.checkauto.config.ApiConfig
import com.tecsup.checkauto.model.Vehiculo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class PlacaSoapService {
    private val client = OkHttpClient()
    private val soapMediaType = "text/xml; charset=utf-8".toMediaType()

    suspend fun searchPlateDirect(plateNumber: String): Result<Vehiculo> = withContext(Dispatchers.IO) {
        try {
            val placaLimpia = plateNumber.uppercase().replace("-", "").replace(" ", "")
            Log.d("PlacaSoapService", "Buscando placa: $placaLimpia")
            
            // Crear SOAP request
            val soapRequest = crearSOAPRequest(placaLimpia, ApiConfig.PLACA_API_USERNAME)
            Log.d("PlacaSoapService", "SOAP Request: $soapRequest")
            Log.d("PlacaSoapService", "URL: ${ApiConfig.PLACA_API_URL}")
            
            // Crear HTTP request
            val requestBody = soapRequest.toRequestBody(soapMediaType)
            val request = Request.Builder()
                .url(ApiConfig.PLACA_API_URL)
                .post(requestBody)
                .addHeader("Content-Type", soapMediaType.toString())
                .addHeader("SOAPAction", "http://regcheck.org.uk/CheckPeru")
                .build()
            
            // Ejecutar request
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            Log.d("PlacaSoapService", "Response Code: ${response.code}")
            Log.d("PlacaSoapService", "Response Body: ${responseBody.take(500)}") // Primeros 500 caracteres
            
            if (response.isSuccessful) {
                try {
                    val jsonContent = extraerJSONDelSOAP(responseBody)
                    Log.d("PlacaSoapService", "JSON extraído: $jsonContent")
                    
                    // Parsear JSON a Vehiculo
                    val vehiculo = parseJsonToVehiculo(jsonContent, placaLimpia)
                    Result.success(vehiculo)
                } catch (parseException: Exception) {
                    Log.e("PlacaSoapService", "Error parseando respuesta: ${parseException.message}", parseException)
                    Result.failure(Exception("Error al procesar la respuesta de la API: ${parseException.message}"))
                }
            } else {
                // Intentar extraer el mensaje de error del SOAP fault
                val soapError = extraerErrorDelSOAP(responseBody)
                
                val errorMsg = when {
                    response.code == 500 -> {
                        when {
                            soapError.contains("Out of credit", ignoreCase = true) -> {
                                "El servicio de búsqueda de placas no está disponible temporalmente debido a falta de créditos en la cuenta de la API. Por favor, contacta al administrador o intenta más tarde."
                            }
                            soapError.contains("Peru Lookup failed", ignoreCase = true) -> {
                                "No se pudo encontrar información para esta placa. Verifica que la placa sea válida."
                            }
                            soapError.isNotEmpty() -> {
                                "Error de la API: $soapError. Verifica que la placa sea válida."
                            }
                            else -> {
                                "Error del servidor (500). La API de placas puede estar temporalmente no disponible. Intenta más tarde."
                            }
                        }
                    }
                    response.code == 401 -> "Error de autenticación (401). Verifica las credenciales de la API."
                    response.code == 404 -> "Endpoint no encontrado (404). Verifica la URL de la API."
                    else -> "Error HTTP ${response.code}: ${response.message}"
                }
                Log.e("PlacaSoapService", errorMsg)
                Log.e("PlacaSoapService", "Response body: $responseBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("PlacaSoapService", "Excepción al buscar placa: ${e.message}", e)
            Result.failure(Exception("Error de conexión: ${e.message}. Verifica tu conexión a internet."))
        }
    }
    
    private fun crearSOAPRequest(placa: String, username: String): String {
        return """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CheckPeru xmlns="http://regcheck.org.uk">
      <RegistrationNumber>$placa</RegistrationNumber>
      <username>$username</username>
    </CheckPeru>
  </soap:Body>
</soap:Envelope>"""
    }
    
    private fun extraerJSONDelSOAP(soapResponse: String): String {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(soapResponse))
            val document = builder.parse(inputSource)
            
            // Buscar vehicleJson
            var nodeList = document.getElementsByTagName("vehicleJson")
            if (nodeList == null || nodeList.length == 0) {
                nodeList = document.getElementsByTagName("VehicleJson")
            }
            
            if (nodeList != null && nodeList.length > 0) {
                var jsonContent = nodeList.item(0).textContent
                // Limpiar caracteres XML
                jsonContent = jsonContent.replace("&quot;", "\"")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&apos;", "'")
                return jsonContent.trim()
            }
            
            // Si no se encuentra, buscar manualmente en el texto
            if (soapResponse.contains("\"Description\"") || soapResponse.contains("\"CarMake\"")) {
                // Buscar el inicio del JSON
                val startIndex = soapResponse.indexOf("{")
                val lastIndex = soapResponse.lastIndexOf("}")
                if (startIndex != -1 && lastIndex != -1 && lastIndex > startIndex) {
                    var jsonContent = soapResponse.substring(startIndex, lastIndex + 1)
                    // Limpiar caracteres XML
                    jsonContent = jsonContent.replace("&quot;", "\"")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&apos;", "'")
                    return jsonContent.trim()
                }
            }
            
            throw Exception("No se encontró JSON en la respuesta SOAP")
        } catch (e: Exception) {
            throw Exception("Error parseando respuesta SOAP: ${e.message}")
        }
    }
    
    private fun extraerErrorDelSOAP(soapResponse: String): String {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(soapResponse))
            val document = builder.parse(inputSource)
            
            // Buscar faultstring en el SOAP fault
            val faultStringNodes = document.getElementsByTagName("faultstring")
            if (faultStringNodes.length > 0) {
                faultStringNodes.item(0).textContent.trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            // Si no se puede parsear, buscar manualmente
            val faultStringIndex = soapResponse.indexOf("<faultstring>")
            if (faultStringIndex != -1) {
                val start = faultStringIndex + "<faultstring>".length
                val end = soapResponse.indexOf("</faultstring>", start)
                if (end != -1) {
                    soapResponse.substring(start, end).trim()
                } else {
                    ""
                }
            } else {
                ""
            }
        }
    }
    
    private fun parseJsonToVehiculo(jsonContent: String, placa: String): Vehiculo {
        // Parsear JSON usando Gson
        val jsonParser = com.google.gson.JsonParser()
        val jsonObject = try {
            jsonParser.parse(jsonContent).asJsonObject
        } catch (e: Exception) {
            // Si falla, crear objeto vacío
            com.google.gson.JsonObject()
        }
        
        return Vehiculo(
            placa = placa,
            marca = jsonObject.get("CarMake")?.asJsonObject?.get("CurrentTextValue")?.asString
                ?: jsonObject.get("Make")?.asString,
            modelo = jsonObject.get("CarModel")?.asJsonObject?.get("CurrentTextValue")?.asString
                ?: jsonObject.get("Model")?.asString,
            anio_registro_api = jsonObject.get("RegistrationYear")?.asString,
            vin = jsonObject.get("VIN")?.asString,
            uso = jsonObject.get("Use")?.asString,
            propietario = jsonObject.get("Owner")?.asString,
            fecha_registro_api = jsonObject.get("Date")?.asString,
            delivery_point = jsonObject.get("DeliveryPoint")?.asString,
            descripcion_api = jsonObject.get("Description")?.asString,
            image_url_api = jsonObject.get("ImageUrl")?.asString
        )
    }
}

