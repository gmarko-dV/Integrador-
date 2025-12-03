package com.tecsup.checkauto.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.checkauto.service.SupabaseService
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
    onNavigateToMisAnuncios: () -> Unit = {}
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
                Text(
                    text = "Configuración",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Perfil del usuario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userName ?: "Usuario",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (userEmail != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Opciones del menú
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
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
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 8.dp)
                    ) {
                        Text(
                            text = if (cantidadNoLeidas > 99) "99+" else cantidadNoLeidas.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                onClick = { /* TODO: Mostrar información */ }
            )

            SettingsMenuItem(
                icon = Icons.Default.Info,
                title = "Ayuda y Soporte",
                subtitle = "Obtén ayuda con la aplicación",
                onClick = { /* TODO: Mostrar ayuda */ }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Cerrar Sesión",
                subtitle = null,
                onClick = onLogout,
                textColor = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Text(
            text = "checkAuto v1.0",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

