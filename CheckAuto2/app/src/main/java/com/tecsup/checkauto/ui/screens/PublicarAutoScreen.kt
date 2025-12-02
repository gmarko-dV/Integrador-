package com.tecsup.checkauto.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.util.regex.Pattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarAutoScreen(
    onSuccess: () -> Unit = {},
    isAuthenticated: Boolean = true
) {
    val context = LocalContext.current
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var emailContacto by remember { mutableStateOf("") }
    var telefonoContacto by remember { mutableStateOf("") }
    var tipoVehiculo by remember { mutableStateOf("") }
    var imagen1Uri by remember { mutableStateOf<Uri?>(null) }
    var imagen2Uri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val tiposVehiculo = listOf(
        "Hatchback",
        "Sedan",
        "Coupé",
        "SUV",
        "Station Wagon",
        "Deportivo"
    )

    if (!isAuthenticated) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🔒",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Debes iniciar sesión para publicar un auto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Publicar Auto en Venta",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Completa el formulario para publicar tu vehículo",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Mensajes de éxito/error
        if (success) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "¡Anuncio publicado exitosamente!",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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

        // Formulario
        OutlinedTextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Modelo *") },
            placeholder = { Text("Ej: Toyota Corolla") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = anio,
                onValueChange = { anio = it },
                label = { Text("Año *") },
                placeholder = { Text("Ej: 2020") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = kilometraje,
                onValueChange = { kilometraje = it },
                label = { Text("Kilometraje *") },
                placeholder = { Text("Ej: 50000") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio (S/) *") },
            placeholder = { Text("Ej: 35000") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
        )

        // Tipo de vehículo
        var expandedTipo by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedTipo,
            onExpandedChange = { expandedTipo = !expandedTipo },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = tipoVehiculo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría de Vehículo *") },
                placeholder = { Text("Selecciona una categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false }
            ) {
                tiposVehiculo.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            tipoVehiculo = tipo
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción *") },
            placeholder = { Text("Describe tu vehículo...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 4,
            maxLines = 6,
            enabled = !isLoading
        )

        OutlinedTextField(
            value = emailContacto,
            onValueChange = { emailContacto = it },
            label = { Text("Email de Contacto") },
            placeholder = { Text("Ej: vendedor@ejemplo.com") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        OutlinedTextField(
            value = telefonoContacto,
            onValueChange = { telefonoContacto = it },
            label = { Text("Teléfono de Contacto") },
            placeholder = { Text("Ej: +51 987 654 321") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )

        // Selección de imágenes
        Text(
            text = "Imágenes * (2 imágenes requeridas)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Imagen 1
            ImageSelector(
                imageUri = imagen1Uri,
                label = "Imagen 1",
                modifier = Modifier.weight(1f),
                onImageSelected = { uri ->
                    imagen1Uri = uri
                },
                enabled = !isLoading
            )

            // Imagen 2
            ImageSelector(
                imageUri = imagen2Uri,
                label = "Imagen 2",
                modifier = Modifier.weight(1f),
                onImageSelected = { uri ->
                    imagen2Uri = uri
                },
                enabled = !isLoading
            )
        }

        // Botón de envío
        Button(
            onClick = {
                // Validaciones
                when {
                    modelo.isBlank() -> error = "El modelo es requerido"
                    anio.isBlank() || anio.toIntOrNull() == null || anio.toInt() < 1900 || anio.toInt() > 2100 -> 
                        error = "El año debe ser válido (1900-2100)"
                    kilometraje.isBlank() || kilometraje.toIntOrNull() == null || kilometraje.toInt() < 0 -> 
                        error = "El kilometraje debe ser mayor o igual a 0"
                    precio.isBlank() || precio.toDoubleOrNull() == null || precio.toDouble() <= 0 -> 
                        error = "El precio debe ser mayor a 0"
                    descripcion.isBlank() -> error = "La descripción es requerida"
                    tipoVehiculo.isBlank() -> error = "Debes seleccionar una categoría de vehículo"
                    emailContacto.isBlank() && telefonoContacto.isBlank() -> 
                        error = "Debes proporcionar al menos un método de contacto (email o teléfono)"
                    emailContacto.isNotBlank() && !isValidEmail(emailContacto) -> 
                        error = "El formato del email no es válido"
                    imagen1Uri == null || imagen2Uri == null -> error = "Se requieren 2 imágenes"
                    else -> {
                        isLoading = true
                        error = null
                        success = false
                        
                        // Simular publicación (más adelante será una llamada real a la API)
                        scope.launch {
                            delay(2000)
                            success = true
                            isLoading = false
                            
                            // Limpiar formulario
                            modelo = ""
                            anio = ""
                            kilometraje = ""
                            precio = ""
                            descripcion = ""
                            emailContacto = ""
                            telefonoContacto = ""
                            tipoVehiculo = ""
                            imagen1Uri = null
                            imagen2Uri = null
                            
                            onSuccess()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publicando...")
            } else {
                Text("Publicar Anuncio")
            }
        }
    }
}

@Composable
fun ImageSelector(
    imageUri: Uri?,
    label: String,
    modifier: Modifier = Modifier,
    onImageSelected: (Uri) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable(enabled = enabled) {
                // TODO: Abrir selector de imágenes
                // Por ahora, simular selección
                // En producción, usar ActivityResultLauncher para seleccionar imagen
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imageUri)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        Pattern.CASE_INSENSITIVE
    )
    return emailPattern.matcher(email).matches()
}
