package com.example.syslearning.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.syslearning.PreguntasRepository
import com.example.syslearning.R
import com.example.syslearning.actividades.SesionActividadesManager
import com.example.syslearning.model.Pregunta
import com.example.syslearning.ui.AnilloProgresoView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Sesión de 10 preguntas estilo Duolingo: temporizador de 20s por
 * pregunta, 2 opciones, XP con bonus por rapidez, racha diaria e
 * insignias (a través de SesionActividadesManager).
 */
class SesionActividadFragment : Fragment(R.layout.fragment_sesion_actividad) {

    private lateinit var db: FirebaseFirestore

    private lateinit var progresoSesion: ProgressBar
    private lateinit var tvContadorPregunta: TextView
    private lateinit var tvXpSesion: TextView
    private lateinit var anilloTiempo: AnilloProgresoView
    private lateinit var tvTiempo: TextView
    private lateinit var tvEnunciado: TextView
    private lateinit var btnOpcionA: MaterialButton
    private lateinit var btnOpcionB: MaterialButton
    private lateinit var btnSalirSesion: TextView

    private var actividadId: String = ""
    private var actividadTitulo: String = ""

    private var preguntas: List<Pregunta> = emptyList()
    private var indiceActual = 0

    private var aciertos = 0
    private var xpTotal = 0

    private var timer: CountDownTimer? = null
    private var tiempoInicioPreguntaMs = 0L

    private val duracionPreguntaMs = 20_000L

    private val colorNeutral = "#1E293B"
    private val colorCorrecto = "#43A047"
    private val colorIncorrecto = "#E53935"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        actividadId = arguments?.getString("actividadId") ?: ""
        actividadTitulo = arguments?.getString("actividadTitulo") ?: actividadId

        progresoSesion = view.findViewById(R.id.progresoSesion)
        tvContadorPregunta = view.findViewById(R.id.tvContadorPregunta)
        tvXpSesion = view.findViewById(R.id.tvXpSesion)
        anilloTiempo = view.findViewById(R.id.anilloTiempo)
        tvTiempo = view.findViewById(R.id.tvTiempo)
        tvEnunciado = view.findViewById(R.id.tvEnunciado)
        btnOpcionA = view.findViewById(R.id.btnOpcionA)
        btnOpcionB = view.findViewById(R.id.btnOpcionB)
        btnSalirSesion = view.findViewById(R.id.btnSalirSesion)

        btnSalirSesion.setOnClickListener {
            confirmarSalida()
        }

        cargarPreguntas()
    }

    private fun cargarPreguntas() {

        tvEnunciado.text = "Cargando preguntas..."
        btnOpcionA.isEnabled = false
        btnOpcionB.isEnabled = false

        PreguntasRepository.obtenerSesion(
            db = db,
            actividadId = actividadId,
            onSuccess = { lista ->

                if (!isAdded) return@obtenerSesion

                if (lista.isEmpty()) {

                    tvEnunciado.text =
                        "Todavía no hay preguntas creadas para esta actividad."

                    return@obtenerSesion
                }

                preguntas = lista
                indiceActual = 0
                aciertos = 0
                xpTotal = 0

                progresoSesion.max = preguntas.size

                mostrarPregunta(0)

            },
            onError = {

                if (!isAdded) return@obtenerSesion

                tvEnunciado.text = "No se pudieron cargar las preguntas."
            }
        )
    }

    private fun mostrarPregunta(indice: Int) {

        val pregunta = preguntas[indice]

        tvEnunciado.text = pregunta.enunciado
        btnOpcionA.text = pregunta.opcionA
        btnOpcionB.text = pregunta.opcionB

        progresoSesion.progress = indice + 1
        tvContadorPregunta.text = "Pregunta ${indice + 1} de ${preguntas.size}"

        resetearBotones()

        btnOpcionA.setOnClickListener { seleccionarRespuesta("A") }
        btnOpcionB.setOnClickListener { seleccionarRespuesta("B") }

        iniciarTimer()
    }

    private fun resetearBotones() {

        btnOpcionA.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorNeutral))
        btnOpcionB.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorNeutral))

        btnOpcionA.isEnabled = true
        btnOpcionB.isEnabled = true

        btnOpcionA.scaleX = 1f
        btnOpcionA.scaleY = 1f
        btnOpcionB.scaleX = 1f
        btnOpcionB.scaleY = 1f
    }

    // ==================================================================
    // Temporizador
    // ==================================================================

    private fun iniciarTimer() {

        timer?.cancel()

        tiempoInicioPreguntaMs = System.currentTimeMillis()

        timer = object : CountDownTimer(duracionPreguntaMs, 100L) {

            override fun onTick(millisUntilFinished: Long) {

                if (!isAdded) return

                tvTiempo.text = ((millisUntilFinished / 1000) + 1).toString()
                anilloTiempo.setProgreso(millisUntilFinished / duracionPreguntaMs.toFloat())
            }

            override fun onFinish() {

                if (!isAdded) return

                responderAutomaticoIncorrecto()
            }

        }.start()
    }

    // ==================================================================
    // Responder
    // ==================================================================

    private fun seleccionarRespuesta(opcion: String) {

        timer?.cancel()

        val pregunta = preguntas[indiceActual]
        val esCorrecta = opcion == pregunta.respuestaCorrecta
        val elapsedMs = System.currentTimeMillis() - tiempoInicioPreguntaMs

        val botonElegido = if (opcion == "A") btnOpcionA else btnOpcionB
        val botonCorrecto = if (pregunta.respuestaCorrecta == "A") btnOpcionA else btnOpcionB

        deshabilitarBotones()

        animarSeleccion(botonElegido, esCorrecta)

        if (esCorrecta) {

            aciertos++

            val xpGanado = calcularXp(elapsedMs)
            xpTotal += xpGanado

            reproducirSonido(correcta = true)

        } else {

            // Resalta cuál era la correcta, además de la elegida en rojo.
            pintarBoton(botonCorrecto, colorCorrecto)

            vibrarLigero()
            reproducirSonido(correcta = false)
        }

        tvXpSesion.text = "⭐ $xpTotal XP"

        view?.postDelayed({ siguientePregunta() }, 1300)
    }

    private fun responderAutomaticoIncorrecto() {

        val pregunta = preguntas[indiceActual]
        val botonCorrecto = if (pregunta.respuestaCorrecta == "A") btnOpcionA else btnOpcionB

        deshabilitarBotones()
        pintarBoton(botonCorrecto, colorCorrecto)

        vibrarLigero()
        reproducirSonido(correcta = false)

        view?.postDelayed({ siguientePregunta() }, 1300)
    }

    private fun siguientePregunta() {

        if (!isAdded) return

        indiceActual++

        if (indiceActual < preguntas.size) {
            mostrarPregunta(indiceActual)
        } else {
            finalizarSesion()
        }
    }

    // ==================================================================
    // Puntuación
    // ==================================================================

    /**
     * Bonus por rapidez sobre un temporizador de 20s:
     * antes de 5s -> 15 XP, antes de 10s -> 12 XP, resto -> 10 XP.
     */
    private fun calcularXp(elapsedMs: Long): Int {
        return when {
            elapsedMs <= 5_000 -> 15
            elapsedMs <= 10_000 -> 12
            else -> 10
        }
    }

    // ==================================================================
    // Animaciones / colores
    // ==================================================================

    private fun pintarBoton(boton: MaterialButton, colorHex: String) {
        boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorHex))
    }

    private fun animarSeleccion(boton: MaterialButton, esCorrecta: Boolean) {

        pintarBoton(boton, if (esCorrecta) colorCorrecto else colorIncorrecto)

        boton.animate()
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(120)
            .withEndAction {
                boton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }

    private fun deshabilitarBotones() {
        btnOpcionA.isEnabled = false
        btnOpcionB.isEnabled = false
    }

    // ==================================================================
    // Sonido y vibración
    // ==================================================================

    private fun reproducirSonido(correcta: Boolean) {

        val nombre = if (correcta) "sonido_correcto" else "sonido_incorrecto"
        val id = resources.getIdentifier(nombre, "raw", requireContext().packageName)

        // Si todavía no agregaste el archivo de audio, se omite sin fallar.
        if (id == 0) return

        try {
            val mediaPlayer = MediaPlayer.create(requireContext(), id)
            mediaPlayer?.setOnCompletionListener { it.release() }
            mediaPlayer?.start()
        } catch (e: Exception) {
            // No interrumpe el juego si el audio falla.
        }
    }

    private fun vibrarLigero() {

        val contexto = context ?: return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                contexto.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            contexto.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(120)
        }
    }

    // ==================================================================
    // Fin de la sesión
    // ==================================================================

    private fun finalizarSesion() {

        timer?.cancel()

        val racha = SesionActividadesManager.registrarSesion(
            context = requireContext(),
            actividadId = actividadId,
            actividadTitulo = actividadTitulo,
            aciertos = aciertos,
            totalPreguntas = preguntas.size,
            xpGanado = xpTotal
        )

        mostrarDialogoResultados(racha)
    }

    private fun mostrarDialogoResultados(racha: Int) {

        if (!isAdded) return

        val aprobo = preguntas.isNotEmpty() &&
                (aciertos.toDouble() / preguntas.size) >= 0.6

        val mensaje = buildString {
            append(if (aprobo) "🎉 ¡Actividad completada!\n\n" else "Sesión terminada\n\n")
            append("Aciertos: $aciertos de ${preguntas.size}\n")
            append("XP ganado: $xpTotal\n")
            if (racha > 0) append("🔥 Racha diaria: $racha día(s)")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(actividadTitulo)
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()
                parentFragmentManager.popBackStack()
            }
            .show()
    }

    private fun confirmarSalida() {

        AlertDialog.Builder(requireContext())
            .setTitle("¿Salir de la sesión?")
            .setMessage("Perderás el progreso de esta sesión.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ ->
                timer?.cancel()
                parentFragmentManager.popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }

    companion object {

        fun newInstance(actividadId: String, actividadTitulo: String): SesionActividadFragment {
            return SesionActividadFragment().apply {
                arguments = Bundle().apply {
                    putString("actividadId", actividadId)
                    putString("actividadTitulo", actividadTitulo)
                }
            }
        }
    }
}