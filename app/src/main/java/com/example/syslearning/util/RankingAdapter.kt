package com.example.syslearning

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private val usuarios = mutableListOf<RankingUsuario>()

    fun actualizarLista(nuevaLista: List<RankingUsuario>) {
        usuarios.clear()
        usuarios.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_usuario, parent, false)

        return RankingViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val usuario = usuarios[position]

        holder.tvNombre.text = usuario.nombre
        holder.tvRacha.text = "${usuario.racha}"
        holder.tvCursos.text = "${usuario.cursosCompletados}"

        holder.tvPromedio.text = usuario.puntos.toString()
    }

    override fun getItemCount(): Int = usuarios.size

    class RankingViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvRankingNombre)
        val tvRacha: TextView = view.findViewById(R.id.tvRankingRacha)
        val tvCursos: TextView = view.findViewById(R.id.tvRankingCursos)
        val tvPromedio: TextView = view.findViewById(R.id.tvRankingPromedio)
    }
}