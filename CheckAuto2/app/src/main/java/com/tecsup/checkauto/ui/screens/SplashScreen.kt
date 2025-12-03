package com.tecsup.checkauto.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = rememberInfiniteTransition(label = "splash_alpha")
    val alpha by alphaAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // Mostrar splash por 2 segundos
        
        // Verificar si hay una sesión guardada
        val currentUser = com.tecsup.checkauto.service.SupabaseAuthService.getCurrentUser()
        if (currentUser != null) {
            // Hay una sesión válida, navegar directamente al dashboard
            onNavigateToDashboard()
        } else {
            // No hay sesión, navegar al login
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0066CC),
                        Color(0xFF004499)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo placeholder - puedes reemplazar con un Image si tienes el logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Color.White.copy(alpha = alpha),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CA",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0066CC)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "checkAuto",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tu portal confiable de autos",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

