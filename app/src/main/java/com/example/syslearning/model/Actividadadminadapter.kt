package com.example.syslearning.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.NivelActividad
import com.google.android.material.card.MaterialCardView

class ActividadAdminAdapter(
    private val lista: List<Actividad>,
    private val onClick: (Actividad) -> Unit,
    private val onLongClick: (Actividad) -> Unit
) : RecyclerView.Adapter<ActividadAdminAdapter.ViewHolder>() {

    class ViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val tvTitulo: TextView = card.findViewById(R.id.tvTituloActividadAdmin)
        val tvDetalle: TextView = card.findViewById(R.id.tvDetalleActividadAdmin)
        val tvEstado: TextView = card.findViewById(R.id.tvEstadoActividadAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad_admin, parent, false) as MaterialCardView
        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val actividad = lista[position]

        holder.tvTitulo.text = actividad.titulo
        holder.tvDetalle.text =
            "Módulo ${actividad.modulo} · ${NivelActividad.desde(actividad.nivel).etiqueta} · orden ${actividad.orden}"

        if (actividad.activo) {
            holder.tvEstado.text = "Activa"
            holder.tvEstado.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            holder.tvEstado.text = "Oculta"
            holder.tvEstado.setTextColor(Color.parseColor("#B0B7C3"))
        }

        holder.card.setOnClickListener { onClick(actividad) }

        holder.card.setOnLongClickListener {
            onLongClick(actividad)
            true
        }
    }
}