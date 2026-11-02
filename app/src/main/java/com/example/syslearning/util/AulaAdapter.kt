package com.example.syslearning.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.Aula

class AulaAdapter(

    private val lista: MutableList<Aula>,

    private val onClick: (Aula) -> Unit,

    private val onLongClick: (Aula) -> Unit

) : RecyclerView.Adapter<AulaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombre = view.findViewById<TextView>(R.id.txtNombreAula)
        val codigo = view.findViewById<TextView>(R.id.txtCodigoAula)
        val cantidad = view.findViewById<TextView>(R.id.txtCantidad)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aula, parent, false)

        return ViewHolder(vista)

    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val aula = lista[position]

        holder.nombre.text = aula.nombre

        holder.codigo.text = "Código: ${aula.codigo}"

        holder.cantidad.text = "0 estudiantes"

        holder.itemView.setOnClickListener {

            onClick(aula)

        }

        holder.itemView.setOnLongClickListener {

            onLongClick(aula)

            true

        }

    }

}