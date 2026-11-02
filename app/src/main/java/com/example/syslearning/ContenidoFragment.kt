package com.example.syslearning


import com.google.firebase.Timestamp
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.example.syslearning.model.CursoFirestore

class ContenidoFragment : Fragment() {

    private lateinit var imgPortada: ImageView
    private var esFirestore = false
    private var idCurso = ""
    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvDatoImportante: TextView
    private lateinit var tvContenido: TextView

    private lateinit var btnMaterial: Button
    private lateinit var tvSabiasQue: TextView
    private lateinit var tvConsejo: TextView
    private lateinit var curso: Curso
    private var urlPdf = ""
    private var urlVideo = ""

    // contenido leido
    private lateinit var scrollContenido: ScrollView
    private var tiempoInicio = 0L
    private var cursoRegistrado = false
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_contenido,
            container,
            false


        )
        esFirestore = arguments?.getBoolean("firestore") ?: false
        idCurso = arguments?.getString("idCurso") ?: ""


        imgPortada = view.findViewById(R.id.imgPortada)
        scrollContenido = view.findViewById(R.id.scrollContenido)
        tvTitulo = view.findViewById(R.id.tvTitulo)
        tvDescripcion = view.findViewById(R.id.tvDescripcion)
        tvDatoImportante = view.findViewById(R.id.tvDatoImportante)
        tvContenido = view.findViewById(R.id.tvContenido)
        btnMaterial = view.findViewById(R.id.btnMaterial)
        tvSabiasQue = view.findViewById(R.id.tvSabiasQue)
        tvConsejo = view.findViewById(R.id.tvConsejo)


        btnMaterial.setOnClickListener {

            mostrarMaterialComplementario()

        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (esFirestore) {

            cargarCursoFirestore()

        } else {

            cargarContenido()

        }
        verificarCursoLeido()

        return view


    }

    private fun cargarCursoFirestore() {

        Log.d("FIRESTORE", "Entró a cargarCursoFirestore")

        db.collection("cursos")
            .document(idCurso)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {

                    Toast.makeText(
                        requireContext(),
                        "El curso no existe.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                val cursoFirestore = document.toObject(CursoFirestore::class.java)
                    ?: return@addOnSuccessListener

                Log.d("FIRESTORE", "PDF: ${cursoFirestore.pdf}")
                Log.d("FIRESTORE", "VIDEO: ${cursoFirestore.video}")

                Glide.with(requireContext())
                    .load(cursoFirestore.imagen)
                    .into(imgPortada)

                tvTitulo.text = cursoFirestore.titulo
                tvDescripcion.text = cursoFirestore.descripcion
                tvContenido.text = cursoFirestore.contenido
                tvDatoImportante.text = cursoFirestore.datoImportante
                tvSabiasQue.text = cursoFirestore.sabiasQue
                tvConsejo.text = cursoFirestore.consejo

                urlPdf = cursoFirestore.pdf
                urlVideo = cursoFirestore.video

            }
            .addOnFailureListener {

                Toast.makeText(
                    requireContext(),
                    "Error al cargar el curso.",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun verificarCursoLeido() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener {

                val progreso =
                    it.get("progreso") as? Map<*, *>

                val nombreCurso = if (esFirestore) {
                    tvTitulo.text.toString()
                } else {
                    curso.titulo
                }

                val progresoCurso =
                    progreso?.get(nombreCurso) as? Map<*, *>

                cursoRegistrado =
                    progresoCurso?.get("completado") == true

            }

    }

    private fun guardarCursoLeido() {

        val uid = auth.currentUser?.uid ?: return

        val nombreCurso = if (esFirestore) {
            tvTitulo.text.toString()
        } else {
            curso.titulo
        }

        val datos = hashMapOf(

            "completado" to true,
            "fecha" to Timestamp.now()

        )

        db.collection("usuarios")
            .document(uid)
            .update(
                "progreso.$nombreCurso",
                datos
            )

    }
    private fun cargarContenido() {

        curso = arguments?.getSerializable("curso") as Curso

        if (curso.imagenUrl.isNotEmpty()) {

            Glide.with(requireContext())
                .load(curso.imagenUrl)
                .placeholder(R.drawable.img_hardware)
                .error(R.drawable.img_hardware)
                .into(imgPortada)

        } else {

            imgPortada.setImageResource(curso.imagen)

        }

        tvTitulo.text = curso.titulo

        tvDescripcion.text = curso.descripcion

        tvDatoImportante.text = curso.datoImportante

        tvContenido.text = curso.contenido

        tvSabiasQue.text = curso.sabiasQue

        tvConsejo.text = curso.consejo



        tiempoInicio = System.currentTimeMillis()

        scrollContenido.viewTreeObserver.addOnScrollChangedListener {

            if (cursoRegistrado)
                return@addOnScrollChangedListener

            val view = scrollContenido.getChildAt(0)

            val diff = view.bottom - (scrollContenido.height + scrollContenido.scrollY)

            if (diff <= 150) {

                val segundos =
                    (System.currentTimeMillis() - tiempoInicio) / 1000

                if (segundos >= 30) {

                    cursoRegistrado = true

                    guardarCursoLeido()

                    Toast.makeText(
                        requireContext(),
                        "✅ Curso completado",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

    }





    private fun mostrarMaterialComplementario() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_material,
            null
        )

        val cardPDF = dialogView.findViewById<CardView>(R.id.cardPDF)
        val cardVideo = dialogView.findViewById<CardView>(R.id.cardVideo)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarMaterial)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        cardPDF.setOnClickListener {

            val url = if (esFirestore) {
                urlPdf
            } else {
                curso.pdf
            }

            if (url.isNotEmpty()) {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )

            } else {

                Toast.makeText(
                    requireContext(),
                    "Este curso no tiene PDF disponible.",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

        cardVideo.setOnClickListener {

            val url = if (esFirestore) {
                urlVideo
            } else {
                curso.video
            }

            if (url.isNotEmpty()) {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )

            } else {

                Toast.makeText(
                    requireContext(),
                    "Este curso no tiene video disponible.",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

        dialog.show()

    }


}