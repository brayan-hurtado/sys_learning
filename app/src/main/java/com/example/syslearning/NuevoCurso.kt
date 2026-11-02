package com.example.syslearning

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import android.widget.ArrayAdapter
import com.example.syslearning.model.CursoFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NuevoCurso : AppCompatActivity() {

    private lateinit var imgCurso: ImageView
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var storage: FirebaseStorage
    private var uriImagen: Uri? = null
    private lateinit var edtTitulo: TextInputEditText
    private lateinit var edtDescripcion: TextInputEditText
    private lateinit var edtContenido: TextInputEditText
    private lateinit var edtDatoImportante: TextInputEditText
    private lateinit var edtSabiasQue: TextInputEditText
    private lateinit var edtConsejo: TextInputEditText
    private lateinit var edtPdf: TextInputEditText
    private lateinit var edtVideo: TextInputEditText
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var spCategoria: Spinner
    private lateinit var spNivel: Spinner

    private lateinit var btnGuardarCurso: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_nuevo_curso)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        imgCurso = findViewById(R.id.imgCurso)
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen)
        storage = FirebaseStorage.getInstance()
        edtTitulo = findViewById(R.id.edtTitulo)
        edtDescripcion = findViewById(R.id.edtDescripcion)
        edtContenido = findViewById(R.id.edtContenido)
        edtDatoImportante = findViewById(R.id.edtDatoImportante)
        edtSabiasQue = findViewById(R.id.edtSabiasQue)
        edtConsejo = findViewById(R.id.edtConsejo)
        edtPdf = findViewById(R.id.edtPdf)
        edtVideo = findViewById(R.id.edtVideo)

        spCategoria = findViewById(R.id.spCategoria)
        spNivel = findViewById(R.id.spNivel)

        btnGuardarCurso = findViewById(R.id.btnGuardarCurso)

        val categorias = listOf(
            "Ofimática",
            "Programación",
            "Redes",
            "Seguridad",

            )

        val adapterCategoria = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categorias
        )

        adapterCategoria.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spCategoria.adapter = adapterCategoria

        val niveles = listOf(
            "Básico",
            "Intermedio",
            "Avanzado"
        )

        val adapterNivel = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            niveles
        )

        adapterNivel.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spNivel.adapter = adapterNivel

        // Avisa al admin para qué aula quedará este curso (o si será global).
        if (AulaActual.hayFiltro()) {
            Toast.makeText(
                this,
                "Este curso se creará para el aula: ${AulaActual.nombre}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "Este curso será visible para todos los estudiantes (global)",
                Toast.LENGTH_LONG
            ).show()
        }

        btnSeleccionarImagen.setOnClickListener {

            seleccionarImagen.launch("image/*")

        }

        // (Antes había un setOnClickListener anidado dentro de otro,
        // por lo que el primer clic no hacía nada. Queda uno solo.)
        btnGuardarCurso.setOnClickListener {

            subirImagen { urlImagen ->

                guardarCurso(urlImagen)

            }

        }
    }

    private fun guardarCurso(urlImagen: String) {

        val id = db.collection("cursos").document().id

        // Si hay un aula seleccionada en el panel, el curso queda
        // asignado a esa aula. Si no, queda global (visible para todos).
        val aulasAsignadas =
            if (AulaActual.hayFiltro()) listOf(AulaActual.codigo) else emptyList()

        val curso = CursoFirestore(

            id = id,

            titulo = edtTitulo.text.toString(),

            descripcion = edtDescripcion.text.toString(),

            contenido = edtContenido.text.toString(),

            datoImportante = edtDatoImportante.text.toString(),

            sabiasQue = edtSabiasQue.text.toString(),

            consejo = edtConsejo.text.toString(),

            categoria = spCategoria.selectedItem.toString(),

            nivel = spNivel.selectedItem.toString(),

            imagen = urlImagen,

            pdf = edtPdf.text.toString(),

            video = edtVideo.text.toString(),

            creadoPor = auth.currentUser!!.uid,

            fechaCreacion = System.currentTimeMillis(),

            estado = true,

            aulasAsignadas = aulasAsignadas

        )

        db.collection("cursos")

            .document(id)

            .set(curso)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Curso creado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                setResult(Activity.RESULT_OK)
                finish()

            }

            .addOnFailureListener { e ->

                Toast.makeText(

                    this,

                    e.localizedMessage,

                    Toast.LENGTH_LONG

                ).show()

            }

    }

    private fun subirImagen(onSuccess: (String) -> Unit) {

        if (uriImagen == null) {

            Toast.makeText(
                this,
                "Seleccione una imagen",
                Toast.LENGTH_SHORT
            ).show()

            return

        }

        val nombreArchivo = "curso_${System.currentTimeMillis()}.jpg"

        val referencia = storage.reference
            .child("imagenesCursos/$nombreArchivo")

        referencia.putFile(uriImagen!!)
            .addOnSuccessListener {

                referencia.downloadUrl
                    .addOnSuccessListener { uri ->

                        onSuccess(uri.toString())

                    }

            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()

                Log.e("STORAGE", "Error", e)

            }

    }



    private val seleccionarImagen =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                uriImagen = uri

                imgCurso.setImageURI(uri)

            }

        }
}
