package com.example.emailproject

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.emailproject.ui.theme.EmailProjectTheme
import kotlinx.coroutines.launch
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.text.input.TextFieldValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmailProjectTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", modifier = Modifier.padding(16.dp))
                Divider()
                NavigationDrawerItem(label = { Text("Inicio") }, selected = false, onClick = {})
                NavigationDrawerItem(label = { Text("Configuración") }, selected = false, onClick = {})
                NavigationDrawerItem(label = { Text("Cerrar Sesión") }, selected = false, onClick = {})
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Correo") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.List, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { paddingValues ->
            FormScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(TextFieldValue("gatogalleta777@gmail.com, kryzglz02@gmail.com")) }
    var comentario by remember { mutableStateOf(TextFieldValue()) }
    var observaciones by remember { mutableStateOf(TextFieldValue()) }
    var distrito by remember { mutableStateOf(TextFieldValue()) }
    var placaKilometrica by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var showOptions by remember { mutableStateOf(false) }

    // Lista de opciones
    val options = listOf("Equipo de Arrastre", "Industria", "Infraestructura", "Locomotoras", "Señales")
    var selectedOption by remember { mutableStateOf(options[0]) }

    // Función para crear URI para la cámara
    fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    Column(modifier = modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("TO:") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        // Mostrar las opciones con botones
        Text("Asunto: ${selectedOption}", modifier = Modifier.padding(vertical = 8.dp))
        options.forEach { option ->
            Button(onClick = { selectedOption = option }) {
                Text(text = option)
            }
        }

        OutlinedTextField(
            value = comentario,
            onValueChange = { comentario = it },
            label = { Text("Comentario") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = distrito,
            onValueChange = { distrito = it },
            label = { Text("Distrito") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = placaKilometrica,
            onValueChange = { placaKilometrica = it },
            label = { Text("Placa Kilométrica") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showOptions = !showOptions }) {
            Text("Seleccionar Imagen")
        }

        if (showOptions) {
            Column {
                Button(onClick = {
                    val imageUriForCamera = createImageUri()
                    // Lógica para tomar foto
                    showOptions = false
                }) {
                    Text("Tomar Foto con la Cámara")
                }

                Button(onClick = {
                    // Lógica para seleccionar imagen del almacenamiento
                    showOptions = false
                }) {
                    Text("Seleccionar Imagen del Almacenamiento")
                }
            }
        }

        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Imagen seleccionada",
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = {
            if (comentario.text.isBlank() || observaciones.text.isBlank() || distrito.text.isBlank() || placaKilometrica.text.isBlank()) {
                errorMessage = "Todos los campos son obligatorios"
            } else {
                errorMessage = ""
                val destinatarios = email.text.split(",").map { it.trim() }.toTypedArray()
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, destinatarios)
                    putExtra(Intent.EXTRA_SUBJECT, selectedOption) // Now the selected option is the subject
                    putExtra(Intent.EXTRA_TEXT, "Comentario: ${comentario.text}\nObservaciones: ${observaciones.text}\nDistrito: ${distrito.text}\nPlaca Kilométrica: ${placaKilometrica.text}")
                    imageUri?.let { putExtra(Intent.EXTRA_STREAM, it) }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Enviar correo"))
            }
        }) {
            Text("Enviar")
        }
    }
}
