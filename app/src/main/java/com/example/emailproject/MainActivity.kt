package com.example.formulario

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var attachButton: Button
    private lateinit var sendButton: Button
    private lateinit var fileTextView: TextView

    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        attachButton = findViewById(R.id.attachButton)
        sendButton = findViewById(R.id.sendButton)
        fileTextView = findViewById(R.id.fileTextView)

        val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    fileUri = uri
                    fileTextView.text = getFileName(uri)
                }
            }
        }

        attachButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "/"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePickerLauncher.launch(intent)
        }

        sendButton.setOnClickListener {
            if (validateInputs()) {
                sendEmail()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Ingrese una descripción", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getFileName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                return it.getString(index)
            }
        }
        return "Archivo adjunto"
    }

    private fun sendEmail() {
        val recipient = emailEditText.text.toString()
        val subject = "Formulario de ${nameEditText.text}"
        val body = descriptionEditText.text.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            fileUri?.let { putExtra(Intent.EXTRA_STREAM, it) }
        }
        startActivity(Intent.createChooser(intent, "Enviar correo"))
    }
}