package com.tecsup.checkauto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.checkauto.ui.screens.*
import com.tecsup.checkauto.ui.components.FloatingChatBubble
import com.tecsup.checkauto.ui.theme.CheckAutoTheme
import com.tecsup.checkauto.service.SupabaseAuthService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicializar Supabase con el contexto de Android para persistencia de sesión
        com.tecsup.checkauto.config.SupabaseConfig.initialize(this)
        
        // Manejar deep links de Supabase para confirmación de email
        handleSupabaseDeepLink(intent)
        
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
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Manejar deep links cuando la app ya está abierta
        handleSupabaseDeepLink(intent)
    }
    
    private fun handleSupabaseDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null) {
            Log.d("MainActivity", "Deep link recibido: $data")
            
            // Verificar si es un enlace de confirmación de Supabase
            // Puede venir de:
            // 1. URL de la web: http://localhost:3000/callback o https://tu-dominio.com/callback
            // 2. Deep link personalizado: checkauto://auth/callback
            val isCallbackLink = (data.scheme == "http" && data.host == "localhost" && data.port == 3000 && data.path == "/callback") ||
                    (data.scheme == "https" && data.path?.contains("/callback") == true) ||
                    (data.scheme == "checkauto" && data.host == "auth")
            
            if (isCallbackLink) {
                // Extraer los parámetros de la URL
                val accessToken = data.getQueryParameter("access_token")
                val refreshToken = data.getQueryParameter("refresh_token")
                val type = data.getQueryParameter("type")
                val error = data.getQueryParameter("error")
                
                Log.d("MainActivity", "Callback recibido - Token: ${accessToken != null}, Tipo: $type, Error: $error")
                
                // Procesar el callback de Supabase
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        if (error != null) {
                            Log.e("MainActivity", "Error en callback: $error")
                            return@launch
                        }
                        
                        // Esperar un momento para que Supabase procese el callback
                        kotlinx.coroutines.delay(500)
                        
                        // Verificar si hay una sesión activa usando el servicio
                        val session = SupabaseAuthService.getCurrentSession()
                        if (session != null) {
                            val user = SupabaseAuthService.getCurrentUser()
                            Log.d("MainActivity", "✅ Sesión confirmada: ${user?.email}")
                            // La sesión ya está activa, el usuario está autenticado
                            // El estado de autenticación se actualizará automáticamente en AppNavigation
                        } else {
                            Log.d("MainActivity", "⚠️ No se encontró sesión. Puede que el usuario necesite confirmar el email primero.")
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error al procesar callback: ${e.message}")
                    }
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
    
    // Verificar y restaurar autenticación al iniciar
    LaunchedEffect(Unit) {
        try {
            // Intentar restaurar la sesión guardada
            val session = com.tecsup.checkauto.service.SupabaseAuthService.getCurrentSession()
            if (session != null) {
                // Hay una sesión guardada, verificar si es válida
                val currentUser = com.tecsup.checkauto.service.SupabaseAuthService.getCurrentUser()
                if (currentUser != null) {
                    isAuthenticated = true
                    userId = currentUser.id
                    userEmail = currentUser.email
                    userName = currentUser.userMetadata?.get("nombre")?.toString() 
                        ?: currentUser.email?.substringBefore("@")
                    Log.d("AppNavigation", "✅ Sesión restaurada: ${currentUser.email}")
                } else {
                    // Sesión inválida, limpiar estado
                    isAuthenticated = false
                    userId = null
                    userEmail = null
                    userName = null
                    Log.d("AppNavigation", "⚠️ Sesión encontrada pero usuario no válido")
                }
            } else {
                // No hay sesión guardada
                isAuthenticated = false
                userId = null
                userEmail = null
                userName = null
                Log.d("AppNavigation", "ℹ️ No hay sesión guardada")
            }
        } catch (e: Exception) {
            Log.e("AppNavigation", "Error al restaurar sesión: ${e.message}")
            isAuthenticated = false
            userId = null
            userEmail = null
            userName = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { userInfo ->
                    isAuthenticated = true
                    userId = userInfo?.id
                    userEmail = userInfo?.email
                    userName = userInfo?.userMetadata?.get("nombre")?.toString() 
                        ?: userInfo?.email?.substringBefore("@")
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onSkip = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { userInfo ->
                    isAuthenticated = true
                    userId = userInfo?.id
                    userEmail = userInfo?.email
                    userName = userInfo?.userMetadata?.get("nombre")?.toString() 
                        ?: userInfo?.email?.substringBefore("@")
                    navController.navigate("dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToAnuncios = { tipoVehiculo ->
                    if (tipoVehiculo != null) {
                        navController.navigate("anuncios/$tipoVehiculo")
                    } else {
                        navController.navigate("anuncios")
                    }
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
                onNavigateToNotificaciones = {
                    navController.navigate("notificaciones")
                },
                onNavigateToDetalleAnuncio = { idAnuncio ->
                    navController.navigate("detalle/$idAnuncio")
                },
                isAuthenticated = isAuthenticated,
                userName = userName,
                userId = userId,
                onLogin = {
                    navController.navigate("login")
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
                    // Navegar al detalle del anuncio para contactar
                    navController.navigate("detalle/${anuncio.idAnuncio}")
                },
                onEliminar = { idAnuncio ->
                    // TODO: Implementar eliminación
                },
                isAuthenticated = isAuthenticated,
                userId = userId
            )
        }
        
        composable("anuncios/{tipoVehiculo}") { backStackEntry ->
            val tipoVehiculo = backStackEntry.arguments?.getString("tipoVehiculo")
            ListaAnunciosScreen(
                tipoVehiculo = tipoVehiculo,
                esMisAnuncios = false,
                onAnuncioClick = { idAnuncio ->
                    navController.navigate("detalle/$idAnuncio")
                },
                onContactar = { anuncio ->
                    // Navegar al detalle del anuncio para contactar
                    navController.navigate("detalle/${anuncio.idAnuncio}")
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
                isAuthenticated = isAuthenticated,
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable("chat") {
            ChatIAScreen()
        }

        composable("configuracion") {
            UserSettingsScreen(
                userName = userName,
                userEmail = userEmail,
                userId = userId,
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        com.tecsup.checkauto.service.SupabaseAuthService.signOut()
                    }
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
                vendedorId = userId,
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

            DetalleAnuncioScreen(
                anuncioId = idAnuncio,
                onBack = {
                    navController.popBackStack()
                },
                onContactar = {
                    // TODO: Implementar contacto con notificaciones
                },
                esPropietario = false, // Se calcula dentro de la pantalla
                isAuthenticated = isAuthenticated,
                userId = userId
            )
        }
    }
    
    // Chat flotante (overlay global, aparece en todas las pantallas)
    FloatingChatBubble()
    }
}
