package com.example.syslearning

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CursoAdapter(

    private val listaCursos: List<Curso>,
    private val onClick: (Curso) -> Unit

) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

    class CursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imgCurso: ImageView = itemView.findViewById(R.id.imgCurso)

        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloCurso)

        val tvDescripcion: TextView =
            itemView.findViewById(R.id.tvDescripcionCurso)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CursoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.card_curso,
                parent,
                false
            )

        return CursoViewHolder(view)

    }



    override fun onBindViewHolder(
        holder: CursoViewHolder,
        position: Int
    )



    {

        val curso = listaCursos[position]

        Log.d("GLIDE", "imagenUrl = ${curso.imagenUrl}")

        if (curso.imagenUrl.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(curso.imagenUrl)
                .placeholder(R.drawable.ic_school)
                .error(R.drawable.ic_school)
                .into(holder.imgCurso)

        } else {

            holder.imgCurso.setImageResource(curso.imagen)

        }

        holder.tvTitulo.text = curso.titulo

        holder.tvDescripcion.text = curso.descripcion

        holder.itemView.setOnClickListener {

            onClick(curso)

        }

    }

    override fun getItemCount(): Int {

        return listaCursos.size

    }

}