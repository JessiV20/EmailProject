package com.example.emailproject

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.text.input.TextFieldValue
import com.example.emailproject.ui.theme.EmailProjectTheme
import androidx.compose.foundation.layout.Spacer

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
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Equipo de Arrastre", "Industria", "Infraestructura", "Locomotoras", "Señales")
    var selectedOption by remember { mutableStateOf(options[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Correo") }
            )
        }
    ) { paddingValues ->
        FormScreen(
            modifier = Modifier.padding(paddingValues),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it }
        )
    }
}
@Composable
fun FormScreen(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(TextFieldValue("gatogalleta777@gmail.com, kryzglz02@gmail.com")) }
    var comentario by remember { mutableStateOf(TextFieldValue()) }
    var observaciones by remember { mutableStateOf(TextFieldValue()) }
    var distrito by remember { mutableStateOf(TextFieldValue()) }
    var placaKilometrica by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var fileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }


    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val uri = createImageUri(context)
            uri?.let { imageUri = it }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) {
            fileUris = uris
        }
    }


    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                IconButton(onClick = { onExpandedChange(!expanded) }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    listOf("Equipo de Arrastre", "Industria", "Infraestructura", "Locomotoras", "Señales").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                onExpandedChange(false)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = "/$selectedOption",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("TO:") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        // Botón para tomar foto o seleccionar archivos
        Button(onClick = { showDialog = true }) {
            Text("Tomar Foto / Seleccionar Archivos")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Seleccionar Opción") },
                text = {
                    Column {
                        TextButton(onClick = {
                            val uri = createImageUri(context)
                            uri?.let { takePictureLauncher.launch(it) }
                            showDialog = false
                        }) {
                            Text("Tomar Foto")
                        }
                        TextButton(onClick = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                            showDialog = false
                        }) {
                            Text("Seleccionar Archivos")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        imageUri?.let {
            AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mostrar los archivos seleccionados
        fileUris.forEach { uri ->
            Text("Archivo seleccionado: ${getFileName(uri)}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de enviar
        Button(onClick = {
            if (comentario.text.isBlank() || observaciones.text.isBlank() || distrito.text.isBlank() || placaKilometrica.text.isBlank()) {
                errorMessage = "Todos los campos son obligatorios"
            } else {
                errorMessage = ""
                val destinatarios = email.text.split(",").map { it.trim() }.toTypedArray()
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*" // Tipo de archivo múltiple
                    putExtra(Intent.EXTRA_EMAIL, destinatarios)
                    putExtra(Intent.EXTRA_SUBJECT, selectedOption)
                    putExtra(Intent.EXTRA_TEXT, "Comentario: ${comentario.text}\nObservaciones: ${observaciones.text}\nDistrito: ${distrito.text}\nPlaca Kilométrica: ${placaKilometrica.text}")
                    imageUri?.let { putExtra(Intent.EXTRA_STREAM, it) }
                    if (fileUris.isNotEmpty()) {
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(fileUris)) // Agregar múltiples archivos
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Enviar correo"))
            }
        }) {
            Text("Enviar")
        }
    }}


fun createImageUri(context: Context): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "temp_image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}



@Composable
private fun getFileName(uri: Uri): String {
    val cursor = LocalContext.current.contentResolver.query(uri, arrayOf(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME), null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME))
        }
    }
    return uri.path ?: "Desconocido"
}
