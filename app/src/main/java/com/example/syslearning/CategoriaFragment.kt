package com.example.syslearning

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.model.CursoFirestore
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

class   CategoriaFragment : Fragment() {

    private lateinit var tvCategoria: TextView
    private lateinit var rvCursos: RecyclerView

    private var categoria = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_categoria,
            container,
            false
        )

        tvCategoria = view.findViewById(R.id.tvCategoria)
        rvCursos = view.findViewById(R.id.rvCursos)

        categoria = arguments?.getString("categoria") ?: ""

        tvCategoria.text = categoria

        cargarGuias()

        return view
    }

    private fun cargarGuias() {

        val listaCursos = mutableListOf<Curso>()

        // Agregar cursos locales
        when (categoria) {

            "Redes" ->
                listaCursos.addAll(Cursos.listaRedes)

            "Ofimática" ->
                listaCursos.addAll(Cursos.listaOfimatica)

            "Seguridad" ->
                listaCursos.addAll(Cursos.listaSeguridad)

            "Programación" ->
                listaCursos.addAll(Cursos.listaProgramacion)

        }

        rvCursos.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CursoAdapter(listaCursos) { curso ->

            val fragment = ContenidoFragment()

            val bundle = Bundle()

            bundle.putSerializable("curso", curso)

            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .addToBackStack(null)
                .commit()

        }

        rvCursos.adapter = adapter

        // Ahora cargar los cursos creados por el administrador

        FirebaseFirestore.getInstance()

            .collection("cursos")

            .whereEqualTo("categoria", categoria)

            .get()

            .addOnSuccessListener { documents ->

                for (doc in documents) {

                    val cursoFirestore =
                        doc.toObject(CursoFirestore::class.java)

                    listaCursos.add(

                        Curso(

                            titulo = cursoFirestore.titulo,
                            descripcion = cursoFirestore.descripcion,
                            contenido = cursoFirestore.contenido,
                            datoImportante = cursoFirestore.datoImportante,
                            sabiasQue = cursoFirestore.sabiasQue,
                            consejo = cursoFirestore.consejo,

                            // Imagen remota
                            imagen = 0,
                            imagenUrl = cursoFirestore.imagen,

                            pdf = cursoFirestore.pdf,
                            video = cursoFirestore.video

                        )

                    )

                }

                adapter.notifyDataSetChanged()

            }

    }



}