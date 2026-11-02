package com.example.syslearning.juego

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.syslearning.R
import com.example.syslearning.juego.Gamificacion.EstadoJuego
import com.example.syslearning.juego.ReglasGamificacion.Componente
import com.example.syslearning.juego.ReglasGamificacion.Costo
import com.example.syslearning.juego.ReglasGamificacion.Enemigo
import com.example.syslearning.juego.ReglasGamificacion.Material
import com.example.syslearning.util.LecturaRapida
import com.google.android.material.button.MaterialButton

/**
 * Sección SYS ROBOT: saldo de monedas, materiales, atributos de SYS-01,
 * mejoras de sus 4 componentes y los virus que se pueden combatir.
 */
class SysRobotFragment : Fragment(R.layout.fragment_sys_robot) {

    private lateinit var tvMonedas: TextView
    private lateinit var tvAviso: TextView
    private lateinit var tvNivelRobot: TextView
    private lateinit var tvRacha: TextView
    private lateinit var contenedorMateriales: LinearLayout
    private lateinit var contenedorAtributos: LinearLayout
    private lateinit var contenedorMejoras: LinearLayout
    private lateinit var contenedorEnemigos: LinearLayout

    private var estado = EstadoJuego()
    private var flotacion: ObjectAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvMonedas = view.findViewById(R.id.tvMonedasSysRobot)
        tvAviso = view.findViewById(R.id.tvAvisoSysRobot)
        tvNivelRobot = view.findViewById(R.id.tvNivelRobot)
        tvRacha = view.findViewById(R.id.tvRachaSysRobot)
        contenedorMateriales = view.findViewById(R.id.contenedorMateriales)
        contenedorAtributos = view.findViewById(R.id.contenedorAtributos)
        contenedorMejoras = view.findViewById(R.id.contenedorMejoras)
        contenedorEnemigos = view.findViewById(R.id.contenedorEnemigos)

        view.findViewById<TextView>(R.id.btnVolverSysRobot).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // SYS-01 flota suavemente en su pantalla.
        val robot = view.findViewById<ImageView>(R.id.imgRobot)
        flotacion = ObjectAnimator.ofFloat(robot, View.TRANSLATION_Y, 0f, -dp(8).toFloat()).apply {
            duration = 1400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        requireActivity().findViewById<View>(R.id.cardBarraInferior)?.visibility = View.GONE

        dibujar()
        cargar()
    }

    override fun onDestroyView() {
        flotacion?.cancel()
        activity?.findViewById<View>(R.id.cardBarraInferior)?.visibility = View.VISIBLE
        super.onDestroyView()
    }

    private fun cargar() {

        Gamificacion.leer(
            requireContext(),
            onEstado = { nuevo ->
                if (!isAdded) return@leer
                estado = nuevo
                dibujar()
            },
            onError = {
                if (!isAdded) return@leer
                mostrarAviso("No se pudo cargar tu progreso. Conéctate a internet una vez para descargarlo.")
            }
        )

        if (!LecturaRapida.hayConexion(requireContext())) {
            mostrarAviso("Sin conexión: puedes ver a SYS-01, pero mejorar y combatir necesitan internet.")
        }
    }

    private fun mostrarAviso(texto: String) {
        tvAviso.text = texto
        tvAviso.visibility = View.VISIBLE
    }

    // ==================================================================
    // Dibujo
    // ==================================================================

    private fun dibujar() {

        tvMonedas.text = "🪙 ${estado.monedas}"

        // Materiales
        contenedorMateriales.removeAllViews()
        Material.values().forEach { material ->
            contenedorMateriales.addView(chip("${material.icono} ${estado.materiales[material] ?: 0}", material.nombre))
        }

        // Atributos
        val a = estado.atributos()
        contenedorAtributos.removeAllViews()
        listOf(
            Triple("❤️", "Vida", a.vida),
            Triple("⚔️", "Ataque", a.ataque),
            Triple("🛡️", "Defensa", a.defensa),
            Triple("🔋", "Energía", a.energia),
            Triple("💨", "Velocidad", a.velocidad)
        ).forEach { (icono, nombre, valor) ->
            contenedorAtributos.addView(atributo(icono, nombre, valor))
        }

        // Apariencia según las mejoras (capas de RobotVisual).
        view?.findViewById<ImageView>(R.id.imgRobot)
            ?.setImageDrawable(RobotVisual.imagen(requireContext(), estado.niveles))
        view?.findViewById<TextView>(R.id.tvNombreRobot)?.text = RobotVisual.nombre(estado.niveles)

        val forma = RobotVisual.forma(estado.niveles)
        val mejoras = estado.niveles.values.sum()
        tvNivelRobot.text = "Forma ${forma.nombre} · Mejoras $mejoras de ${Componente.values().size * Componente.NIVEL_MAXIMO}\n${forma.descripcion}" +
                if (a.tieneHabilidadEspecial) " · ⚡ Pulso EMP desbloqueado" else ""

        // Racha
        val racha = ReglasGamificacion.rachaVigente(estado.racha, Gamificacion.fechaHoy(), Gamificacion.fechaAyer())
        val proximoHito = ReglasGamificacion.HITOS_RACHA.keys
            .firstOrNull { it > racha && it !in estado.racha.hitosCobrados }
        tvRacha.text = buildString {
            append("🔥 Racha de aprendizaje: $racha día(s)")
            if (proximoHito != null) {
                append(" · premio de ${ReglasGamificacion.HITOS_RACHA[proximoHito]} 🪙 al llegar a $proximoHito")
            }
        }

        dibujarMejoras()
        dibujarEnemigos()
    }

    private fun dibujarMejoras() {

        contenedorMejoras.removeAllViews()

        Componente.values().forEach { componente ->

            val fila = layoutInflater.inflate(R.layout.item_mejora_robot, contenedorMejoras, false)
            val nivel = estado.niveles[componente] ?: 0

            fila.findViewById<TextView>(R.id.tvIconoMejora).text = componente.icono
            fila.findViewById<TextView>(R.id.tvNombreMejora).text = componente.nombre
            fila.findViewById<TextView>(R.id.tvNivelMejora).text =
                "●".repeat(nivel) + "○".repeat(Componente.NIVEL_MAXIMO - nivel)
            fila.findViewById<TextView>(R.id.tvDescripcionMejora).text = componente.descripcion

            val tvCosto = fila.findViewById<TextView>(R.id.tvCostoMejora)
            val boton = fila.findViewById<MaterialButton>(R.id.btnMejorar)

            if (nivel >= Componente.NIVEL_MAXIMO) {
                tvCosto.text = "✓ Nivel máximo"
                boton.text = "Máximo"
                boton.isEnabled = false
                boton.alpha = 0.5f
            } else {
                val costo = componente.costos[nivel]
                tvCosto.text = "Nivel ${nivel + 1}: ${textoCosto(costo)}" +
                        if (costo.materiales.isNotEmpty()) "  ★ especial" else ""
                boton.text = "Mejorar"
                boton.setOnClickListener { confirmarCompra(componente, nivel + 1, costo) }
            }

            contenedorMejoras.addView(fila)
        }
    }

    private fun textoCosto(costo: Costo): String = buildString {
        append("🪙 ${costo.monedas}")
        costo.materiales.forEach { (material, cantidad) -> append("  ${material.icono}×$cantidad") }
    }

    private fun dibujarEnemigos() {

        contenedorEnemigos.removeAllViews()

        Enemigo.values().forEach { enemigo ->

            val fila = layoutInflater.inflate(R.layout.item_enemigo, contenedorEnemigos, false)
            val desbloqueado = ReglasGamificacion.desbloqueado(enemigo, estado.derrotados)
            val victorias = estado.victorias[enemigo.clave] ?: 0

            fila.findViewById<ImageView>(R.id.imgEnemigo).setImageResource(imagenDe(enemigo))
            fila.findViewById<TextView>(R.id.tvNombreEnemigo).text =
                enemigo.nombre + if (victorias > 0) "  ✓" else ""

            fila.findViewById<TextView>(R.id.tvDificultadEnemigo).apply {
                text = enemigo.dificultad
                background = GradientDrawable().apply {
                    cornerRadius = 40f
                    setColor(Color.parseColor(colorDificultad(enemigo)))
                }
            }

            // Cada victoria fortalece al virus para la próxima revancha.
            val stats = ReglasGamificacion.statsVirus(enemigo, victorias)
            fila.findViewById<TextView>(R.id.tvStatsEnemigo).text =
                "Nv. ${stats.nivel}   ❤️ ${stats.vida}   ⚔️ ${stats.ataque}" +
                        if (victorias > 0) "   🏅 $victorias" else ""

            val tvRecompensa = fila.findViewById<TextView>(R.id.tvRecompensaEnemigo)
            val boton = fila.findViewById<MaterialButton>(R.id.btnCombatir)

            when {
                !desbloqueado -> {
                    val previo = Enemigo.desde(enemigo.requiere)?.nombre ?: ""
                    tvRecompensa.text = "🔒 Vence al $previo para desbloquearlo"
                    fila.alpha = 0.55f
                    boton.isEnabled = false
                    boton.text = "Bloqueado"
                }
                victorias == 0 -> {
                    tvRecompensa.text = "Primera victoria: 🪙 ${enemigo.monedas}" +
                            enemigo.materialesPrimeraVictoria.entries.joinToString("") { (m, c) -> "  ${m.icono}×$c" }
                    boton.setOnClickListener { abrirCombate(enemigo) }
                }
                else -> {
                    val restantes = ReglasGamificacion.REPETICIONES_PREMIADAS_POR_DIA - estado.repeticionesHoy()
                    tvRecompensa.text = if (restantes > 0) {
                        "Revancha: 🪙 ${enemigo.monedas / 10} + 1 material ($restantes premiadas hoy)"
                    } else {
                        "Revancha sin premio hasta mañana"
                    }
                    boton.text = "Revancha"
                    boton.setOnClickListener { abrirCombate(enemigo) }
                }
            }

            contenedorEnemigos.addView(fila)
        }
    }

    // ==================================================================
    // Acciones
    // ==================================================================

    private fun confirmarCompra(componente: Componente, nivel: Int, costo: Costo) {

        val previa = ReglasGamificacion.evaluarCompra(
            componente, nivel - 1, nivel, estado.monedas, estado.materiales
        )

        if (previa is ReglasGamificacion.Compra.Rechazada) {
            Toast.makeText(requireContext(), previa.motivo, Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("${componente.icono} ${componente.nombre} nivel $nivel")
            .setMessage("${componente.descripcion}\n\nCosto: ${textoCosto(costo)}\nTu saldo: 🪙 ${estado.monedas}")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Mejorar") { _, _ ->
                Gamificacion.comprarMejora(requireContext(), componente) { resultado ->
                    if (!isAdded) return@comprarMejora
                    when (resultado) {
                        is Gamificacion.ResultadoCompra.Exito -> {
                            val formaAntes = RobotVisual.forma(estado.niveles)
                            estado = resultado.estado
                            dibujar()
                            val evoluciono = resultado.nivel == Componente.NIVEL_MAXIMO ||
                                    RobotVisual.forma(estado.niveles) != formaAntes
                            animarMejora(evoluciono)
                            mostrarCambioVisual(componente, resultado.nivel, formaAntes)
                        }
                        is Gamificacion.ResultadoCompra.Error ->
                            Toast.makeText(requireContext(), resultado.mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    /** Destello al mejorar; al evolucionar, además un giro completo. */
    private fun animarMejora(evoluciono: Boolean) {
        val robot = view?.findViewById<ImageView>(R.id.imgRobot) ?: return
        robot.alpha = 0.3f
        robot.animate()
            .alpha(1f)
            .scaleX(1.15f).scaleY(1.15f)
            .rotationY(if (evoluciono) 360f else 0f)
            .setDuration(if (evoluciono) 700 else 250)
            .withEndAction {
                robot.rotationY = 0f
                robot.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
            .start()
    }

    private fun mostrarCambioVisual(componente: Componente, nivel: Int, formaAntes: RobotVisual.Forma) {

        val formaAhora = RobotVisual.forma(estado.niveles)

        // El cuerpo de SYS-01 evolucionó a una nueva forma.
        if (formaAhora != formaAntes) {
            val mensaje = when (formaAhora) {
                RobotVisual.Forma.EXPLORADOR ->
                    "SYS-01 creció: cuerpo más largo, orejas en punta y patas más fuertes."
                RobotVisual.Forma.GUARDIAN ->
                    "SYS-01 ahora es un Guardián: placas de combate en el lomo y cola con filo."
                RobotVisual.Forma.OMEGA ->
                    "Completaste las 4 mejoras al máximo. SYS-01 alcanzó su forma final: " +
                            "metal violeta, alas de energía, melena dorada y un aura legendaria."
                RobotVisual.Forma.CACHORRO -> ""
            }
            AlertDialog.Builder(requireContext())
                .setTitle("🌟 ¡SYS-01 evolucionó a ${formaAhora.nombre}!")
                .setMessage("$mensaje\n\n${componente.nombre} nivel $nivel: ${RobotVisual.cambioVisual(componente, nivel)}")
                .setPositiveButton("¡Genial!", null)
                .show()
            return
        }

        if (nivel == Componente.NIVEL_MAXIMO) {
            AlertDialog.Builder(requireContext())
                .setTitle("✨ ¡${componente.nombre} evolucionó!")
                .setMessage(RobotVisual.cambioVisual(componente, nivel))
                .setPositiveButton("Ver a SYS-01", null)
                .show()
            return
        }

        Toast.makeText(
            requireContext(),
            "${componente.nombre} nivel $nivel: ${RobotVisual.cambioVisual(componente, nivel)}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun abrirCombate(enemigo: Enemigo) {

        // La recompensa se registra con una transacción: sin internet no se
        // podría cobrar la victoria, así que se avisa antes de empezar.
        if (!LecturaRapida.hayConexion(requireContext())) {
            Toast.makeText(
                requireContext(),
                "Necesitas conexión a internet para combatir y cobrar la recompensa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, CombateFragment.newInstance(enemigo))
            .addToBackStack(null)
            .commit()
    }

    // ==================================================================
    // Componentes de interfaz
    // ==================================================================

    private fun dp(valor: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor.toFloat(), resources.displayMetrics).toInt()

    private fun chip(texto: String, descripcion: String): TextView = TextView(requireContext()).apply {
        text = texto
        contentDescription = descripcion
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setBackgroundResource(R.drawable.bg_juego_chip)
        setPadding(dp(6), dp(6), dp(6), dp(6))
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(6)
        }
    }

    private fun atributo(icono: String, nombre: String, valor: Int): View =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            addView(TextView(context).apply {
                text = icono
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            })
            addView(TextView(context).apply {
                text = valor.toString()
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = nombre
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#B8C4D9"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            })
        }

    companion object {

        fun imagenDe(enemigo: Enemigo): Int = when (enemigo) {
            Enemigo.BASICO -> R.drawable.virus_basico
            Enemigo.CORRUPTOR -> R.drawable.virus_corruptor
            Enemigo.DESTRUCTOR -> R.drawable.virus_destructor
        }

        fun colorDificultad(enemigo: Enemigo): String = when (enemigo) {
            Enemigo.BASICO -> "#2E7D32"
            Enemigo.CORRUPTOR -> "#6A1B9A"
            Enemigo.DESTRUCTOR -> "#C62828"
        }
    }
}
