package com.example.syslearning.juego

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.syslearning.R
import com.example.syslearning.juego.ReglasGamificacion.Atributos
import com.example.syslearning.juego.ReglasGamificacion.Enemigo
import com.example.syslearning.model.Ejercicio
import com.google.android.material.button.MaterialButton

/**
 * Combate por turnos que mezcla preguntas educativas y batalla:
 *
 *  1. Turno de SYS-01: aparece una pregunta. Si la respuesta es correcta,
 *     SYS-01 dispara su láser (daño según su ataque; ×2 con Pulso EMP).
 *     Si es incorrecta, falla el disparo.
 *  2. Turno del virus: ataca según su ataque y la defensa de SYS-01.
 *     Si la respuesta fue incorrecta, el golpe es crítico. SYS-01 puede
 *     esquivar según su velocidad.
 *  3. Termina cuando la vida de uno llega a cero.
 */
class CombateFragment : Fragment(R.layout.fragment_combate) {

    private lateinit var enemigo: Enemigo
    private lateinit var atributos: Atributos

    /** Vida y ataque del virus según su nivel (sube con cada victoria). */
    private lateinit var virus: ReglasGamificacion.StatsVirus

    private lateinit var tvRonda: TextView
    private lateinit var tvTurno: TextView
    private lateinit var tvMensaje: TextView
    private lateinit var barraVidaVirus: ProgressBar
    private lateinit var barraVidaRobot: ProgressBar
    private lateinit var barraEnergia: ProgressBar
    private lateinit var tvVidaVirus: TextView
    private lateinit var tvVidaRobot: TextView
    private lateinit var tvEnergia: TextView
    private lateinit var imgVirus: ImageView
    private lateinit var imgRobot: ImageView
    private lateinit var laser: View
    private lateinit var tvDanoVirus: TextView
    private lateinit var tvDanoRobot: TextView
    private lateinit var tvPregunta: TextView
    private lateinit var tvCodigo: TextView
    private lateinit var contenedorOpciones: LinearLayout
    private lateinit var btnPulso: MaterialButton
    private lateinit var pantallaResultado: View

    private var vidaRobot = 0
    private var vidaVirus = 0
    private var energia = 0
    private var ronda = 1
    private var pulsoActivo = false
    private var terminado = false

    private var preguntas: List<Ejercicio> = emptyList()

    /** SYS-01 con sus mejoras visibles (también en la pantalla de victoria). */
    private var imagenRobot: Drawable? = null
    private var indicePregunta = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enemigo = Enemigo.desde(arguments?.getString(ARG_ENEMIGO)) ?: Enemigo.BASICO

        tvRonda = view.findViewById(R.id.tvRonda)
        tvTurno = view.findViewById(R.id.tvTurno)
        tvMensaje = view.findViewById(R.id.tvMensajeCombate)
        barraVidaVirus = view.findViewById(R.id.barraVidaVirus)
        barraVidaRobot = view.findViewById(R.id.barraVidaRobot)
        barraEnergia = view.findViewById(R.id.barraEnergiaRobot)
        tvVidaVirus = view.findViewById(R.id.tvVidaVirus)
        tvVidaRobot = view.findViewById(R.id.tvVidaRobot)
        tvEnergia = view.findViewById(R.id.tvEnergiaRobot)
        imgVirus = view.findViewById(R.id.imgVirusCombate)
        imgRobot = view.findViewById(R.id.imgRobotCombate)
        laser = view.findViewById(R.id.laser)
        tvDanoVirus = view.findViewById(R.id.tvDanoVirus)
        tvDanoRobot = view.findViewById(R.id.tvDanoRobot)
        tvPregunta = view.findViewById(R.id.tvPreguntaCombate)
        tvCodigo = view.findViewById(R.id.tvCodigoCombate)
        contenedorOpciones = view.findViewById(R.id.contenedorOpcionesCombate)
        btnPulso = view.findViewById(R.id.btnPulsoEmp)
        pantallaResultado = view.findViewById(R.id.pantallaResultado)

        view.findViewById<TextView>(R.id.tvNombreVirusCombate).text = enemigo.nombre
        imgVirus.setImageResource(SysRobotFragment.imagenDe(enemigo))

        view.findViewById<TextView>(R.id.btnSalirCombate).setOnClickListener { confirmarHuida() }
        view.findViewById<MaterialButton>(R.id.btnVolverSysRobotResultado).setOnClickListener { volver() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (terminado) volver() else confirmarHuida()
                }
            }
        )

        btnPulso.setOnClickListener {
            pulsoActivo = !pulsoActivo
            actualizarBotonPulso()
        }

        // La barra flotante de la app taparía las opciones.
        view.post { activity?.findViewById<View>(R.id.cardBarraInferior)?.visibility = View.GONE }

        tvPregunta.text = "Preparando el combate…"

        // Atributos actuales de SYS-01 (con sus mejoras) y preguntas.
        Gamificacion.leer(
            requireContext(),
            onEstado = { estado ->
                if (!isAdded) return@leer
                atributos = estado.atributos()
                imagenRobot = RobotVisual.imagen(requireContext(), estado.niveles)
                imgRobot.setImageDrawable(imagenRobot)
                virus = ReglasGamificacion.statsVirus(enemigo, estado.victorias[enemigo.clave] ?: 0)
                cargarPreguntas()
            },
            onError = {
                if (!isAdded) return@leer
                atributos = ReglasGamificacion.atributos(emptyMap())
                virus = ReglasGamificacion.statsVirus(enemigo, 0)
                cargarPreguntas()
            }
        )
    }

    override fun onDestroyView() {
        activity?.findViewById<View>(R.id.cardBarraInferior)?.visibility = View.VISIBLE
        super.onDestroyView()
    }

    private fun cargarPreguntas() {
        PreguntasCombate.obtener(requireContext()) { lista ->
            if (!isAdded) return@obtener
            preguntas = lista
            iniciar()
        }
    }

    private fun iniciar() {

        vidaRobot = atributos.vida
        vidaVirus = virus.vida
        energia = atributos.energia

        barraVidaRobot.max = atributos.vida
        barraVidaVirus.max = virus.vida

        view?.findViewById<TextView>(R.id.tvNombreVirusCombate)?.text =
            "${enemigo.nombre} · Nv. ${virus.nivel}"
        barraEnergia.max = atributos.energia

        btnPulso.visibility = if (atributos.tieneHabilidadEspecial) View.VISIBLE else View.GONE

        actualizarBarras()
        turnoRobot()
    }

    // ==================================================================
    // Turnos
    // ==================================================================

    private fun turnoRobot() {

        if (!isAdded || terminado) return

        tvRonda.text = "Ronda $ronda"
        indicarTurno(esRobot = true)
        pulsoActivo = false
        actualizarBotonPulso()

        val pregunta = preguntas[indicePregunta % preguntas.size]
        indicePregunta++

        tvPregunta.text = pregunta.enunciado
        tvCodigo.text = pregunta.codigo
        tvCodigo.visibility = if (pregunta.codigo.isBlank()) View.GONE else View.VISIBLE

        contenedorOpciones.removeAllViews()

        PreguntasCombate.opciones(pregunta).forEachIndexed { indice, texto ->
            contenedorOpciones.addView(botonOpcion(texto) { responder(pregunta, indice) })
        }
    }

    private fun responder(pregunta: Ejercicio, indice: Int) {

        if (terminado) return

        bloquearOpciones(pregunta, indice)

        val correcta = indice == pregunta.respuestaCorrecta
        val usarPulso = pulsoActivo && energia >= ReglasGamificacion.COSTO_PULSO_EMP

        if (usarPulso) energia -= ReglasGamificacion.COSTO_PULSO_EMP
        btnPulso.isEnabled = false

        if (correcta) {

            val dano = ReglasGamificacion.danoRobot(atributos.ataque, usarPulso)
            vidaVirus = (vidaVirus - dano).coerceAtLeast(0)

            tvMensaje.setTextColor(Color.parseColor("#69F0AE"))
            tvMensaje.text = if (usarPulso) "✅ ¡Correcto! ⚡ Pulso EMP: $dano de daño" else "✅ ¡Correcto! SYS-01 ataca: $dano de daño"

            animarDisparo(usarPulso) {
                mostrarDano(tvDanoVirus, "-$dano")
                sacudir(imgVirus)
                actualizarBarras()
                despuesDeAtacar(respuestaIncorrecta = false)
            }

        } else {

            val opcionCorrecta = PreguntasCombate.opciones(pregunta).getOrNull(pregunta.respuestaCorrecta) ?: ""
            tvMensaje.setTextColor(Color.parseColor("#FF8A80"))
            tvMensaje.text = "❌ Incorrecto. Era: $opcionCorrecta. ¡SYS-01 falla el disparo!" +
                    if (pregunta.explicacion.isNotBlank()) "\n${pregunta.explicacion}" else ""

            mostrarDano(tvDanoVirus, "Falló")
            actualizarBarras()
            despuesDeAtacar(respuestaIncorrecta = true)
        }
    }

    private fun despuesDeAtacar(respuestaIncorrecta: Boolean) {

        if (vidaVirus <= 0) {
            view?.postDelayed({ victoria() }, 700)
            return
        }

        view?.postDelayed({ turnoVirus(respuestaIncorrecta) }, 1300)
    }

    private fun turnoVirus(respuestaIncorrecta: Boolean) {

        if (!isAdded || terminado) return

        indicarTurno(esRobot = false)

        val dano = ReglasGamificacion.danoEnemigo(
            ataqueEnemigo = virus.ataque,
            defensa = atributos.defensa,
            velocidad = atributos.velocidad,
            respuestaIncorrecta = respuestaIncorrecta
        )

        // El virus se lanza hacia SYS-01.
        imgVirus.animate()
            .translationX(-dp(40).toFloat())
            .translationY(dp(30).toFloat())
            .setDuration(220)
            .withEndAction {
                imgVirus.animate().translationX(0f).translationY(0f).setDuration(220).start()

                if (!isAdded) return@withEndAction

                if (dano == 0) {
                    mostrarDano(tvDanoRobot, "¡Esquivó!")
                    tvMensaje.setTextColor(Color.parseColor("#22E5FF"))
                    tvMensaje.text = "💨 SYS-01 esquivó el ataque gracias a su velocidad"
                } else {
                    vidaRobot = (vidaRobot - dano).coerceAtLeast(0)
                    mostrarDano(tvDanoRobot, "-$dano")
                    sacudir(imgRobot)
                    tvMensaje.setTextColor(Color.parseColor("#FFB74D"))
                    tvMensaje.text = if (respuestaIncorrecta) {
                        "💥 ¡Golpe crítico de ${enemigo.nombre}! -$dano"
                    } else {
                        "${enemigo.nombre} ataca: -$dano"
                    }
                }

                // SYS-01 recupera algo de energía en cada ronda.
                energia = (energia + ReglasGamificacion.ENERGIA_POR_TURNO).coerceAtMost(atributos.energia)
                actualizarBarras()

                if (vidaRobot <= 0) {
                    view?.postDelayed({ derrota() }, 700)
                } else {
                    ronda++
                    view?.postDelayed({ turnoRobot() }, 1100)
                }
            }
            .start()
    }

    // ==================================================================
    // Final del combate
    // ==================================================================

    private fun victoria() {

        if (!isAdded || terminado) return
        terminado = true

        mostrarResultado(
            imagen = R.drawable.robot_sys01,
            imagenCompuesta = imagenRobot?.constantState?.newDrawable()?.mutate(),
            titulo = "¡VICTORIA!",
            colorTitulo = "#22E5FF",
            detalle = "Registrando tu recompensa…"
        )

        Gamificacion.reclamarVictoria(
            enemigo,
            onResultado = { resultado ->
                if (!isAdded) return@reclamarVictoria
                val r = resultado.recompensa
                val detalle = buildString {
                    append("SYS-01 eliminó al ${enemigo.nombre} en ${ronda} ronda(s).\n\n")
                    append(r.mensaje)
                    append("\n\n")
                    if (r.monedas > 0) append("🪙 +${r.monedas} monedas\n")
                    r.materiales.forEach { (material, cantidad) ->
                        append("${material.icono} +$cantidad ${material.nombre.lowercase()}\n")
                    }
                    append("\nSaldo: 🪙 ${resultado.estado.monedas}")
                }
                view?.findViewById<TextView>(R.id.tvDetalleResultado)?.text = detalle
            },
            onError = {
                if (!isAdded) return@reclamarVictoria
                view?.findViewById<TextView>(R.id.tvDetalleResultado)?.text =
                    "Ganaste, pero no se pudo registrar la recompensa porque se perdió la conexión. " +
                            "Vuelve a combatir con internet para cobrarla."
            }
        )
    }

    private fun derrota() {

        if (!isAdded || terminado) return
        terminado = true

        mostrarResultado(
            imagen = SysRobotFragment.imagenDe(enemigo),
            titulo = "DERROTA",
            colorTitulo = "#FF5252",
            detalle = "El ${enemigo.nombre} dañó los circuitos de SYS-01.\n\n" +
                    "Consejos:\n• Repasa las actividades: cada respuesta correcta es un disparo.\n" +
                    "• Mejora la armadura para resistir más.\n" +
                    "• Mejora la mandíbula láser para hacer más daño.\n\n" +
                    "Perder no te quita monedas."
        )
    }

    private fun mostrarResultado(
        imagen: Int,
        titulo: String,
        colorTitulo: String,
        detalle: String,
        imagenCompuesta: Drawable? = null
    ) {
        val raiz = view ?: return
        val imgResultado = raiz.findViewById<ImageView>(R.id.imgResultado)
        if (imagenCompuesta != null) imgResultado.setImageDrawable(imagenCompuesta) else imgResultado.setImageResource(imagen)
        raiz.findViewById<TextView>(R.id.tvTituloResultado).apply {
            text = titulo
            setTextColor(Color.parseColor(colorTitulo))
        }
        raiz.findViewById<TextView>(R.id.tvDetalleResultado).text = detalle
        pantallaResultado.alpha = 0f
        pantallaResultado.visibility = View.VISIBLE
        pantallaResultado.animate().alpha(1f).setDuration(300).start()
    }

    private fun confirmarHuida() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Retirarse del combate?")
            .setMessage("No pierdes monedas, pero tampoco recibes recompensa.")
            .setNegativeButton("Seguir luchando", null)
            .setPositiveButton("Retirarse") { _, _ -> volver() }
            .show()
    }

    private fun volver() {
        terminado = true
        if (isAdded) parentFragmentManager.popBackStack()
    }

    // ==================================================================
    // Efectos visuales
    // ==================================================================

    private fun actualizarBarras() {
        barraVidaRobot.progress = vidaRobot
        barraVidaVirus.progress = vidaVirus
        barraEnergia.progress = energia
        tvVidaRobot.text = "❤️ $vidaRobot / ${atributos.vida}"
        tvVidaVirus.text = "❤️ $vidaVirus / ${virus.vida}  ⚔️ ${virus.ataque}"
        tvEnergia.text = "🔋 $energia / ${atributos.energia}"
    }

    private fun indicarTurno(esRobot: Boolean) {
        tvTurno.text = if (esRobot) "🐶 Tu turno" else "🦠 Turno del virus"
        tvTurno.setTextColor(Color.parseColor(if (esRobot) "#22E5FF" else "#FF8A80"))
    }

    private fun actualizarBotonPulso() {
        val disponible = energia >= ReglasGamificacion.COSTO_PULSO_EMP
        btnPulso.isEnabled = disponible
        btnPulso.alpha = if (disponible) 1f else 0.5f
        btnPulso.text = when {
            !disponible -> "⚡ Pulso EMP: energía insuficiente"
            pulsoActivo -> "⚡ Pulso EMP ACTIVADO: responde bien para daño ×2"
            else -> "⚡ Activar Pulso EMP (30 de energía): daño ×2"
        }
    }

    /** Disparo láser desde SYS-01 hacia el virus. */
    private fun animarDisparo(pulso: Boolean, alTerminar: () -> Unit) {

        laser.setBackgroundColor(Color.parseColor(if (pulso) "#FFD54F" else "#22E5FF"))
        laser.scaleY = if (pulso) 2f else 1f
        laser.translationX = 0f
        laser.translationY = 0f
        laser.visibility = View.VISIBLE

        imgRobot.animate().translationX(dp(10).toFloat()).setDuration(120).withEndAction {
            imgRobot.animate().translationX(0f).setDuration(120).start()
        }.start()

        // El virus está dentro de su columna; se suma la posición de esa
        // columna para llevar el láser al centro del virus.
        val columna = imgVirus.parent as? View
        val centroVirusX = (columna?.x ?: 0f) + imgVirus.x + imgVirus.width / 2f
        val centroVirusY = (columna?.y ?: 0f) + imgVirus.y + imgVirus.height / 2f

        val destinoX = centroVirusX - (laser.x + laser.width / 2f)
        val destinoY = centroVirusY - (laser.y + laser.height / 2f)

        laser.animate()
            .translationX(destinoX)
            .translationY(destinoY)
            .setDuration(350)
            .withEndAction {
                laser.visibility = View.INVISIBLE
                if (isAdded) alTerminar()
            }
            .start()
    }

    private fun sacudir(vista: View) {
        vista.animate().translationX(dp(8).toFloat()).setDuration(50).withEndAction {
            vista.animate().translationX(-dp(8).toFloat()).setDuration(50).withEndAction {
                vista.animate().translationX(0f).setDuration(50).start()
            }.start()
        }.start()
        vista.animate().alpha(0.4f).setDuration(80).withEndAction {
            vista.animate().alpha(1f).setDuration(120).start()
        }.start()
    }

    private fun mostrarDano(tv: TextView, texto: String) {
        tv.text = texto
        tv.alpha = 1f
        tv.translationY = 0f
        tv.visibility = View.VISIBLE
        tv.animate().translationY(-dp(36).toFloat()).alpha(0f).setDuration(900).withEndAction {
            tv.visibility = View.INVISIBLE
        }.start()
    }

    private fun botonOpcion(texto: String, alTocar: () -> Unit): TextView =
        TextView(requireContext()).apply {
            text = texto
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            minHeight = dp(48)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = fondoOpcion("#1A22E5FF", "#5522E5FF")
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
            setOnClickListener { alTocar() }
        }

    private fun bloquearOpciones(pregunta: Ejercicio, elegida: Int) {
        for (i in 0 until contenedorOpciones.childCount) {
            val opcion = contenedorOpciones.getChildAt(i)
            opcion.isClickable = false
            opcion.setOnClickListener(null)
            opcion.background = when (i) {
                pregunta.respuestaCorrecta -> fondoOpcion("#3343E97B", "#43E97B")
                elegida -> fondoOpcion("#33FF5252", "#FF5252")
                else -> fondoOpcion("#0DFFFFFF", "#22FFFFFF")
            }
        }
    }

    private fun fondoOpcion(fondo: String, borde: String) = GradientDrawable().apply {
        cornerRadius = dp(14).toFloat()
        setColor(Color.parseColor(fondo))
        setStroke(dp(1), Color.parseColor(borde))
    }

    private fun dp(valor: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor.toFloat(), resources.displayMetrics).toInt()

    companion object {

        private const val ARG_ENEMIGO = "enemigo"

        fun newInstance(enemigo: Enemigo) = CombateFragment().apply {
            arguments = Bundle().apply { putString(ARG_ENEMIGO, enemigo.clave) }
        }
    }
}
