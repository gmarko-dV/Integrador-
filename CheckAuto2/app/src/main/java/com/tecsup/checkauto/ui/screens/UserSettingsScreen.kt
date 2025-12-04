package com.tecsup.checkauto.ui.screens

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun UserSettingsScreen(
    userName: String? = null,
    userEmail: String? = null,
    userAvatar: String? = null,
    userId: String? = null,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToMisAnuncios: () -> Unit = {},
    onNavigateToAyudaSoporte: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {}
) {
    // Cargar cantidad de notificaciones no leídas
    var cantidadNoLeidas by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(userId) {
        if (userId != null) {
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
            // Header negro elegante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF000000))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Mi Perfil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        letterSpacing = 0.3.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Perfil del usuario con glass effect
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar elegante
                    Box {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.size(90.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp),
                                        tint = Color.White.copy(alpha = 0.9f)
                                    )
                            }
                        }
                        // Badge de verificación
                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Verificado",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName ?: "Usuario",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        letterSpacing = 0.2.sp
                    )
                    if (userEmail != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = userEmail,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Opciones del menú con glass effect
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Opciones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp, top = 8.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    SettingsMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones",
                        subtitle = "Gestiona tus notificaciones",
                        onClick = onNavigateToNotificaciones
                    )
                    // Badge de notificaciones no leídas
                    if (cantidadNoLeidas > 0) {
                        Surface(
                            color = Color(0xFFFF6B6B),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 14.dp, top = 10.dp)
                        ) {
                            Text(
                                text = if (cantidadNoLeidas > 99) "99+" else cantidadNoLeidas.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                SettingsMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Mis Anuncios",
                    subtitle = "Ver y gestionar tus anuncios publicados",
                    onClick = onNavigateToMisAnuncios
                )

                SettingsMenuItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de",
                    subtitle = "Información sobre la aplicación",
                    onClick = onNavigateToAcercaDe
                )

                SettingsMenuItem(
                    icon = Icons.Default.Info,
                    title = "Ayuda y Soporte",
                    subtitle = "Obtén ayuda con la aplicación",
                    onClick = onNavigateToAyudaSoporte
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Cerrar Sesión",
                    subtitle = null,
                    onClick = onLogout,
                    textColor = Color(0xFFFF6B6B)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Text(
                text = "checkAuto v1.0",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = Color.White
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (textColor == Color(0xFFFF6B6B)) {
                    Color(0xFFFF6B6B).copy(alpha = 0.15f)
                } else {
                    Color(0xFF0066CC).copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(10.dp),
                border = if (textColor != Color(0xFFFF6B6B)) {
                    androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        Color(0xFF0066CC).copy(alpha = 0.3f)
                    )
                } else null
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = if (textColor == Color(0xFFFF6B6B)) {
                        textColor.copy(alpha = 0.9f)
                    } else {
                        Color(0xFF0066CC)
                    }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.95f),
                    letterSpacing = 0.1.sp
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

