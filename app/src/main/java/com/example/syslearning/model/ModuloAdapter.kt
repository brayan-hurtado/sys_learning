package com.example.syslearning.model

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.actividades.ProgresoModulos.EstadoModulo
import com.google.android.material.card.MaterialCardView

/**
 * Tarjetas de módulos: número, título, barra de avance, actividades
 * completadas y estado del reto final.
 */
class ModuloAdapter(
    private val onClick: (EstadoModulo) -> Unit
) : RecyclerView.Adapter<ModuloAdapter.ViewHolder>() {

    private val lista = mutableListOf<EstadoModulo>()

    private val colores = listOf("#3E7BFA", "#8B5CF6", "#FF7A45", "#00B8D9", "#E91E63")

    fun actualizar(nuevos: List<EstadoModulo>) {
        lista.clear()
        lista.addAll(nuevos)
        notifyDataSetChanged()
    }

    class ViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val tvNumero: TextView = card.findViewById(R.id.tvNumeroModulo)
        val tvEtiqueta: TextView = card.findViewById(R.id.tvEtiquetaModulo)
        val tvTitulo: TextView = card.findViewById(R.id.tvTituloModulo)
        val tvEstado: TextView = card.findViewById(R.id.tvEstadoModulo)
        val tvDescripcion: TextView = card.findViewById(R.id.tvDescripcionModulo)
        val progreso: ProgressBar = card.findViewById(R.id.progresoModulo)
        val tvAvance: TextView = card.findViewById(R.id.tvAvanceModulo)
        val tvReto: TextView = card.findViewById(R.id.tvRetoModulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_modulo, parent, false) as MaterialCardView
        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val estado = lista[position]
        val modulo = estado.modulo

        val color = when {
            estado.completado -> "#43A047"
            !estado.desbloqueado -> "#B0B7C3"
            else -> colores[(modulo.numero - 1).coerceAtLeast(0) % colores.size]
        }

        holder.tvNumero.text = if (estado.completado) "✓" else modulo.numero.toString()
        holder.tvNumero.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
        }

        holder.tvEtiqueta.text = "MÓDULO ${modulo.numero}"
        holder.tvTitulo.text = modulo.titulo
        holder.tvDescripcion.text = modulo.descripcion
        holder.tvDescripcion.visibility = if (modulo.descripcion.isBlank()) View.GONE else View.VISIBLE

        holder.progreso.progress = estado.porcentaje
        holder.tvAvance.text = "${estado.completadas} de ${estado.total} actividades · ${estado.porcentaje} %"

        holder.tvEstado.text = when {
            estado.completado -> "🏅"
            !estado.desbloqueado -> "🔒"
            else -> "›"
        }
        holder.tvEstado.setTextColor(Color.parseColor("#143C8A"))

        // Estado del reto final
        val reto = estado.reto
        val (textoReto, fondoReto, colorTexto) = when {
            reto == null -> Triple("Sin reto final", "#F2F3F6", "#8A93A6")
            reto.estado == EstadoActividad.COMPLETADA -> Triple("🏆 Reto superado", "#E8F5E9", "#2E7D32")
            reto.estado == EstadoActividad.DISPONIBLE -> Triple("🏆 Reto disponible", "#FFF3CD", "#B26A00")
            else -> Triple("🔒 Reto final", "#F2F3F6", "#8A93A6")
        }
        holder.tvReto.text = textoReto
        holder.tvReto.setTextColor(Color.parseColor(colorTexto))
        holder.tvReto.background = GradientDrawable().apply {
            cornerRadius = 40f
            setColor(Color.parseColor(fondoReto))
        }

        holder.card.alpha = if (estado.desbloqueado) 1f else 0.6f
        holder.card.setOnClickListener { onClick(estado) }
        holder.card.contentDescription =
            "Módulo ${modulo.numero}, ${modulo.titulo}, ${estado.porcentaje} por ciento" +
                    if (!estado.desbloqueado) ", bloqueado" else ""
    }
}
