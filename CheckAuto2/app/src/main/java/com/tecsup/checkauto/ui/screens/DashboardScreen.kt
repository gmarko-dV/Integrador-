package com.tecsup.checkauto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAnuncios: (String?) -> Unit,
    onNavigateToBuscar: () -> Unit,
    onNavigateToPublicar: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToDetalleAnuncio: (Long) -> Unit = {},
    isAuthenticated: Boolean = false,
    userName: String? = null,
    userId: String? = null,
    onLogin: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showVehicleTypes by remember { mutableStateOf(true) }
    val context = LocalContext.current
    
    // Cargar cantidad de notificaciones no leídas
    var cantidadNoLeidas by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(userId) {
        if (userId != null && isAuthenticated) {
            scope.launch {
                try {
                    val notificacionesNoLeidas = com.tecsup.checkauto.service.SupabaseService.getUnreadNotificaciones(userId)
                    cantidadNoLeidas = notificacionesNoLeidas.size
                } catch (e: Exception) {
                    // Error silencioso
                }
            }
        }
    }

    // URL del fondo de carretera (igual que la web)
    val backgroundImageUrl = "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80"

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de imagen
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(backgroundImageUrl)
                    .crossfade(true)
                    .allowHardware(false) // Permitir todos los formatos de imagen
                    .build()
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header negro
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "checkAuto",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAuthenticated) {
                                // Notificaciones con badge
                                Box {
                                    IconButton(
                                        onClick = onNavigateToNotificaciones,
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF0066CC).copy(alpha = 0.2f),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Notificaciones",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    // Badge de notificaciones no leídas
                                    if (cantidadNoLeidas > 0) {
                                        Surface(
                                            color = Color(0xFFFF6B6B),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 8.dp, y = (-8).dp)
                                        ) {
                                            Text(
                                                text = if (cantidadNoLeidas > 99) "99+" else cantidadNoLeidas.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                // Perfil
                                IconButton(
                                    onClick = onNavigateToConfiguracion,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF0066CC).copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "Perfil",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    onClick = onLogin,
                                    color = Color(0xFF0066CC).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFF0066CC).copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        "Iniciar Sesión",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hero Section (solo si no hay tab seleccionado)
            if (selectedTab == 0 && showVehicleTypes) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "El portal confiable para la compra y venta de autos nuevos y usados en Perú.",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Text(
                        text = "En CheckAuto® encontrarás miles de vehículos disponibles en todas las ciudades del país, con precios competitivos y herramientas como nuestro buscador de placas para verificar la información del auto antes de decidir.",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )

                    // Botones de acción mejorados
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = {
                                selectedTab = 1
                                showVehicleTypes = false
                                onNavigateToBuscar()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            color = Color(0xFF0066CC),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Color(0xFF0052A3)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Buscar Autos",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                        Surface(
                            onClick = {
                                selectedTab = 2
                                showVehicleTypes = false
                                onNavigateToPublicar()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            color = Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Publicar Anuncio",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }
                }
            }

            // Tipos de vehículos (solo si no hay tab seleccionado)
            if (selectedTab == 0 && showVehicleTypes) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "BUSCAR POR TIPO DE VEHÍCULO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

    // Cargar categorías desde Supabase
    var categoriasVehiculos by remember { mutableStateOf<List<com.tecsup.checkauto.service.CategoriaVehiculoSupabase>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        try {
            categoriasVehiculos = com.tecsup.checkauto.service.SupabaseService.getCategoriasVehiculos()
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
        }
    }
    
    // Mapeo de emojis para tipos de vehículos (fallback si no hay imagen)
    val emojiMap = mapOf(
        "Hatchback" to "🚗",
        "Sedan" to "🚙",
        "Coupé" to "🏎️",
        "SUV" to "🚐",
        "Station Wagon" to "🚕",
        "Deportivo" to "🏁"
    )
    
    // Mapeo de recursos drawable por código de categoría
    // Los nombres deben coincidir con los archivos en res/drawable/
    val imagenResourceMap = mapOf(
        "hatchback" to "ic_hatchback",
        "sedan" to "ic_sedan",
        "coupe" to "ic_coupe",
        "suv" to "ic_suv",
        "station-wagon" to "ic_station_wagon",
        "deportivo" to "ic_deportivo"
    )
    
    // Data class para manejar la información de cada tipo de vehículo
    data class VehicleTypeInfo(
        val nombre: String,
        val imagenUrl: String?,
        val imagenResourceId: Int?,
        val emoji: String
    )
    
    // Función helper para obtener el ID del recurso drawable
    fun getDrawableResourceId(resourceName: String?): Int? {
        if (resourceName == null) return null
        return try {
            val resId = context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )
            if (resId != 0) resId else null
        } catch (_: Exception) {
            null
        }
    }
    
    val vehicleTypes = categoriasVehiculos.map { categoria ->
        // Prioridad: url_imagen (URL externa) > imagen_url (URL externa) > recurso local por código > emoji
        val imagenUrl = categoria.url_imagen ?: categoria.imagen_url
        val imagenResourceName = categoria.codigo?.let { imagenResourceMap[it.lowercase()] }
        val imagenResourceId = imagenResourceName?.let { getDrawableResourceId(it) }
        
        VehicleTypeInfo(
            nombre = categoria.nombre,
            imagenUrl = imagenUrl,
            imagenResourceId = imagenResourceId,
            emoji = emojiMap[categoria.nombre] ?: "🚗"
        )
    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(vehicleTypes) { vehicleInfo ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Pasar el nombre de la categoría para filtrar (se guarda así en los anuncios)
                                        onNavigateToAnuncios(vehicleInfo.nombre)
                                    },
                                color = Color(0xFF1A1A1A).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Prioridad: URL externa > recurso local > emoji
                                        when {
                                            vehicleInfo.imagenUrl != null -> {
                                                // Imagen desde URL externa
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        ImageRequest.Builder(context)
                                                            .data(vehicleInfo.imagenUrl)
                                                            .crossfade(true)
                                                            .allowHardware(false) // Permitir todos los formatos de imagen
                                                            .build()
                                                    ),
                                                    contentDescription = vehicleInfo.nombre,
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            vehicleInfo.imagenResourceId != null -> {
                                                // Imagen desde recurso local
                                                Image(
                                                    painter = painterResource(id = vehicleInfo.imagenResourceId),
                                                    contentDescription = vehicleInfo.nombre,
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            else -> {
                                                // Fallback a emoji
                                                Text(
                                                    text = vehicleInfo.emoji,
                                                    fontSize = 42.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = vehicleInfo.nombre,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            color = Color.White,
                                            letterSpacing = 0.2.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tabs Navigation
            if (selectedTab != 0 || !showVehicleTypes) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            showVehicleTypes = true
                        },
                        text = { Text("🚗 Anuncios") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            showVehicleTypes = false
                        },
                        text = { Text("🔍 Buscar") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            showVehicleTypes = false
                        },
                        text = { Text("➕ Publicar") }
                    )
                    if (isAuthenticated) {
                        Tab(
                            selected = selectedTab == 3,
                            onClick = {
                                selectedTab = 3
                                showVehicleTypes = false
                            },
                            text = { Text("🤖 Chat IA") }
                        )
                    }
                }
            }

            // Content Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1F2E), // Azul muy oscuro que complementa el header
                                Color(0xFF0F1419)  // Negro azulado más suave
                            )
                        )
                    ),
                color = Color.Transparent,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (!showVehicleTypes) {
                            ListaAnunciosScreen(
                                tipoVehiculo = null,
                                esMisAnuncios = false,
                                onAnuncioClick = onNavigateToDetalleAnuncio,
                                onContactar = {},
                                onEliminar = {},
                                isAuthenticated = isAuthenticated,
                                userId = null
                            )
                        }
                    }
                    1 -> PlateSearchScreen()
                    2 -> PublicarAutoScreen(
                        onSuccess = {},
                        isAuthenticated = isAuthenticated
                    )
                    3 -> ChatIAScreen()
                }
            }
        }
    }
}

