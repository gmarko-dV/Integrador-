package com.tecsup.checkauto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.checkauto.model.Vehiculo
import com.tecsup.checkauto.service.PlacaSoapService
import kotlinx.coroutines.launch

@Composable
fun PlateSearchScreen() {
    var plateNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<Vehiculo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val placaSoapService = remember { PlacaSoapService() }
    
    // UserId temporal para la app móvil (en producción debería venir de autenticación)
    val userId = "mobile-user"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔍 Búsqueda de Placas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Formulario de búsqueda
        OutlinedTextField(
            value = plateNumber,
            onValueChange = { plateNumber = it.uppercase() },
            label = { Text("Número de Placa") },
            placeholder = { Text("Ej: ABC123, T3V213") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (plateNumber.isNotBlank()) {
                        // Validar formato
                        val plateRegex = Regex("^[A-Z0-9]{6,7}$")
                        if (plateRegex.matches(plateNumber)) {
                            isLoading = true
                            error = null
                            searchResult = null
                            
                            // Llamada real a la API
                            scope.launch {
                                val result = placaSoapService.searchPlateDirect(plateNumber)
                                result.onSuccess { vehiculo ->
                                    searchResult = vehiculo
                                    isLoading = false
                                }.onFailure { exception ->
                                    error = exception.message ?: "Error al buscar la placa. Verifica tu conexión a internet y que el backend esté ejecutándose."
                                    isLoading = false
                                }
                            }
                        } else {
                            error = "Formato inválido. Debe tener 6-7 caracteres (ej: ABC123)"
                        }
                    } else {
                        error = "Por favor ingresa un número de placa"
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && plateNumber.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscando...")
                } else {
                    Text("Buscar")
                }
            }

            if (plateNumber.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        plateNumber = ""
                        searchResult = null
                        error = null
                    }
                ) {
                    Text("Limpiar")
                }
            }
        }

        // Error
        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $error",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Resultado
        if (searchResult != null) {
            Spacer(modifier = Modifier.height(24.dp))
            VehicleDetailsCard(vehicle = searchResult!!)
        }
    }
}

@Composable
fun VehicleDetailsCard(vehicle: Vehiculo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 Información del Vehículo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Información básica
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.marca ?: "N/A"} ${vehicle.modelo ?: ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Año: ${vehicle.anio_registro_api ?: "N/A"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Placa: ${vehicle.placa}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // Especificaciones
            Spacer(modifier = Modifier.height(16.dp))
            VehicleSpecItem("VIN", vehicle.vin ?: "No disponible")
            VehicleSpecItem("Uso", vehicle.uso ?: "No disponible")
            VehicleSpecItem("Propietario", vehicle.propietario ?: "No disponible")
            VehicleSpecItem("Fecha de Registro", vehicle.fecha_registro_api ?: "No disponible")
            if (vehicle.delivery_point != null) {
                VehicleSpecItem("Punto de Entrega", vehicle.delivery_point)
            }

            if (vehicle.descripcion_api != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Descripción:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = vehicle.descripcion_api,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun VehicleSpecItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

