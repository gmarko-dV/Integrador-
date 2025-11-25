// Servicio para consultar la API externa de placas de Perú
class PlateApiService {
  constructor() {
    this.baseUrl = 'https://www.placaapi.pe/api/reg.asmx';
    this.username = process.env.REACT_APP_PLATE_API_USERNAME || 'your_username_here';
  }

  // Método para consultar la API de placas usando HTTP POST
  async checkPeruPlate(plateNumber) {
    try {
      const response = await fetch(`${this.baseUrl}/CheckPeru`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          RegistrationNumber: plateNumber,
          username: this.username
        })
      });

      if (!response.ok) {
        throw new Error(`Error HTTP: ${response.status}`);
      }

      const xmlText = await response.text();
      return this.parseXmlResponse(xmlText);
    } catch (error) {
      console.error('Error consultando API de placas:', error);
      throw new Error('No se pudo consultar la información de la placa. Verifica que la placa exista y esté registrada.');
    }
  }

  // Método para parsear la respuesta XML de la API
  parseXmlResponse(xmlText) {
    try {
      // Crear un parser XML
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlText, 'text/xml');
      
      // Verificar si hay errores en el XML
      const parseError = xmlDoc.getElementsByTagName('parsererror');
      if (parseError.length > 0) {
        throw new Error('Error al parsear la respuesta de la API');
      }

      // Extraer vehicleJson
      const vehicleJsonElement = xmlDoc.getElementsByTagName('vehicleJson')[0];
      if (!vehicleJsonElement || !vehicleJsonElement.textContent) {
        throw new Error('No se encontró información del vehículo');
      }

      // Parsear el JSON
      const vehicleData = JSON.parse(vehicleJsonElement.textContent);
      
      // Transformar los datos al formato de nuestra base de datos
      return this.transformVehicleData(vehicleData);
    } catch (error) {
      console.error('Error parseando respuesta XML:', error);
      throw new Error('Error al procesar la información del vehículo');
    }
  }

  // Transformar los datos de la API al formato de nuestra base de datos
  transformVehicleData(apiData) {
    return {
      placa: this.extractPlateFromData(apiData),
      descripcion_api: apiData.Description || null,
      marca: apiData.CarMake?.CurrentTextValue || apiData.Make || null,
      modelo: apiData.CarModel?.CurrentTextValue || apiData.Model || null,
      anio_registro_api: apiData.RegistrationYear || null,
      vin: apiData.VIN || null,
      uso: apiData.Use || null,
      propietario: apiData.Owner || null,
      delivery_point: apiData.DeliveryPoint || null,
      fecha_registro_api: apiData.Date ? new Date(apiData.Date) : null,
      image_url_api: apiData.ImageUrl || null,
      datos_api: apiData, // Guardar todos los datos originales como JSON
      fecha_actualizacion_api: new Date()
    };
  }

  // Extraer el número de placa de los datos (puede estar en diferentes campos)
  extractPlateFromData(data) {
    // La placa debería venir del parámetro de búsqueda, pero por si acaso la buscamos en los datos
    return data.RegistrationNumber || data.PlateNumber || 'N/A';
  }

  // Método alternativo usando SOAP (si es necesario)
  async checkPeruPlateSOAP(plateNumber) {
    const soapEnvelope = `<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CheckPeru xmlns="http://regcheck.org.uk">
      <RegistrationNumber>${plateNumber}</RegistrationNumber>
      <username>${this.username}</username>
    </CheckPeru>
  </soap:Body>
</soap:Envelope>`;

    try {
      const response = await fetch(this.baseUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'text/xml; charset=utf-8',
          'SOAPAction': 'http://regcheck.org.uk/CheckPeru'
        },
        body: soapEnvelope
      });

      if (!response.ok) {
        throw new Error(`Error HTTP: ${response.status}`);
      }

      const xmlText = await response.text();
      return this.parseXmlResponse(xmlText);
    } catch (error) {
      console.error('Error consultando API SOAP de placas:', error);
      throw new Error('No se pudo consultar la información de la placa');
    }
  }

  // Validar formato de placa peruana
  validatePlateFormat(plate) {
    const plateRegex = /^[A-Z]{3}[0-9]{3,4}$/;
    return plateRegex.test(plate.toUpperCase());
  }

  // Ahora el frontend usa únicamente el backend Spring Boot
  // que consulta la API real de placas de Perú.
}

export default new PlateApiService();
