package com.example.syslearning

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.PreguntaAdminAdapter
import com.example.syslearning.model.Pregunta
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Lista y crea preguntas para una actividad específica.
 * Recibe "actividadId" y "actividadTitulo" por argumentos.
 */
class PreguntasAdminFragment : Fragment(R.layout.fragment_preguntas_admin) {

    private lateinit var recycler: RecyclerView
    private lateinit var fabNuevaPregunta: FloatingActionButton
    private lateinit var tvTituloActividadPreguntas: TextView
    private lateinit var adapter: PreguntaAdminAdapter

    private val db = FirebaseFirestore.getInstance()
    private val listaPreguntas = mutableListOf<Pregunta>()

    private var actividadId: String = ""
    private var actividadTitulo: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actividadId = arguments?.getString("actividadId") ?: ""
        actividadTitulo = arguments?.getString("actividadTitulo") ?: ""

        recycler = view.findViewById(R.id.rvPreguntasAdmin)
        fabNuevaPregunta = view.findViewById(R.id.fabNuevaPregunta)
        tvTituloActividadPreguntas = view.findViewById(R.id.tvTituloActividadPreguntas)

        tvTituloActividadPreguntas.text = "Preguntas de: $actividadTitulo"

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = PreguntaAdminAdapter(listaPreguntas) { pregunta ->
            confirmarEliminarPregunta(pregunta)
        }

        recycler.adapter = adapter

        cargarPreguntas()

        fabNuevaPregunta.setOnClickListener {
            mostrarDialogoPregunta()
        }
    }

    private fun cargarPreguntas() {

        db.collection("preguntas")
            .whereEqualTo("actividadId", actividadId)
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                listaPreguntas.clear()

                for (doc in documentos) {
                    val pregunta = doc.toObject(Pregunta::class.java)
                    pregunta.id = doc.id
                    listaPreguntas.add(pregunta)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "No se pudieron cargar las preguntas",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun mostrarDialogoPregunta() {

        val vista = layoutInflater.inflate(R.layout.dialog_nueva_pregunta, null)

        val etEnunciado = vista.findViewById<TextInputEditText>(R.id.etEnunciadoPregunta)
        val etOpcionA = vista.findViewById<TextInputEditText>(R.id.etOpcionAPregunta)
        val etOpcionB = vista.findViewById<TextInputEditText>(R.id.etOpcionBPregunta)
        val radioGroup = vista.findViewById<RadioGroup>(R.id.radioGroupRespuestaCorrecta)
        val radioA = vista.findViewById<RadioButton>(R.id.radioOpcionA)
        val btnCancelar = vista.findViewById<Button>(R.id.btnCancelarPregunta)
        val btnGuardar = vista.findViewById<Button>(R.id.btnGuardarPregunta)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(vista)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {

            val enunciado = etEnunciado.text.toString().trim()
            val opcionA = etOpcionA.text.toString().trim()
            val opcionB = etOpcionB.text.toString().trim()
            val correcta = if (radioGroup.checkedRadioButtonId == radioA.id) "A" else "B"

            if (enunciado.isEmpty() || opcionA.isEmpty() || opcionB.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pregunta = hashMapOf(
                "actividadId" to actividadId,
                "enunciado" to enunciado,
                "opcionA" to opcionA,
                "opcionB" to opcionB,
                "respuestaCorrecta" to correcta,
                "explicacion" to "",
                "estado" to true
            )

            db.collection("preguntas")
                .add(pregunta)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Pregunta creada", Toast.LENGTH_SHORT).show()
                    cargarPreguntas()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "No se pudo crear la pregunta", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun confirmarEliminarPregunta(pregunta: Pregunta) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar pregunta")
            .setMessage("¿Eliminar esta pregunta?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->

                db.collection("preguntas")
                    .document(pregunta.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Pregunta eliminada", Toast.LENGTH_SHORT).show()
                        cargarPreguntas()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }
}