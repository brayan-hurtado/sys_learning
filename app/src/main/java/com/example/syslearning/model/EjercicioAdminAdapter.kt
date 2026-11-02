package com.example.syslearning.model

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.actividades.EvaluadorEjercicio
import com.google.android.material.card.MaterialCardView

/**
 * Lista de ejercicios de una micro actividad en el panel de administrador.
 * Muestra el paso, el tipo, la respuesta correcta y avisa si al ejercicio
 * le falta la pista o la explicación (necesarias para RF 08).
 */
class EjercicioAdminAdapter(
    private val lista: List<Ejercicio>,
    private val onClick: (Ejercicio) -> Unit,
    private val onLongClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<EjercicioAdminAdapter.ViewHolder>() {

    class ViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val tvPaso: TextView = card.findViewById(R.id.tvPasoEjercicioAdmin)
        val tvTipo: TextView = card.findViewById(R.id.tvTipoEjercicioAdmin)
        val tvEstado: TextView = card.findViewById(R.id.tvEstadoEjercicioAdmin)
        val tvEnunciado: TextView = card.findViewById(R.id.tvEnunciadoEjercicioAdmin)
        val tvRespuesta: TextView = card.findViewById(R.id.tvRespuestaEjercicioAdmin)
        val tvAdvertencia: TextView = card.findViewById(R.id.tvAdvertenciaEjercicioAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio_admin, parent, false) as MaterialCardView
        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ejercicio = lista[position]
        val paso = ejercicio.pasoEjercicio()
        val tipo = ejercicio.tipoEjercicio()

        holder.tvPaso.text = if (paso == PasoEjercicio.PRACTICA) "PRÁCTICA" else "COMPROBACIÓN"
        holder.tvPaso.background = GradientDrawable().apply {
            cornerRadius = 40f
            setColor(Color.parseColor(if (paso == PasoEjercicio.PRACTICA) "#1565C0" else "#6A1B9A"))
        }

        holder.tvTipo.text = "${tipo.sigla} · ${tipo.etiqueta} · orden ${ejercicio.orden}"

        if (ejercicio.activo) {
            holder.tvEstado.text = "Activo"
            holder.tvEstado.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            holder.tvEstado.text = "Oculto"
            holder.tvEstado.setTextColor(Color.parseColor("#B0B7C3"))
        }

        holder.tvEnunciado.text = ejercicio.enunciado

        holder.tvRespuesta.text = "✔ " + EvaluadorEjercicio.solucion(ejercicio)
            .replace("\n", "  ")

        val faltantes = mutableListOf<String>()
        if (ejercicio.pista1.isBlank()) faltantes.add("pista")
        if (ejercicio.explicacion.isBlank()) faltantes.add("explicación")
        val error = EvaluadorEjercicio.validarParaGuardar(ejercicio)

        val advertencia = when {
            error != null -> "⚠ $error"
            faltantes.isNotEmpty() -> "⚠ Falta ${faltantes.joinToString(" y ")}: se usará un texto genérico."
            else -> null
        }

        holder.tvAdvertencia.visibility = if (advertencia != null) View.VISIBLE else View.GONE
        holder.tvAdvertencia.text = advertencia ?: ""

        holder.card.setOnClickListener { onClick(ejercicio) }
        holder.card.setOnLongClickListener {
            onLongClick(ejercicio)
            true
        }
    }
}
