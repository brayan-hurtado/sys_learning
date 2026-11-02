package com.example.syslearning.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.CursoFirestore
import java.util.Locale

class CursoFirestoreAdapter(

    private val lista: MutableList<CursoFirestore>,

    private val onClick: (CursoFirestore) -> Unit,

    private val onLongClick: (CursoFirestore) -> Unit

) : RecyclerView.Adapter<CursoFirestoreAdapter.ViewHolder>() {

    private val listaCompleta = mutableListOf<CursoFirestore>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombre = view.findViewById<TextView>(R.id.txtNombreCurso)
        val categoria = view.findViewById<TextView>(R.id.txtCategoria)
        val nivel = view.findViewById<TextView>(R.id.txtNivel)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)

        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val curso = lista[position]

        holder.nombre.text = curso.titulo
        holder.categoria.text = curso.categoria
        holder.nivel.text = curso.nivel

        holder.itemView.setOnClickListener {
            onClick(curso)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(curso)
            true
        }
    }

    fun actualizarLista(nuevaLista: List<CursoFirestore>) {

        listaCompleta.clear()
        listaCompleta.addAll(nuevaLista)

        lista.clear()
        lista.addAll(nuevaLista)

        notifyDataSetChanged()
    }

    fun filtrar(texto: String) {

        val busqueda = texto.lowercase(Locale.getDefault())

        lista.clear()

        if (busqueda.isEmpty()) {

            lista.addAll(listaCompleta)

        } else {

            lista.addAll(
                listaCompleta.filter {

                    it.titulo.lowercase(Locale.getDefault()).contains(busqueda) ||
                            it.categoria.lowercase(Locale.getDefault()).contains(busqueda) ||
                            it.nivel.lowercase(Locale.getDefault()).contains(busqueda)

                }
            )

        }

        notifyDataSetChanged()
    }
}


