package com.integrador.service;

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
    private final String USERNAME = "flavv";

    private final RestTemplate restTemplate;

    public PlacaAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String consultarPlacaReal(String placa) {
        try {
            String placaLimpia = limpiarPlaca(placa);

            System.out.println("=== CONSULTANDO PLACA: " + placaLimpia + " ===");

            // Crear XML SOAP request
            String soapRequest = crearSOAPRequest(placaLimpia, USERNAME);

            // Configurar headers para SOAP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "http://regcheck.org.uk/CheckPeru");

            HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

            System.out.println("Enviando request SOAP...");

            // Enviar request SOAP
            ResponseEntity<String> response = restTemplate.exchange(
                    SOAP_URL, HttpMethod.POST, entity, String.class);

            System.out.println("Response Status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                String soapResponse = response.getBody();
                System.out.println("SOAP Response recibido, longitud: " +
                        (soapResponse != null ? soapResponse.length() : 0) + " caracteres");

                // Extraer el JSON del XML SOAP
                return extraerJSONDelSOAP(soapResponse);
            } else {
                throw new RuntimeException("Error en la API SOAP: " + response.getStatusCode());
            }

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

            System.out.println("JSON extraído: " + jsonContent);
            return jsonContent;

        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta SOAP: " + e.getMessage(), e);
        }
    }

    private String limpiarPlaca(String placa) {
        return placa.toUpperCase().replace("-", "").replace(" ", "");
    }
}
