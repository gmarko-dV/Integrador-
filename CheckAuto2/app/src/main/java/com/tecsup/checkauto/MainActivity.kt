package com.tecsup.checkauto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.checkauto.model.Anuncio
import com.tecsup.checkauto.ui.screens.*
import com.tecsup.checkauto.ui.theme.CheckAutoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckAutoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isAuthenticated by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userEmail by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    isAuthenticated = true
                    userId = "mobile-user-123"
                    userName = "Usuario Móvil"
                    userEmail = "usuario@ejemplo.com"
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToAnuncios = {
                    navController.navigate("anuncios")
                },
                onNavigateToBuscar = {
                    navController.navigate("buscar")
                },
                onNavigateToPublicar = {
                    navController.navigate("publicar")
                },
                onNavigateToChat = {
                    navController.navigate("chat")
                },
                onNavigateToConfiguracion = {
                    navController.navigate("configuracion")
                },
                onNavigateToDetalleAnuncio = { idAnuncio ->
                    navController.navigate("detalle/$idAnuncio")
                },
                isAuthenticated = isAuthenticated,
                userName = userName,
                onLogin = {
                    // TODO: Implementar login con Auth0
                    isAuthenticated = true
                    userId = "mobile-user-123"
                    userName = "Usuario Móvil"
                    userEmail = "usuario@ejemplo.com"
                }
            )
        }

        composable("anuncios") {
            ListaAnunciosScreen(
                tipoVehiculo = null,
                esMisAnuncios = false,
                onAnuncioClick = { idAnuncio ->
                    navController.navigate("detalle/$idAnuncio")
                },
                onContactar = { anuncio ->
                    // TODO: Implementar contacto
                },
                onEliminar = { idAnuncio ->
                    // TODO: Implementar eliminación
                },
                isAuthenticated = isAuthenticated,
                userId = userId
            )
        }

        composable("buscar") {
            PlateSearchScreen()
        }

        composable("publicar") {
            PublicarAutoScreen(
                onSuccess = {
                    navController.popBackStack()
                },
                isAuthenticated = isAuthenticated
            )
        }

        composable("chat") {
            ChatIAScreen()
        }

        composable("configuracion") {
            UserSettingsScreen(
                userName = userName,
                userEmail = userEmail,
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    isAuthenticated = false
                    userId = null
                    userName = null
                    userEmail = null
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToNotificaciones = {
                    navController.navigate("notificaciones")
                },
                onNavigateToMisAnuncios = {
                    navController.navigate("mis-anuncios")
                }
            )
        }

        composable("notificaciones") {
            NotificacionesScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAnuncioClick = { idAnuncio ->
                    navController.navigate("detalle/$idAnuncio")
                }
            )
        }

        composable("mis-anuncios") {
            ListaAnunciosScreen(
                tipoVehiculo = null,
                esMisAnuncios = true,
                onAnuncioClick = { idAnuncio ->
                    navController.navigate("detalle/$idAnuncio")
                },
                onContactar = { anuncio ->
                    // No aplica en mis anuncios
                },
                onEliminar = { idAnuncio ->
                    // TODO: Implementar eliminación
                },
                isAuthenticated = isAuthenticated,
                userId = userId
            )
        }

        composable("detalle/{idAnuncio}") { backStackEntry ->
            val idAnuncio = backStackEntry.arguments?.getString("idAnuncio")?.toLongOrNull() ?: 0L
            
            // Por ahora, crear un anuncio de ejemplo
            // En producción, esto vendría de la API
            val anuncioEjemplo = Anuncio(
                idAnuncio = idAnuncio,
                modelo = "Toyota Corolla",
                anio = 2020,
                kilometraje = 50000,
                precio = 35000.0,
                descripcion = "Excelente estado, único dueño, mantenimiento al día. Vehículo en perfectas condiciones.",
                emailContacto = "vendedor@ejemplo.com",
                telefonoContacto = "+51 987 654 321",
                fechaCreacion = "2024-01-15",
                imagenes = listOf(),
                tipoVehiculo = "Sedan",
                idUsuario = userId
            )

            DetalleAnuncioScreen(
                anuncio = anuncioEjemplo,
                onBack = {
                    navController.popBackStack()
                },
                onContactar = {
                    // TODO: Implementar contacto
                },
                esPropietario = anuncioEjemplo.idUsuario == userId,
                isAuthenticated = isAuthenticated
            )
        }
    }
}
