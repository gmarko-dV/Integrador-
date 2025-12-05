package com.tecsup.checkauto.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var titulo by remember { mutableStateOf("") }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1F2E), // Azul muy oscuro que complementa el header
                        Color(0xFF0F1419)  // Negro azulado más suave
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // Header con gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0066CC),
                            Color(0xFF0052A3)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "Publicar Auto en Venta",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Completa el formulario para publicar tu vehículo",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 0.2.sp
                )
            }
        }
        
        // Contenido del formulario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            // Mensajes de éxito/error mejorados
            if (success) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF4CAF50).copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("✅", fontSize = 24.sp)
                        Text(
                            text = "¡Anuncio publicado exitosamente!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            if (error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    color = Color(0xFFFF6B6B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFFF6B6B).copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("❌", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Error",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = error ?: "",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Formulario mejorado con glassmorphism
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Nombre del Auto *", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ej: Toyota Corolla 2020", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                singleLine = true,
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF0066CC)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF0066CC),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )
            
            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it },
                label = { Text("Modelo *", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ej: Corolla", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                singleLine = true,
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF0066CC)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF0066CC),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = anio,
                    onValueChange = { anio = it },
                    label = { Text("Año *", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ej: 2020", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = Color(0xFF0066CC),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF0066CC),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                OutlinedTextField(
                    value = kilometraje,
                    onValueChange = { kilometraje = it },
                    label = { Text("Kilometraje *", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ej: 50000", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = Color(0xFF0066CC),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF0066CC),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio (S/) *", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ej: 35000", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                singleLine = true,
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF0066CC)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF0066CC),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )

            // Tipo de vehículo mejorado
            var expandedTipo by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = !expandedTipo },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                OutlinedTextField(
                    value = tipoVehiculo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría de Vehículo *", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Selecciona una categoría", color = Color.White.copy(alpha = 0.5f)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = Color(0xFF0066CC),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF0066CC),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
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
                label = { Text("Descripción *", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Describe tu vehículo...", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                minLines = 4,
                maxLines = 6,
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF0066CC),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )

            OutlinedTextField(
                value = emailContacto,
                onValueChange = { emailContacto = it },
                label = { Text("Email de Contacto", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ej: vendedor@ejemplo.com", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                singleLine = true,
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0066CC)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF0066CC),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )

            OutlinedTextField(
                value = telefonoContacto,
                onValueChange = { telefonoContacto = it },
                label = { Text("Teléfono de Contacto", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ej: +51 987 654 321", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                singleLine = true,
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0066CC)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color(0xFF0066CC),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF0066CC),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                )
            )

            // Selección de imágenes mejorada
            Text(
                text = "Imágenes *",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.3.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Se requieren 2 imágenes",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // Botón de envío mejorado
            Button(
                onClick = {
                // Validaciones
                                when {
                                    titulo.isBlank() -> error = "El nombre del auto es requerido"
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
                                    titulo = titulo,
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
                                var imagenesGuardadas = 0
                                
                                imagenesUri.forEachIndexed { index, uri ->
                                    try {
                                        android.util.Log.d("PublicarAutoScreen", "Procesando imagen ${index + 1} de ${imagenesUri.size}")
                                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                                        val imageBytes = inputStream?.readBytes()
                                        
                                        if (imageBytes == null) {
                                            throw Exception("No se pudo leer la imagen ${index + 1}")
                                        }
                                        
                                        android.util.Log.d("PublicarAutoScreen", "Imagen ${index + 1} leída: ${imageBytes.size} bytes")
                                        
                                        // Obtener MIME type y extensión, soportando todos los tipos de imágenes
                                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                                        val extension = when {
                                            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                                            mimeType.contains("png") -> "png"
                                            mimeType.contains("webp") -> "webp"
                                            mimeType.contains("gif") -> "gif"
                                            mimeType.contains("bmp") -> "bmp"
                                            mimeType.contains("heic") || mimeType.contains("heif") -> "heic"
                                            mimeType.contains("svg") -> "svg"
                                            else -> {
                                                // Intentar extraer extensión del MIME type
                                                val mimeExtension = mimeType.split("/").lastOrNull() ?: "jpg"
                                                // Si el MIME type no tiene extensión válida, usar jpg por defecto
                                                if (mimeExtension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")) {
                                                    mimeExtension
                                                } else {
                                                    "jpg"
                                                }
                                            }
                                        }
                                        val fileName = "${anuncioCreado.id_anuncio}/imagen${index + 1}.${extension}"
                                        
                                        // Obtener nombre del archivo original si está disponible
                                        val originalFileName = try {
                                            val cursor = context.contentResolver.query(uri, null, null, null, null)
                                            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                            val name = if (nameIndex != null && nameIndex >= 0) {
                                                cursor?.moveToFirst()
                                                cursor?.getString(nameIndex)
                                            } else null
                                            cursor?.close()
                                            name ?: "imagen${index + 1}.${extension}"
                                        } catch (e: Exception) {
                                            "imagen${index + 1}.${extension}"
                                        }
                                        
                                        android.util.Log.d("PublicarAutoScreen", "Subiendo imagen ${index + 1} a Supabase Storage: $fileName")
                                        val imageUrl = SupabaseService.uploadImage(
                                            SupabaseConfig.STORAGE_BUCKET_ANUNCIOS,
                                            fileName,
                                            imageBytes
                                        )
                                        imagenesUrls.add(imageUrl)
                                        android.util.Log.d("PublicarAutoScreen", "Imagen ${index + 1} subida exitosamente. URL: $imageUrl")
                                        
                                        // Guardar referencia en la tabla de imágenes con todos los campos
                                        android.util.Log.d("PublicarAutoScreen", "Guardando imagen ${index + 1} en BD para anuncio ${anuncioCreado.id_anuncio}")
                                        val imagenGuardada = SupabaseService.addImagen(
                                            com.tecsup.checkauto.service.ImagenSupabase(
                                                id_anuncio = anuncioCreado.id_anuncio ?: 0,
                                                url_imagen = imageUrl,
                                                nombre_archivo = originalFileName,
                                                tipo_archivo = mimeType,
                                                tamano_archivo = imageBytes.size.toLong(),
                                                orden = index + 1 // Orden empieza en 1
                                            )
                                        )
                                        imagenesGuardadas++
                                        android.util.Log.d("PublicarAutoScreen", "✅ Imagen ${index + 1} guardada exitosamente en BD con ID: ${imagenGuardada.id_imagen}")
                                    } catch (e: Exception) {
                                        android.util.Log.e("PublicarAutoScreen", "❌ Error al procesar imagen ${index + 1}: ${e.message}", e)
                                        error = "Error al procesar imagen ${index + 1}: ${e.message}"
                                        throw e // Re-lanzar para detener el proceso
                                    }
                                }
                                
                                if (imagenesGuardadas == 0) {
                                    throw Exception("No se pudieron guardar las imágenes. Verifica los permisos del bucket en Supabase.")
                                }
                                
                                android.util.Log.d("PublicarAutoScreen", "✅ Total de imágenes guardadas: $imagenesGuardadas de ${imagenesUri.size}")
                                
                                success = true
                                isLoading = false
                                
                                // Limpiar formulario
                                titulo = ""
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
                    .height(60.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066CC)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 4.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Publicando...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Publicar Anuncio",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
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
    Surface(
        modifier = modifier
            .height(160.dp)
            .clickable(enabled = enabled) {
                onSelectClick()
            },
        color = if (imageUri != null) 
            Color.White.copy(alpha = 0.1f) 
        else 
            Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (imageUri != null)
                Color(0xFF0066CC).copy(alpha = 0.4f)
            else
                Color.White.copy(alpha = 0.15f)
        )
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
                            .allowHardware(false) // Permitir todos los formatos de imagen
                            .build()
                    ),
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                // Overlay con check
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Seleccionada",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = Color(0xFF0066CC).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp),
                            tint = Color(0xFF0066CC)
                        )
                    }
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Toca para seleccionar",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
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
