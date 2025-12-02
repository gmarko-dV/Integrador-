package com.tecsup.checkauto.service

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
            
            // Crear SOAP request
            val soapRequest = crearSOAPRequest(placaLimpia, ApiConfig.PLACA_API_USERNAME)
            
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
            
            if (response.isSuccessful) {
                val soapResponse = response.body?.string() ?: ""
                val jsonContent = extraerJSONDelSOAP(soapResponse)
                
                // Parsear JSON a Vehiculo
                val vehiculo = parseJsonToVehiculo(jsonContent, placaLimpia)
                Result.success(vehiculo)
            } else {
                Result.failure(Exception("Error HTTP: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

