package com.integrador.dto;

public class SearchResponse {
    private boolean success;
    private VehicleInfo data;
    private String message;

    public SearchResponse(boolean success, VehicleInfo data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static class VehicleInfo {
        private String licensePlate;
        private String brand;
        private String model;
        private Integer year;
        private String description;

        // Nuevos campos específicos de Perú
        private String owner;
        private String vin;
        private String imageUrl;
        private String use;
        private String deliveryPoint;
        private String registrationDate;

        // Campos técnicos
        private String engineSize;
        private String fuelType;
        private String numberOfSeats;
        private String transmission;
        private String bodyStyle;

        // Getters y Setters para todos los campos
        public String getLicensePlate() { return licensePlate; }
        public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public String getVin() { return vin; }
        public void setVin(String vin) { this.vin = vin; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getUse() { return use; }
        public void setUse(String use) { this.use = use; }

        public String getDeliveryPoint() { return deliveryPoint; }
        public void setDeliveryPoint(String deliveryPoint) { this.deliveryPoint = deliveryPoint; }

        public String getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

        public String getEngineSize() { return engineSize; }
        public void setEngineSize(String engineSize) { this.engineSize = engineSize; }

        public String getFuelType() { return fuelType; }
        public void setFuelType(String fuelType) { this.fuelType = fuelType; }

        public String getNumberOfSeats() { return numberOfSeats; }
        public void setNumberOfSeats(String numberOfSeats) { this.numberOfSeats = numberOfSeats; }

        public String getTransmission() { return transmission; }
        public void setTransmission(String transmission) { this.transmission = transmission; }

        public String getBodyStyle() { return bodyStyle; }
        public void setBodyStyle(String bodyStyle) { this.bodyStyle = bodyStyle; }
    }

    // Getters y Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public VehicleInfo getData() { return data; }
    public void setData(VehicleInfo data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
