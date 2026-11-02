package com.example.syslearning.fragments

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.NuevoCurso
import com.example.syslearning.R
import com.example.syslearning.adapter.CursoFirestoreAdapter
import com.example.syslearning.model.CursoFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.syslearning.ContenidoFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.appcompat.app.AlertDialog
import com.example.syslearning.Cursos
import com.google.firebase.storage.FirebaseStorage
import com.example.syslearning.AulaActual

class CursosFragment : Fragment(R.layout.fragment_cursos) {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CursoFirestoreAdapter
    private lateinit var edtBuscar: EditText
    private lateinit var fabNuevoCurso: FloatingActionButton

    private val listaCursos = mutableListOf<CursoFirestore>()

    private val nuevoCursoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarCursos()
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvCursos)
        edtBuscar = view.findViewById(R.id.edtBuscarCurso)
        fabNuevoCurso = view.findViewById(R.id.fabNuevoCurso)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = CursoFirestoreAdapter(

            listaCursos,

            { curso ->

                val fragment = ContenidoFragment()
                val bundle = Bundle()

                if (curso.id.startsWith("local_")) {

                    val indice = curso.id.removePrefix("local_").toInt()

                    bundle.putBoolean("firestore", false)
                    bundle.putSerializable(
                        "curso",
                        Cursos.todosLosCursos[indice]
                    )

                } else {

                    bundle.putBoolean("firestore", true)
                    bundle.putString("idCurso", curso.id)

                }

                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameAdmin, fragment)
                    .addToBackStack(null)
                    .commit()

            },

            {  curso ->

                if (curso.id.startsWith("local_")) {

                    Toast.makeText(
                        requireContext(),
                        "Los cursos del sistema no se pueden eliminar.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    mostrarDialogoEliminar(curso)

                }

            }

        )

        recycler.adapter = adapter



        cargarCursos()



        fabNuevoCurso.setOnClickListener {

            it.animate()
                .rotationBy(90f)
                .setDuration(150)
                .withEndAction {

                    nuevoCursoLauncher.launch(
                        Intent(requireContext(), NuevoCurso::class.java)
                    )

                    it.rotation = 0f
                }

        }

        edtBuscar.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                adapter.filtrar(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

        })



    }

    private fun mostrarDialogoEliminar(curso: CursoFirestore) {

        AlertDialog.Builder(requireContext())

            .setTitle("Eliminar curso")

            .setMessage(
                "¿Deseas eliminar el curso \"${curso.titulo}\"?\n\nEsta acción no se puede deshacer."
            )

            .setNegativeButton("Cancelar", null)

            .setPositiveButton("Eliminar") { _, _ ->

                eliminarCurso(curso)

            }

            .show()

    }

    private fun eliminarCurso(curso: CursoFirestore) {

        val firestore = FirebaseFirestore.getInstance()

        val storage = FirebaseStorage.getInstance()

        storage
            .getReferenceFromUrl(curso.imagen)
            .delete()
            .addOnCompleteListener {

                firestore
                    .collection("cursos")
                    .document(curso.id)
                    .delete()
                    .addOnSuccessListener {

                        Toast.makeText(
                            requireContext(),
                            "Curso eliminado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        cargarCursos()

                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(
                            requireContext(),
                            e.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()

                    }

            }

    }

    private fun cargarCursos() {

        listaCursos.clear()

        // ==============================
        // Cursos incluidos en la aplicación (siempre visibles, son currícula base)
        // ==============================

        Cursos.todosLosCursos.forEachIndexed { index, curso ->

            listaCursos.add(
                CursoFirestore(
                    id = "local_$index",
                    titulo = curso.titulo,
                    descripcion = curso.descripcion,
                    contenido = curso.contenido,
                    datoImportante = curso.datoImportante,
                    sabiasQue = curso.sabiasQue,
                    consejo = curso.consejo,
                    categoria = "Sistema",
                    nivel = "-",
                    imagen = "",
                    pdf = curso.pdf,
                    video = curso.video,
                    creadoPor = "SysLearning"
                )
            )
        }

        // ==============================
        // Cursos creados en Firestore, filtrados por el aula activa (si hay)
        // ==============================

        val codigoAula = if (AulaActual.hayFiltro()) AulaActual.codigo else null

        FirebaseFirestore.getInstance()
            .collection("cursos")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentos ->

                for (doc in documentos) {

                    val curso = doc.toObject(CursoFirestore::class.java)
                    curso.id = doc.id

                    val esGlobal = curso.aulasAsignadas.isEmpty()
                    val perteneceAlAula = codigoAula != null && curso.aulasAsignadas.contains(codigoAula)

                    // Sin filtro: se ven todos.
                    // Con filtro de aula: solo los globales + los asignados a esa aula.
                    if (codigoAula == null || esGlobal || perteneceAlAula) {
                        listaCursos.add(curso)
                    }
                }

                Log.d("CURSOS", "TOTAL = ${listaCursos.size}")

                listaCursos.forEach {
                    Log.d("CURSOS", "ID=${it.id}  TITULO=${it.titulo}")
                }

                adapter.actualizarLista(listaCursos.toList())


            }
    }



}