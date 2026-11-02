package com.example.syslearning.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.syslearning.R
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Detalle de un aula: nombre, código e indicadores básicos.
 *
 * Los estudiantes inscritos se identifican porque el documento
 * "usuarios/{uid}" tiene un campo "aulas" (List<String>) que contiene
 * el CÓDIGO del aula (ej. "SYS-SMQM9") — por eso se usa
 * whereArrayContains en vez de whereEqualTo.
 *
 * "Cursos" y "Actividades" se calculan agregando el progreso de los
 * estudiantes de esta aula (cuántos cursos distintos tienen marcado
 * "completado" al menos una vez entre todos, y cuántas marcas de
 * "completado" en total). Si tu app maneja "actividades" como algo
 */
class DetalleAulaFragment : Fragment(R.layout.fragment_detalle_aula) {

    private lateinit var txtNombreAula: TextView
    private lateinit var txtCodigoAula: TextView
    private lateinit var txtCantidadEstudiantes: TextView
    private lateinit var txtCantidadCursos: TextView
    private lateinit var txtCantidadActividades: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtNombreAula = view.findViewById(R.id.txtNombreAula)
        txtCodigoAula = view.findViewById(R.id.txtCodigoAula)
        txtCantidadEstudiantes = view.findViewById(R.id.txtCantidadEstudiantes)
        txtCantidadCursos = view.findViewById(R.id.txtCantidadCursos)
        txtCantidadActividades = view.findViewById(R.id.txtCantidadActividades)

        val idAula = arguments?.getString("idAula") ?: return

        cargarAula(idAula)
    }

    private fun cargarAula(idAula: String) {

        db.collection("aulas")
            .document(idAula)
            .get()
            .addOnSuccessListener { doc ->

                if (!isAdded) return@addOnSuccessListener

                val nombre = doc.getString("nombre") ?: "Aula"
                val codigo = doc.getString("codigo") ?: ""

                txtNombreAula.text = nombre
                txtCodigoAula.text = codigo.ifEmpty { "---" }

                if (codigo.isNotEmpty()) {
                    cargarConteos(codigo)
                }
            }
    }

    private fun cargarConteos(codigoAula: String) {

        db.collection("usuarios")
            .whereArrayContains("aulas", codigoAula)
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                txtCantidadEstudiantes.text = documentos.size().toString()

                val cursosConProgreso = mutableSetOf<String>()
                var totalCompletados = 0

                documentos.forEach { estudiante ->

                    val progreso = estudiante.get("progreso") as? Map<*, *>

                    progreso?.forEach { entrada ->

                        val nombreCurso = entrada.key.toString()
                        val datos = entrada.value as? Map<*, *>

                        if (datos?.get("completado") == true) {
                            cursosConProgreso.add(nombreCurso)
                            totalCompletados++
                        }
                    }
                }

                txtCantidadCursos.text = cursosConProgreso.size.toString()
                txtCantidadActividades.text = totalCompletados.toString()
            }
    }
}