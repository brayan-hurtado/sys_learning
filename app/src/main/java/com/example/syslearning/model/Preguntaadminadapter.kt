package com.example.syslearning.model

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.google.android.material.card.MaterialCardView

class PreguntaAdminAdapter(
    private val lista: List<Pregunta>,
    private val onLongClickEliminar: (Pregunta) -> Unit
) : RecyclerView.Adapter<PreguntaAdminAdapter.ViewHolder>() {

    class ViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val tvEnunciado: TextView = card.findViewById(R.id.tvEnunciadoAdmin)
        val tvOpcionA: TextView = card.findViewById(R.id.tvOpcionAAdmin)
        val tvOpcionB: TextView = card.findViewById(R.id.tvOpcionBAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pregunta_admin, parent, false) as MaterialCardView
        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pregunta = lista[position]

        holder.tvEnunciado.text = pregunta.enunciado
        holder.tvOpcionA.text = "A) ${pregunta.opcionA}"
        holder.tvOpcionB.text = "B) ${pregunta.opcionB}"

        val colorCorrecto = Color.parseColor("#2E7D32")
        val colorNormal = Color.parseColor("#333333")

        holder.tvOpcionA.setTextColor(if (pregunta.respuestaCorrecta == "A") colorCorrecto else colorNormal)
        holder.tvOpcionB.setTextColor(if (pregunta.respuestaCorrecta == "B") colorCorrecto else colorNormal)

        holder.card.setOnLongClickListener {
            onLongClickEliminar(pregunta)
            true
        }
    }
}