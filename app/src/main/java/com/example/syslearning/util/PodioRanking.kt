package com.example.syslearning

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

/**
 * Llena el podio (vista_podio_ranking.xml) con los 3 primeros del ranking
 * y comparte el estilo de avatar con RankingAdapter.
 */
object PodioRanking {

    private val coloresAvatar = listOf(
        "#3E7BFA", "#8B5CF6", "#FF7A45", "#43A047", "#00B8D9", "#E91E63", "#5C6BC0", "#F59E0B"
    )

    private val coloresBase = mapOf(1 to "#F5B700", 2 to "#90A4C8", 3 to "#C77D43")

    private val formatoNumero: NumberFormat = NumberFormat.getIntegerInstance(Locale("es", "CO"))

    fun formatear(puntos: Int): String = formatoNumero.format(puntos)

    /** Hasta 2 iniciales del nombre: "Brayan Hurtado" -> "BH". */
    fun iniciales(nombre: String): String {
        val partes = nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            partes.isEmpty() -> "?"
            partes.size == 1 -> partes[0].take(2).uppercase()
            else -> "${partes[0].first()}${partes[1].first()}".uppercase()
        }
    }

    /** Círculo de color estable para cada nombre (+ borde si es el usuario actual). */
    fun pintarAvatar(tv: TextView, nombre: String, esUsuarioActual: Boolean, borde: String = "#FFFFFF") {
        val indice = (nombre.lowercase().hashCode() and 0x7fffffff) % coloresAvatar.size
        tv.text = iniciales(nombre)
        tv.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(coloresAvatar[indice]))
            if (esUsuarioActual) {
                setStroke((3 * tv.resources.displayMetrics.density).toInt(), Color.parseColor(borde))
            }
        }
    }

    /**
     * Muestra el podio. Si hay menos de 3 usuarios, los puestos vacíos se
     * ocultan. Devuelve cuántos puestos quedaron en el podio.
     */
    fun mostrar(raiz: View, ranking: List<RankingUsuario>, uidActual: String?): Int {

        val puestos = listOf(
            Triple(R.id.podio1, R.id.podio1Avatar, Triple(R.id.podio1Nombre, R.id.podio1Puntos, R.id.podio1Base)),
            Triple(R.id.podio2, R.id.podio2Avatar, Triple(R.id.podio2Nombre, R.id.podio2Puntos, R.id.podio2Base)),
            Triple(R.id.podio3, R.id.podio3Avatar, Triple(R.id.podio3Nombre, R.id.podio3Puntos, R.id.podio3Base))
        )

        val radio = 14 * raiz.resources.displayMetrics.density

        puestos.forEachIndexed { indice, (idPuesto, idAvatar, textos) ->

            val (idNombre, idPuntos, idBase) = textos
            val puesto = raiz.findViewById<View>(idPuesto)
            val usuario = ranking.getOrNull(indice)

            if (usuario == null) {
                // Se deja el espacio (INVISIBLE) para que el podio no se descuadre.
                puesto.visibility = View.INVISIBLE
                return@forEachIndexed
            }

            puesto.visibility = View.VISIBLE

            val esYo = uidActual != null && usuario.uid == uidActual

            pintarAvatar(raiz.findViewById(idAvatar), usuario.nombre, esYo, borde = "#FFD54F")

            raiz.findViewById<TextView>(idNombre).text =
                if (esYo) "Tú" else usuario.nombre.substringBefore(" ")

            raiz.findViewById<TextView>(idPuntos).text = "${formatear(usuario.puntos)} pts"

            raiz.findViewById<TextView>(idBase).background = GradientDrawable().apply {
                setColor(Color.parseColor(coloresBase.getValue(indice + 1)))
                cornerRadii = floatArrayOf(radio, radio, radio, radio, 0f, 0f, 0f, 0f)
            }
        }

        val contenedor = raiz.findViewById<View>(R.id.contenedorPodio)
        contenedor.visibility = if (ranking.isEmpty()) View.GONE else View.VISIBLE

        return minOf(3, ranking.size)
    }
}
