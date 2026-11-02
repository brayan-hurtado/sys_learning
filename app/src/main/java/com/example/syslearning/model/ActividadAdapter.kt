package com.example.syslearning.model

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.EstadoActividad
import com.example.syslearning.ui.AnilloProgresoView
import com.google.android.material.card.MaterialCardView


class ActividadAdapter(

    lista: List<Actividad>,

    private val onClick: (Actividad) -> Unit

) : RecyclerView.Adapter<ActividadAdapter.ViewHolder>() {

    private val lista = lista.toMutableList()

    /** Reemplaza las actividades sin crear un adapter nuevo. */
    fun actualizar(nuevas: List<Actividad>) {
        lista.clear()
        lista.addAll(nuevas)
        notifyDataSetChanged()
    }

    // Paleta que van rotando las tarjetas DISPONIBLES, para que la
    // cuadrícula no se vea monocroma si hay varias a la vez.
    private val paletaDisponible = listOf(
        "#FF7A45", // naranja (como "Walk")
        "#3E7BFA", // azul (como "Water")
        "#8B5CF6"  // morado
    )

    private val colorCompletada = "#43A047"
    private val colorBloqueada = "#F2F3F6"

    class ViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val contenedorIcono: FrameLayout = card.findViewById(R.id.contenedorIcono)
        val imgEstado: ImageView = card.findViewById(R.id.imgEstadoActividad)
        val tvTitulo: TextView = card.findViewById(R.id.tvTituloActividad)
        val tvSubtitulo: TextView = card.findViewById(R.id.tvSubtituloActividad)
        val contenedorAnillo: FrameLayout = card.findViewById(R.id.contenedorAnillo)
        val anillo: AnilloProgresoView = card.findViewById(R.id.anilloProgreso)
        val tvXp: TextView = card.findViewById(R.id.tvXpAnillo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad_bento, parent, false) as MaterialCardView

        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val actividad = lista[position]

        holder.tvTitulo.text = actividad.titulo

        when (actividad.estado) {

            EstadoActividad.DISPONIBLE -> {

                val color = paletaDisponible[position % paletaDisponible.size]

                holder.card.setCardBackgroundColor(Color.parseColor(color))

                holder.tvTitulo.setTextColor(Color.WHITE)
                holder.tvSubtitulo.setTextColor(Color.parseColor("#E8ECF3"))
                holder.tvSubtitulo.text = "Disponible"

                holder.imgEstado.setImageResource(R.drawable.ic_play)
                holder.imgEstado.setColorFilter(Color.WHITE)
                holder.contenedorIcono.background = crearCirculo("#33FFFFFF")

                holder.contenedorAnillo.visibility = View.VISIBLE
                holder.anillo.setProgreso(actividad.xp.coerceAtMost(100) / 100f)
                holder.tvXp.text = "${actividad.xp}\nXP"
            }

            EstadoActividad.COMPLETADA -> {

                holder.card.setCardBackgroundColor(Color.parseColor(colorCompletada))

                holder.tvTitulo.setTextColor(Color.WHITE)
                holder.tvSubtitulo.setTextColor(Color.parseColor("#E4F5E6"))
                holder.tvSubtitulo.text = "Completada"

                holder.imgEstado.setImageResource(R.drawable.ic_check)
                holder.imgEstado.setColorFilter(Color.WHITE)
                holder.contenedorIcono.background = crearCirculo("#33FFFFFF")

                holder.contenedorAnillo.visibility = View.GONE
            }

            EstadoActividad.BLOQUEADA -> {

                holder.card.setCardBackgroundColor(Color.parseColor(colorBloqueada))

                holder.tvTitulo.setTextColor(Color.parseColor("#8A93A6"))
                holder.tvSubtitulo.setTextColor(Color.parseColor("#B0B7C3"))
                holder.tvSubtitulo.text = "Bloqueada"

                holder.imgEstado.setImageResource(R.drawable.ic_lock)
                holder.imgEstado.setColorFilter(Color.parseColor("#8A93A6"))
                holder.contenedorIcono.background = crearCirculo("#E3E6EC")

                holder.contenedorAnillo.visibility = View.GONE
            }
        }

        // El reto final del módulo se distingue en dorado.
        if (NivelActividad.desde(actividad.nivel) == NivelActividad.INTEGRACION) {
            when (actividad.estado) {
                EstadoActividad.DISPONIBLE -> {
                    holder.card.setCardBackgroundColor(Color.parseColor("#F5B700"))
                    holder.tvSubtitulo.text = "🏆 Reto final"
                }
                EstadoActividad.COMPLETADA -> holder.tvSubtitulo.text = "🏆 Reto superado"
                EstadoActividad.BLOQUEADA -> holder.tvSubtitulo.text = "🔒 Reto final"
            }
        }

        holder.card.setOnClickListener {

            if (actividad.estado != EstadoActividad.BLOQUEADA) {
                onClick(actividad)
            } else {
                val mensaje = if (NivelActividad.desde(actividad.nivel) == NivelActividad.INTEGRACION) {
                    "🔒 Completa las demás actividades del módulo para abrir el reto final"
                } else {
                    "🔒 Completa la actividad anterior para desbloquear esta"
                }
                android.widget.Toast.makeText(holder.card.context, mensaje, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearCirculo(colorHex: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(colorHex))
        }
    }
}