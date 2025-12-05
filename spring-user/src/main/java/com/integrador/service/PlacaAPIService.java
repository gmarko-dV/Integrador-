package com.integrador.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class PlacaAPIService {

    private final String SOAP_URL = "https://www.placaapi.pe/api/reg.asmx";
    
    @Value("${placa.api.username:jhon123}")
    private String username;

    private final RestTemplate restTemplate;

    public PlacaAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String consultarPlacaReal(String placa) {
        try {
            String placaLimpia = limpiarPlaca(placa);

            System.out.println("=== CONSULTANDO PLACA: " + placaLimpia + " ===");
            System.out.println("Username configurado: " + username);
            System.out.println("URL de la API: " + SOAP_URL);

            // Crear XML SOAP request
            String soapRequest = crearSOAPRequest(placaLimpia, username);

            // Configurar headers para SOAP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "http://regcheck.org.uk/CheckPeru");

            HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

            System.out.println("Enviando request SOAP...");

            // Enviar request SOAP
            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(
                        SOAP_URL, HttpMethod.POST, entity, String.class);
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // Si hay un error HTTP 500, intentar extraer el mensaje del body
                String errorBody = e.getResponseBodyAsString();
                System.err.println("Error HTTP 500 recibido. Body: " + errorBody);
                
                if (errorBody != null && errorBody.contains("<soap:Fault>")) {
                    String errorMessage = extraerMensajeErrorSOAP(errorBody);
                    throw new RuntimeException(crearMensajeErrorPersonalizado(errorMessage, placaLimpia));
                }
                throw new RuntimeException("Error del servidor al consultar la API de placas: " + e.getMessage());
            } catch (org.springframework.web.client.RestClientException e) {
                throw new RuntimeException("Error de conexión con la API de placas: " + e.getMessage());
            }

            System.out.println("Response Status: " + response.getStatusCode());

            String soapResponse = response.getBody();
            System.out.println("SOAP Response recibido, longitud: " +
                    (soapResponse != null ? soapResponse.length() : 0) + " caracteres");
            
            if (soapResponse != null) {
                System.out.println("SOAP Response (primeros 1000 chars): " + 
                        (soapResponse.length() > 1000 ? soapResponse.substring(0, 1000) + "..." : soapResponse));
            }

            // Verificar si la respuesta contiene un SOAP Fault (incluso si el HTTP status es 200)
            if (soapResponse != null && (soapResponse.contains("<soap:Fault>") || soapResponse.contains("soap:Fault"))) {
                String errorMessage = extraerMensajeErrorSOAP(soapResponse);
                System.err.println("Error SOAP detectado: " + errorMessage);
                throw new RuntimeException(crearMensajeErrorPersonalizado(errorMessage, placaLimpia));
            }

            if (response.getStatusCode() == HttpStatus.OK) {
                // Verificar si la respuesta contiene errores de autenticación o tokens
                if (soapResponse != null) {
                    String responseLower = soapResponse.toLowerCase();
                    if (responseLower.contains("invalid") || responseLower.contains("unauthorized") ||
                        responseLower.contains("token") || responseLower.contains("credential") ||
                        responseLower.contains("authentication") || responseLower.contains("forbidden")) {
                        throw new RuntimeException("Error de autenticación con la API de placas. Verifica las credenciales configuradas.");
                    }
                    if (responseLower.contains("no se encontró") || responseLower.contains("not found") ||
                        responseLower.contains("no existe") || responseLower.contains("does not exist")) {
                        throw new RuntimeException("La placa " + placaLimpia + " no fue encontrada en el sistema.");
                    }
                }

                // Extraer el JSON del XML SOAP
                return extraerJSONDelSOAP(soapResponse);
            } else {
                throw new RuntimeException("Error en la API SOAP: " + response.getStatusCode() + 
                        (soapResponse != null ? ". Respuesta: " + soapResponse.substring(0, Math.min(200, soapResponse.length())) : ""));
            }

        } catch (RuntimeException e) {
            // Re-lanzar RuntimeException tal cual
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error consultando placa: " + e.getMessage(), e);
        }
    }

    private String crearSOAPRequest(String placa, String username) {
        String soapRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<CheckPeru xmlns=\"http://regcheck.org.uk\">" +
                "<RegistrationNumber>" + placa + "</RegistrationNumber>" +
                "<username>" + username + "</username>" +
                "</CheckPeru>" +
                "</soap:Body>" +
                "</soap:Envelope>";

        System.out.println("SOAP Request: " + soapRequest);
        return soapRequest;
    }

    private String extraerJSONDelSOAP(String soapResponse) {
        try {
            if (soapResponse == null || soapResponse.isEmpty()) {
                throw new RuntimeException("Respuesta SOAP vacía");
            }

            System.out.println("Parseando SOAP response...");

            // Parsear XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(soapResponse)));

            // Buscar el elemento vehicleJson
            NodeList nodeList = document.getElementsByTagName("vehicleJson");
            if (nodeList.getLength() == 0) {
                // Intentar con otro nombre posible
                nodeList = document.getElementsByTagName("VehicleJson");
                if (nodeList.getLength() == 0) {
                    System.out.println("No se encontró vehicleJson, buscando cualquier texto JSON...");
                    // Buscar manualmente en el texto
                    if (soapResponse.contains("\"Description\"")) {
                        int start = soapResponse.indexOf("\"Description\"");
                        int end = soapResponse.indexOf("}", start) + 1;
                        if (start != -1 && end != -1) {
                            String jsonContent = soapResponse.substring(start - 1, end);
                            System.out.println("JSON encontrado manualmente: " + jsonContent);
                            return jsonContent;
                        }
                    }
                    throw new RuntimeException("No se encontró JSON en la respuesta SOAP");
                }
            }

            String jsonContent = nodeList.item(0).getTextContent();

            // Limpiar el JSON de caracteres XML
            jsonContent = jsonContent.replace("&quot;", "\"")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&apos;", "'");

            // Limpiar espacios en blanco al inicio y final
            jsonContent = jsonContent.trim();

            System.out.println("JSON extraído (primeros 500 chars): " + 
                    (jsonContent.length() > 500 ? jsonContent.substring(0, 500) + "..." : jsonContent));
            
            // Verificar que el JSON sea válido
            if (jsonContent.isEmpty()) {
                throw new RuntimeException("El JSON extraído está vacío");
            }
            
            if (!jsonContent.startsWith("{") && !jsonContent.startsWith("[")) {
                System.err.println("JSON inválido extraído: " + jsonContent);
                throw new RuntimeException("El JSON extraído no tiene un formato válido");
            }

            return jsonContent;

        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta SOAP: " + e.getMessage(), e);
        }
    }

    private String limpiarPlaca(String placa) {
        return placa.toUpperCase().replace("-", "").replace(" ", "");
    }
    
    private String extraerMensajeErrorSOAP(String soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(soapResponse)));
            
            // Buscar el elemento faultstring (con y sin namespace)
            NodeList faultStringList = document.getElementsByTagName("faultstring");
            if (faultStringList.getLength() == 0) {
                // Intentar con namespace
                faultStringList = document.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "faultstring");
            }
            if (faultStringList.getLength() > 0) {
                String faultString = faultStringList.item(0).getTextContent();
                // Limpiar caracteres XML
                faultString = faultString.replace("&quot;", "\"")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&apos;", "'");
                return faultString.trim();
            }
            
            // Si no hay faultstring, buscar en faultcode
            NodeList faultCodeList = document.getElementsByTagName("faultcode");
            if (faultCodeList.getLength() > 0) {
                return faultCodeList.item(0).getTextContent().trim();
            }
            
            return "Error desconocido en la respuesta SOAP";
        } catch (Exception e) {
            // Si no se puede parsear, buscar manualmente
            String lowerResponse = soapResponse.toLowerCase();
            if (lowerResponse.contains("faultstring")) {
                // Buscar con diferentes variantes
                String[] patterns = {"<faultstring>", "<soap:faultstring>", "faultstring>"};
                for (String pattern : patterns) {
                    int start = soapResponse.indexOf(pattern);
                    if (start != -1) {
                        start += pattern.length();
                        int end = soapResponse.indexOf("</", start);
                        if (end == -1) {
                            end = soapResponse.indexOf("<", start);
                        }
                        if (end != -1 && end > start) {
                            String faultString = soapResponse.substring(start, end).trim();
                            // Limpiar caracteres XML
                            faultString = faultString.replace("&quot;", "\"")
                                    .replace("&amp;", "&")
                                    .replace("&lt;", "<")
                                    .replace("&gt;", ">")
                                    .replace("&apos;", "'");
                            return faultString;
                        }
                    }
                }
            }
            return "Error al procesar la respuesta de error de la API";
        }
    }
    
    private String crearMensajeErrorPersonalizado(String errorMessage, String placa) {
        String errorLower = errorMessage.toLowerCase();
        
        if (errorLower.contains("peru lookup failed")) {
            return "La búsqueda de la placa '" + placa + "' falló. " +
                   "Posibles causas:\n" +
                   "1. La placa no existe en el sistema de registro\n" +
                   "2. El usuario '" + username + "' no tiene créditos suficientes\n" +
                   "3. Problema temporal con el servicio de búsqueda\n\n" +
                   "Por favor, verifica:\n" +
                   "- Que la placa esté correctamente escrita\n" +
                   "- Que el usuario tenga créditos disponibles en placaapi.pe\n" +
                   "- Intenta con otra placa conocida";
        } else if (errorLower.contains("invalid") || 
                  errorLower.contains("unauthorized") ||
                  errorLower.contains("credential") ||
                  errorLower.contains("authentication")) {
            return "Error de autenticación con la API de placas.\n\n" +
                   "El usuario '" + username + "' puede no tener créditos disponibles o las credenciales son incorrectas.\n" +
                   "Por favor, verifica en el dashboard de placaapi.pe que el usuario tenga créditos suficientes.";
        } else {
            return "Error en la API de placas: " + errorMessage + "\n\n" +
                   "Placa consultada: " + placa + "\n" +
                   "Usuario: " + username;
        }
    }
}
