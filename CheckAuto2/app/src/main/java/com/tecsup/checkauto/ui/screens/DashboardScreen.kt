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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAnuncios: () -> Unit,
    onNavigateToBuscar: () -> Unit,
    onNavigateToPublicar: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToDetalleAnuncio: (Long) -> Unit = {},
    isAuthenticated: Boolean = false,
    userName: String? = null,
    onLogin: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showVehicleTypes by remember { mutableStateOf(true) }
    val context = LocalContext.current

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
            // Header
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
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
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAuthenticated) {
                                // Notificaciones placeholder
                                IconButton(onClick = { /* TODO: Abrir notificaciones */ }) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notificaciones",
                                        tint = Color.White
                                    )
                                }
                                // Perfil
                                IconButton(onClick = onNavigateToConfiguracion) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "Perfil",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                TextButton(onClick = onLogin) {
                                    Text("Iniciar Sesión", color = Color.White)
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
                        Button(
                            onClick = {
                                selectedTab = 1
                                showVehicleTypes = false
                                onNavigateToBuscar()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066CC)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Buscar Autos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                selectedTab = 2
                                showVehicleTypes = false
                                onNavigateToPublicar()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0066CC)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Publicar Anuncio", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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

                    val vehicleTypes = listOf(
                        "Hatchback" to "🚗",
                        "Sedan" to "🚙",
                        "Coupé" to "🏎️",
                        "SUV" to "🚐",
                        "Station Wagon" to "🚕",
                        "Deportivo" to "🏁"
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(220.dp)
                    ) {
                        items(vehicleTypes) { (type, emoji) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigateToAnuncios()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 36.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF333333)
                                    )
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
                    .weight(1f),
                color = Color.White.copy(alpha = 0.95f),
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

