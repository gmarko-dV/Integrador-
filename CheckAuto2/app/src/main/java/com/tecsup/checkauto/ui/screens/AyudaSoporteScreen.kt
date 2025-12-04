package com.tecsup.checkauto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun AyudaSoporteScreen(
    userEmail: String? = null,
    userName: String? = null,
    onBack: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf(userName ?: "") }
    var correo by remember { mutableStateOf(userEmail ?: "") }
    var telefono by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val handleSubmit: () -> Unit = handleSubmit@{
        // Validaciones
        error = null
        if (nombre.trim().isEmpty()) {
            error = "El nombre es requerido"
            return@handleSubmit
        }
        if (correo.trim().isEmpty()) {
            error = "El correo electrónico es requerido"
            return@handleSubmit
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
            error = "El formato del correo electrónico no es válido"
            return@handleSubmit
        }
        if (mensaje.trim().isEmpty()) {
            error = "El mensaje es requerido"
            return@handleSubmit
        }

        loading = true
        scope.launch {
            try {
                // Simular envío (aquí puedes agregar la llamada a tu API cuando esté lista)
                delay(1000)

                success = true
                nombre = userName ?: ""
                correo = userEmail ?: ""
                telefono = ""
                mensaje = ""

                // Ocultar mensaje de éxito después de 5 segundos
                delay(5000)
                success = false
            } catch (e: Exception) {
                error = "Error al enviar el mensaje. Por favor, intenta nuevamente."
            } finally {
                loading = false
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
            // Header azul claro como la web
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
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ayuda y Soporte",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            }

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Información de contacto
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Datos de Contacto",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ContactInfoItem(
                            icon = Icons.Default.Info,
                            title = "Informaciones",
                            email = "info@checkauto.pe"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ContactInfoItem(
                            icon = Icons.Default.Settings,
                            title = "Soporte",
                            email = "soporte@checkauto.pe"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ContactInfoItem(
                            icon = Icons.Default.Info,
                            title = "Publicidad & Marketing",
                            email = "publicidad@checkauto.pe"
                        )
                    }
                }

                // Formulario
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Escríbenos un mensaje...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Si tienes alguna duda sobre nuestros servicios o necesitas ayuda para publicar tu anuncio, no dudes en escribirnos y nos pondremos en contacto contigo a la brevedad posible.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        if (error != null) {
                            Surface(
                                color = Color(0xFFFF6B6B).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFFFF6B6B).copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = error!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (success) {
                            Surface(
                                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFF4CAF50).copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "¡Mensaje enviado exitosamente! Nos pondremos en contacto contigo pronto.",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Campo Nombre
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre *", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                focusedBorderColor = Color(0xFF0066CC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFF0066CC),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0066CC))
                            }
                        )

                        // Campo Correo
                        OutlinedTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = { Text("Correo Electrónico *", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                focusedBorderColor = Color(0xFF0066CC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFF0066CC),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0066CC))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            )
                        )

                        // Campo Teléfono
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                focusedBorderColor = Color(0xFF0066CC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color(0xFF0066CC),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0066CC))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            )
                        )

                        // Campo Mensaje
                        Column(modifier = Modifier.padding(bottom = 20.dp)) {
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF0066CC),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Mensaje *",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            OutlinedTextField(
                                value = mensaje,
                                onValueChange = { mensaje = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                    focusedBorderColor = Color(0xFF0066CC),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                placeholder = { Text("Escribe tu mensaje aquí...", color = Color.White.copy(alpha = 0.5f)) }
                            )
                        }

                        // Botón Enviar
                        Button(
                            onClick = handleSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !loading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066CC)
                            )
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Enviar Mensaje",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    email: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = Color(0xFF0066CC).copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF0066CC),
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066CC)
            )
        }
    }
}

