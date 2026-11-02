package com.example.syslearning

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.app.DatePickerDialog
import android.widget.EditText
import java.util.Calendar
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.view.View
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.CheckBox
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue



class registro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referencias
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val spTipoDocumento = findViewById<Spinner>(R.id.spTipoDocumento)
        val etDocumento = findViewById<EditText>(R.id.etDocumento)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etFechaNacimiento = findViewById<EditText>(R.id.etFechaNacimiento)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Spinner
        val tiposDocumento = arrayOf(
            "Seleccione un tipo de documento",
            "Cédula de Ciudadanía",
            "Tarjeta de Identidad",
            "Cédula de Extranjería",
            "Pasaporte"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tiposDocumento
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipoDocumento.adapter = adapter

        // Calendario
        etFechaNacimiento.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    etFechaNacimiento.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // Registrar usuario
        btnRegistrar.setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val tipoDocumento = spTipoDocumento.selectedItem.toString()
            val documento = etDocumento.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val fecha = etFechaNacimiento.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() ||
                documento.isEmpty() ||
                correo.isEmpty() ||
                fecha.isEmpty() ||
                telefono.isEmpty() ||
                password.isEmpty()
            ) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tipoDocumento == "Seleccione un tipo de documento") {
                Toast.makeText(this, "Seleccione un tipo de documento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Antes de crear la cuenta, el usuario debe aceptar los términos
            // y autorizar el tratamiento de datos (Ley 1581 de 2012).
            mostrarTerminos(esMenorDeEdad(fecha)) { consentimiento ->
                crearCuenta(
                    nombre = nombre,
                    tipoDocumento = tipoDocumento,
                    documento = documento,
                    correo = correo,
                    fecha = fecha,
                    telefono = telefono,
                    password = password,
                    consentimiento = consentimiento
                )
            }
        }

        // Volver al Login
        tvLogin.setOnClickListener {
            finish()
        }
    }

    // ==================================================================
    // Términos y condiciones
    // ==================================================================

    /**
     * Muestra la ventana de términos. Las casillas se habilitan cuando el
     * usuario llega al final del texto, y "Aceptar" solo cuando marcó
     * todas las obligatorias. Devuelve el consentimiento que se guarda
     * como evidencia en Firestore.
     */
    private fun mostrarTerminos(
        esMenor: Boolean,
        onAceptar: (Map<String, Any>) -> Unit
    ) {

        val vista = layoutInflater.inflate(R.layout.dialog_terminos, null)

        val scroll = vista.findViewById<ScrollView>(R.id.scrollTerminos)
        val tvTexto = vista.findViewById<TextView>(R.id.tvTextoTerminos)
        val tvAviso = vista.findViewById<TextView>(R.id.tvAvisoLectura)
        val cbTerminos = vista.findViewById<CheckBox>(R.id.cbAceptoTerminos)
        val cbDatos = vista.findViewById<CheckBox>(R.id.cbAutorizoDatos)
        val cbRepresentante = vista.findViewById<CheckBox>(R.id.cbAutorizacionRepresentante)
        val btnRechazar = vista.findViewById<MaterialButton>(R.id.btnRechazarTerminos)
        val btnAceptar = vista.findViewById<MaterialButton>(R.id.btnAceptarTerminos)

        tvTexto.text = HtmlCompat.fromHtml(
            leerTextoTerminos(),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        cbRepresentante.visibility = if (esMenor) View.VISIBLE else View.GONE

        val casillas = listOfNotNull(
            cbTerminos,
            cbDatos,
            if (esMenor) cbRepresentante else null
        )

        fun actualizarBoton() {
            btnAceptar.isEnabled = casillas.all { it.isChecked }
        }

        fun habilitarCasillas() {
            casillas.forEach { it.isEnabled = true }
            tvAviso.visibility = View.GONE
        }

        casillas.forEach { casilla ->
            casilla.setOnCheckedChangeListener { _, _ -> actualizarBoton() }
        }

        // Se habilitan al llegar al final del texto (o si cabe completo).
        scroll.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!scroll.canScrollVertically(1)) habilitarCasillas()
        }
        scroll.post {
            if (!scroll.canScrollVertically(1)) habilitarCasillas()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(vista)
            .setCancelable(false)
            .create()

        btnRechazar.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(
                this,
                "Para crear la cuenta debes aceptar los términos y condiciones",
                Toast.LENGTH_LONG
            ).show()
        }

        btnAceptar.setOnClickListener {

            dialog.dismiss()

            val consentimiento = mutableMapOf<String, Any>(
                "terminosAceptados" to true,
                "tratamientoDatosAutorizado" to true,
                "versionTerminos" to VERSION_TERMINOS,
                "fechaAceptacion" to FieldValue.serverTimestamp(),
                "fechaAceptacionLocal" to Timestamp.now(),
                "medio" to "app_android_registro"
            )

            if (esMenor) {
                consentimiento["autorizacionRepresentanteLegal"] = true
            }

            onAceptar(consentimiento)
        }

        dialog.show()
    }

    private fun leerTextoTerminos(): String {
        return resources.openRawResource(R.raw.terminos_condiciones)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    /** La fecha llega como "d/M/yyyy" desde el DatePicker. */
    private fun esMenorDeEdad(fecha: String): Boolean {

        val partes = fecha.split("/").mapNotNull { it.trim().toIntOrNull() }
        if (partes.size != 3) return false

        val (dia, mes, anio) = partes
        val hoy = Calendar.getInstance()

        var edad = hoy.get(Calendar.YEAR) - anio
        val mesActual = hoy.get(Calendar.MONTH) + 1
        val diaActual = hoy.get(Calendar.DAY_OF_MONTH)

        if (mesActual < mes || (mesActual == mes && diaActual < dia)) {
            edad--
        }

        return edad < 18
    }

    // ==================================================================
    // Creación de la cuenta
    // ==================================================================

    private fun crearCuenta(
        nombre: String,
        tipoDocumento: String,
        documento: String,
        correo: String,
        fecha: String,
        telefono: String,
        password: String,
        consentimiento: Map<String, Any>
    ) {

        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener {

                val uid = auth.currentUser!!.uid

                // El rol siempre es "estudiante": solo un administrador
                // puede cambiarlo (CU27).
                val usuario = hashMapOf(
                    "nombre" to nombre,
                    "tipoDocumento" to tipoDocumento,
                    "documento" to documento,
                    "correo" to correo,
                    "fechaNacimiento" to fecha,
                    "telefono" to telefono,
                    "rol" to "estudiante",
                    "consentimiento" to consentimiento
                )

                db.collection("usuarios")
                    .document(uid)
                    .set(usuario)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Usuario registrado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, Pantalla2::class.java))
                        finish()
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Error al guardar los datos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        val view = currentFocus

        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }

        return super.dispatchTouchEvent(ev)
    }

    companion object {
        /** Cambiar cuando se actualice res/raw/terminos_condiciones.html */
        const val VERSION_TERMINOS = "2026-11-02"
    }
}
