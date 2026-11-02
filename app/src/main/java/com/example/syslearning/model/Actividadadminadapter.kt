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

    /** Número de módulo -> título, para mostrarlo en cada fila. */
    var nombresModulos: Map<Int, String> = emptyMap()

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

        val nombreModulo = nombresModulos[actividad.modulo]?.let { " $it" } ?: ""
        val esReto = NivelActividad.desde(actividad.nivel) == NivelActividad.INTEGRACION
        holder.tvTitulo.text = (if (esReto) "🏆 " else "") + actividad.titulo
        holder.tvDetalle.text =
            "Módulo ${actividad.modulo}$nombreModulo · ${if (esReto) "Reto final" else NivelActividad.desde(actividad.nivel).etiqueta} · orden ${actividad.orden}\n" +
                    if (actividad.aulasAsignadas.isEmpty()) "🌐 Global" else "🏫 ${actividad.aulasAsignadas.joinToString(", ")}"

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