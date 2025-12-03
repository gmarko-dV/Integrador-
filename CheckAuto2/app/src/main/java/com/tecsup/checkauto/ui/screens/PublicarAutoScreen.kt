package com.tecsup.checkauto.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.tecsup.checkauto.service.SupabaseService
import com.tecsup.checkauto.service.SupabaseAuthService
import com.tecsup.checkauto.service.ModelConverter
import com.tecsup.checkauto.config.SupabaseConfig
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarAutoScreen(
    onSuccess: () -> Unit = {},
    isAuthenticated: Boolean = true
) {
    val context = LocalContext.current
    
    // Variables de estado
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
    
    // Launchers para seleccionar imágenes desde la galería
    val launcherImagen1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imagen1Uri = it }
    }
    
    val launcherImagen2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imagen2Uri = it }
    }
    var isLoading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Cargar categorías desde Supabase
    var categoriasVehiculos by remember { mutableStateOf<List<com.tecsup.checkauto.service.CategoriaVehiculoSupabase>>(emptyList()) }
    var isLoadingCategorias by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        isLoadingCategorias = true
        try {
            categoriasVehiculos = SupabaseService.getCategoriasVehiculos()
            // Si no hay categorías, usar las por defecto
            if (categoriasVehiculos.isEmpty()) {
                categoriasVehiculos = listOf(
                    com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                        id_categoria = null,
                        nombre = "Hatchback",
                        codigo = "hatchback"
                    ),
                    com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                        id_categoria = null,
                        nombre = "Sedan",
                        codigo = "sedan"
                    ),
                    com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                        id_categoria = null,
                        nombre = "Coupé",
                        codigo = "coupe"
                    ),
                    com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                        id_categoria = null,
                        nombre = "SUV",
                        codigo = "suv"
                    ),
                    com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                        id_categoria = null,
                        nombre = "Station Wagon",
                        codigo = "station-wagon"
                    ),
                    com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                        id_categoria = null,
                        nombre = "Deportivo",
                        codigo = "deportivo"
                    )
                )
            }
            isLoadingCategorias = false
        } catch (e: Exception) {
            // En caso de error, usar categorías por defecto
            categoriasVehiculos = listOf(
                com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                    id_categoria = null,
                    nombre = "Hatchback",
                    codigo = "hatchback"
                ),
                com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                    id_categoria = null,
                    nombre = "Sedan",
                    codigo = "sedan"
                ),
                com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                    id_categoria = null,
                    nombre = "Coupé",
                    codigo = "coupe"
                ),
                com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                    id_categoria = null,
                    nombre = "SUV",
                    codigo = "suv"
                ),
                com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                    id_categoria = null,
                    nombre = "Station Wagon",
                    codigo = "station-wagon"
                ),
                com.tecsup.checkauto.service.CategoriaVehiculoSupabase(
                    id_categoria = null,
                    nombre = "Deportivo",
                    codigo = "deportivo"
                )
            )
            isLoadingCategorias = false
        }
    }

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
                if (isLoadingCategorias) {
                    DropdownMenuItem(
                        text = { 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            }
                        },
                        onClick = {}
                    )
                } else {
                    categoriasVehiculos.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                tipoVehiculo = categoria.nombre
                                expandedTipo = false
                            }
                        )
                    }
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
                onSelectClick = {
                    launcherImagen1.launch("image/*")
                },
                enabled = !isLoading
            )

            // Imagen 2
            ImageSelector(
                imageUri = imagen2Uri,
                label = "Imagen 2",
                modifier = Modifier.weight(1f),
                onSelectClick = {
                    launcherImagen2.launch("image/*")
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
                        
                        scope.launch {
                            try {
                                val userId = SupabaseAuthService.getCurrentUserId()
                                if (userId == null) {
                                    error = "Debes iniciar sesión para publicar"
                                    isLoading = false
                                    return@launch
                                }
                                
                                // Crear anuncio
                                val anuncio = com.tecsup.checkauto.model.Anuncio(
                                    modelo = modelo,
                                    anio = anio.toInt(),
                                    kilometraje = kilometraje.toInt(),
                                    precio = precio.toDouble(),
                                    descripcion = descripcion,
                                    emailContacto = emailContacto.takeIf { it.isNotBlank() },
                                    telefonoContacto = telefonoContacto.takeIf { it.isNotBlank() },
                                    tipoVehiculo = tipoVehiculo,
                                    idUsuario = userId
                                )
                                
                                val anuncioSupabase = ModelConverter.anuncioToAnuncioSupabase(anuncio)
                                val anuncioCreado = SupabaseService.createAnuncio(anuncioSupabase)
                                
                                // Subir imágenes
                                val imagenesUri = listOfNotNull(imagen1Uri, imagen2Uri)
                                val imagenesUrls = mutableListOf<String>()
                                
                                imagenesUri.forEachIndexed { index, uri ->
                                    try {
                                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                                        val imageBytes = inputStream?.readBytes()
                                        
                                        if (imageBytes != null) {
                                            val extension = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
                                            val fileName = "${anuncioCreado.id_anuncio}/imagen${index + 1}.${extension}"
                                            val imageUrl = SupabaseService.uploadImage(
                                                SupabaseConfig.STORAGE_BUCKET_ANUNCIOS,
                                                fileName,
                                                imageBytes
                                            )
                                            imagenesUrls.add(imageUrl)
                                            
                                            // Guardar referencia en la tabla de imágenes
                                            SupabaseService.addImagen(
                                                com.tecsup.checkauto.service.ImagenSupabase(
                                                    id_anuncio = anuncioCreado.id_anuncio ?: 0,
                                                    url_imagen = imageUrl,
                                                    orden = index
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        // Continuar aunque falle una imagen
                                        error = "Error al subir imagen ${index + 1}: ${e.message}"
                                    }
                                }
                                
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
                                
                                delay(1000)
                                onSuccess()
                            } catch (e: Exception) {
                                error = "Error al publicar: ${e.message}"
                                isLoading = false
                            }
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
    onSelectClick: () -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable(enabled = enabled) {
                onSelectClick()
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
