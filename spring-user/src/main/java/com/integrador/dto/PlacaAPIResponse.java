package com.integrador.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacaAPIResponse {

    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public String getPropertyAsString(String name) {
        Object value = properties.get(name);
        if (value == null) return null;

        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Map) {
            // Si es un objeto, buscar CurrentTextValue u otros campos
            Map<?, ?> map = (Map<?, ?>) value;
            Object textValue = map.get("CurrentTextValue");
            if (textValue != null) return textValue.toString();

            // Si no hay CurrentTextValue, devolver el primer valor string que encuentre
            for (Object val : map.values()) {
                if (val instanceof String) {
                    return (String) val;
                }
            }
        }
        return value.toString();
    }

    // Métodos helper para propiedades específicas de Perú
    public String getDescription() {
        return getPropertyAsString("Description");
    }

    public String getRegistrationYear() {
        return getPropertyAsString("RegistrationYear");
    }

    public String getCarMake() {
        String make = getPropertyAsString("CarMake");
        if (make == null) {
            make = getPropertyAsString("Make");
        }
        return make;
    }

    public String getCarModel() {
        String model = getPropertyAsString("CarModel");
        if (model == null) {
            model = getPropertyAsString("Model");
        }
        return model;
    }

    public String getMakeDescription() {
        return getPropertyAsString("MakeDescription");
    }

    public String getModelDescription() {
        return getPropertyAsString("ModelDescription");
    }

    public String getOwner() {
        return getPropertyAsString("Owner");
    }

    public String getVIN() {
        return getPropertyAsString("VIN");
    }

    public String getImageUrl() {
        return getPropertyAsString("ImageUrl");
    }

    public String getUse() {
        return getPropertyAsString("Use");
    }

    public String getDeliveryPoint() {
        return getPropertyAsString("DeliveryPoint");
    }

    public String getDate() {
        return getPropertyAsString("Date");
    }

    public String getEngineSize() {
        return getPropertyAsString("EngineSize");
    }

    public String getFuelType() {
        return getPropertyAsString("FuelType");
    }

    public String getNumberOfSeats() {
        return getPropertyAsString("NumberOfSeats");
    }

    @Override
    public String toString() {
        return "PlacaAPIResponse{" + "properties=" + properties + '}';
    }
}
