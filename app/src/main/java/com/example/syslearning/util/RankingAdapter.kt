package com.example.syslearning

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

/**
 * Filas de la tabla de posiciones. Con un podio arriba, se le pasa el
 * ranking sin los 3 primeros y [posicionInicial] = 4.
 */
class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private val usuarios = mutableListOf<RankingUsuario>()
    private var posicionInicial = 1
    private var uidActual: String? = null

    fun actualizarLista(
        nuevaLista: List<RankingUsuario>,
        posicionInicial: Int = 1,
        uidActual: String? = null
    ) {
        usuarios.clear()
        usuarios.addAll(nuevaLista)
        this.posicionInicial = posicionInicial
        this.uidActual = uidActual
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_usuario, parent, false)

        return RankingViewHolder(view as MaterialCardView)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {

        val usuario = usuarios[position]
        val puesto = posicionInicial + position
        val esYo = uidActual != null && usuario.uid == uidActual

        holder.tvPosicion.text = when (puesto) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> puesto.toString()
        }

        PodioRanking.pintarAvatar(holder.tvAvatar, usuario.nombre, esYo, borde = "#143C8A")

        holder.tvNombre.text = usuario.nombre

        holder.tvDetalle.text = buildString {
            append("📚 ${usuario.cursosCompletados}")
            append("   🧩 ${usuario.actividadesCompletadas}")
            if (usuario.intentosActividades > 0) {
                append("   🎯 ${usuario.precision} %")
                append("   🔁 ${usuario.intentosActividades}")
            }
            if (usuario.racha > 0) append("   🔥 ${usuario.racha}")
        }

        holder.tvPuntos.text = PodioRanking.formatear(usuario.puntos)

        // Resalta la fila del usuario que está viendo la tabla.
        holder.tvTu.visibility = if (esYo) View.VISIBLE else View.GONE
        holder.tvTu.background = GradientDrawable().apply {
            cornerRadius = 40f
            setColor(Color.parseColor("#143C8A"))
        }

        holder.card.setCardBackgroundColor(Color.parseColor(if (esYo) "#EAF6FF" else "#FFFFFF"))
        holder.card.strokeColor = Color.parseColor(if (esYo) "#42C8F5" else "#E3E8F0")
        holder.card.strokeWidth =
            ((if (esYo) 2 else 1) * holder.card.resources.displayMetrics.density).toInt()

        holder.card.contentDescription =
            "Puesto $puesto, ${usuario.nombre}, ${usuario.puntos} puntos"
    }

    override fun getItemCount(): Int = usuarios.size

    class RankingViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val tvPosicion: TextView = card.findViewById(R.id.tvRankingPosicion)
        val tvAvatar: TextView = card.findViewById(R.id.tvRankingAvatar)
        val tvNombre: TextView = card.findViewById(R.id.tvRankingNombre)
        val tvTu: TextView = card.findViewById(R.id.tvRankingTu)
        val tvDetalle: TextView = card.findViewById(R.id.tvRankingDetalle)
        val tvPuntos: TextView = card.findViewById(R.id.tvRankingPuntos)
    }
}
