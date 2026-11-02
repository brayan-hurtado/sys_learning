package com.example.syslearning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.ProgressBar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage


class DetalleUsuarioFragment : Fragment(R.layout.fragment_detalle_usuario) {

    private lateinit var imgPerfil: CircleImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtCorreo: TextView
    private lateinit var chipRol: Chip
    private lateinit var tvCursosCompletados: TextView
    private lateinit var progresoGeneral: ProgressBar
    private lateinit var tvPorcentaje: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvDocumento: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgPerfil = view.findViewById(R.id.imgPerfil)
        txtNombre = view.findViewById(R.id.txtNombre)
        txtCorreo = view.findViewById(R.id.txtCorreo)
        chipRol = view.findViewById(R.id.chipRol)
        tvCursosCompletados = view.findViewById(R.id.tvCursosCompletados)
        progresoGeneral = view.findViewById(R.id.progresoGeneral)
        tvPorcentaje = view.findViewById(R.id.tvPorcentaje)
        tvTelefono = view.findViewById(R.id.tvTelefono)
        tvDocumento = view.findViewById(R.id.tvDocumento)

        val uid = arguments?.getString("uid")

        if (uid != null) {
            cargarUsuario(uid)
        }
    }



    private fun cargarUsuario(uid: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    mostrarProgresoCero()
                    return@addOnSuccessListener
                }

                txtNombre.text = document.getString("nombre") ?: "Sin nombre"
                txtCorreo.text = document.getString("correo") ?: "Sin correo"
                mostrarTelefono(document)
                mostrardocumento(document)


                val rol = document.getString("rol") ?: ""
                chipRol.text = rol

                if (rol.equals("admin", true)) {
                    chipRol.setChipBackgroundColorResource(R.color.azul_admin)
                } else {
                    chipRol.setChipBackgroundColorResource(R.color.verde_estudiante)
                }

                cargarFoto(document.getString("fotoPerfil"))


                val progreso = document.get("progreso") as? Map<*, *>

                val cursosCompletados = progreso?.count { entrada ->
                    val datos = entrada.value as? Map<*, *>
                    datos?.get("completado") == true
                } ?: 0

                val totalCursos = Cursos.todosLosCursos.size

                val porcentaje = if (totalCursos > 0) {
                    (cursosCompletados * 100) / totalCursos
                } else {
                    0
                }

                progresoGeneral.max = 100
                progresoGeneral.progress = porcentaje

                tvCursosCompletados.text =
                    "$cursosCompletados de $totalCursos cursos completados"

                tvPorcentaje.text = "$porcentaje% completado"

            }
            .addOnFailureListener {
                mostrarProgresoCero()
            }
    }

    private fun mostrarTelefono(document: DocumentSnapshot) {
        val telefono = document.getString("telefono") ?: "Sin teléfono registrado"
        tvTelefono.text = telefono




    }

    private fun mostrardocumento(document: DocumentSnapshot) {
        val documento = document.getString("documento") ?: "Sin numero de Documento"
        tvDocumento.text = documento




    }

    private fun cargarFoto(foto: String?) {
        val rutaFoto = foto?.trim()

        if (rutaFoto.isNullOrEmpty()) {
            return
        }

        when {
            // URL de descarga normal de Firebase Storage
            rutaFoto.startsWith("https://") || rutaFoto.startsWith("http://") -> {
                mostrarFoto(rutaFoto)
            }

            // URL de Storage: gs://bucket/ruta/imagen.jpg
            rutaFoto.startsWith("gs://") -> {
                FirebaseStorage.getInstance()
                    .getReferenceFromUrl(rutaFoto)
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        mostrarFoto(uri.toString())
                    }
            }

            // Ruta guardada como: fotos/uid.jpg
            else -> {
                FirebaseStorage.getInstance()
                    .reference
                    .child(rutaFoto)
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        mostrarFoto(uri.toString())
                    }
            }
        }
    }

    private fun mostrarFoto(url: String) {
        if (!isAdded) return

        Glide.with(imgPerfil)
            .load(url)
            .into(imgPerfil)
    }



    private fun mostrarProgresoCero() {
        tvCursosCompletados.text = "0 cursos completados"
        tvPorcentaje.text = "0% completado"
        progresoGeneral.progress = 0
    }

    companion object {
        fun newInstance(uid: String): DetalleUsuarioFragment {
            return DetalleUsuarioFragment().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                }
            }
        }
    }
}