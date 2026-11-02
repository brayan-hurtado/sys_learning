package com.example.syslearning.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.syslearning.R
import com.example.syslearning.game.EstadoJuego
import com.example.syslearning.game.Diccionario
import com.example.syslearning.game.PalabraDelDiaManager

class PalabraDelDiaFragment : Fragment(R.layout.fragment_palabra_dia) {

    private val colorGris = "#787C7E"
    private val colorAmarillo = "#C9B458"
    private val colorVerde = "#6AAA64"
    private val colorBordeVacio = "#3A3A3C"
    private val colorBordeLleno = "#565758"
    private val colorBordeActivo = "#8A8A8C"
    private val colorTeclaDefault = "#818384"

    private lateinit var contenedorTablero: LinearLayout
    private lateinit var contenedorTeclado: LinearLayout

    private lateinit var estado: EstadoJuego
    private lateinit var palabraObjetivo: String
    private var longitud = 5

    private lateinit var celdas: Array<Array<TextView>>
    private var filaActual = 0
    private var intentoActual = StringBuilder()

    private val teclas = mutableMapOf<Char, TextView>()
    private val teclasEspeciales = mutableListOf<TextView>()
    private val estadoTeclas = mutableMapOf<Char, Int>()

    private val filasTeclado = listOf(
        "QWERTYUIOP",
        "ASDFGHJKLÑ",
        "ZXCVBNM"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contenedorTablero = view.findViewById(R.id.contenedorTablero)
        contenedorTeclado = view.findViewById(R.id.contenedorTeclado)

        view.findViewById<TextView>(R.id.btnVolver).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        estado = PalabraDelDiaManager.cargarEstado(requireContext())
        palabraObjetivo = estado.palabra
        longitud = palabraObjetivo.length

        construirTablero()
        construirTeclado()
        restaurarPartida()
    }

    private fun construirTablero() {
        contenedorTablero.removeAllViews()

        val tamCelda = when (longitud) {
            5 -> dpToPx(58)
            6 -> dpToPx(50)
            else -> dpToPx(44)
        }

        val margen = dpToPx(4)

        celdas = Array(6) {
            val filaLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            val celdasFila = Array(longitud) {
                TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(tamCelda, tamCelda).apply {
                        setMargins(margen, margen, margen, margen)
                    }

                    gravity = Gravity.CENTER
                    textSize = 22f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    background = crearFondo(null, 8f, colorBordeVacio, 2)
                    cameraDistance = 8000f

                    filaLayout.addView(this)
                }
            }

            contenedorTablero.addView(filaLayout)
            celdasFila
        }
    }

    private fun construirTeclado() {
        contenedorTeclado.removeAllViews()
        teclas.clear()
        teclasEspeciales.clear()

        filasTeclado.forEachIndexed { indiceFila, fila ->

            val filaLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(6)
                }
            }

            if (indiceFila == 2) {
                val enter = crearTeclaEspecial("✓", dpToPx(46)) {
                    enviarIntento()
                }

                teclasEspeciales.add(enter)
                filaLayout.addView(enter)
            }

            fila.forEach { letra ->
                val tecla = crearTeclaLetra(letra)
                teclas[letra] = tecla
                filaLayout.addView(tecla)
            }

            if (indiceFila == 2) {
                val borrar = crearTeclaEspecial("⌫", dpToPx(46)) {
                    borrarLetra()
                }

                teclasEspeciales.add(borrar)
                filaLayout.addView(borrar)
            }

            contenedorTeclado.addView(filaLayout)
        }
    }

    private fun crearTeclaLetra(letra: Char): TextView {
        return TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(46)).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }

            gravity = Gravity.CENTER
            text = letra.toString()
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            background = crearFondo(colorTeclaDefault, 6f)
            isClickable = true

            setOnClickListener {
                agregarLetra(letra)
            }
        }
    }

    private fun crearTeclaEspecial(
        etiqueta: String,
        ancho: Int,
        accion: () -> Unit
    ): TextView {
        return TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(ancho, dpToPx(46)).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }

            gravity = Gravity.CENTER
            text = etiqueta
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            background = crearFondo(colorTeclaDefault, 6f)
            isClickable = true

            setOnClickListener {
                accion()
            }
        }
    }

    private fun agregarLetra(letra: Char) {
        if (estado.terminado) return
        if (intentoActual.length >= longitud) return

        intentoActual.append(letra)
        actualizarFilaActual()
    }

    private fun borrarLetra() {
        if (estado.terminado) return

        if (intentoActual.isNotEmpty()) {
            intentoActual.deleteCharAt(intentoActual.length - 1)
            actualizarFilaActual()
        }
    }

    private fun actualizarFilaActual() {
        for (col in 0 until longitud) {
            val celda = celdas[filaActual][col]

            when {
                col < intentoActual.length -> {
                    celda.text = intentoActual[col].toString()
                    celda.background = crearFondo(null, 8f, colorBordeLleno, 2)
                }

                col == intentoActual.length -> {
                    celda.text = ""
                    celda.background = crearFondo(null, 8f, colorBordeActivo, 2)
                }

                else -> {
                    celda.text = ""
                    celda.background = crearFondo(null, 8f, colorBordeVacio, 2)
                }
            }
        }
    }

    private fun enviarIntento() {
        if (estado.terminado) return

        if (intentoActual.length < longitud) {
            Toast.makeText(
                requireContext(),
                "Completa la palabra",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val palabra = intentoActual.toString()

        // Ya no se valida si la palabra existe en el diccionario.
        // Se evalúa cualquier combinación de letras de longitud correcta.
        val colores = PalabraDelDiaManager.evaluarIntento(
            palabra,
            palabraObjetivo
        )

        deshabilitarTeclado()

        animarFila(filaActual, colores) {
            estado.intentos.add(palabra)
            actualizarTeclado(palabra, colores)
            PalabraDelDiaManager.guardarEstado(requireContext(), estado)

            when {
                palabra == palabraObjetivo -> terminarJuego(gano = true)

                estado.intentos.size >= 6 -> terminarJuego(gano = false)

                else -> {
                    filaActual++
                    intentoActual = StringBuilder()
                    habilitarTeclado()
                    actualizarFilaActual()
                }
            }
        }
    }

    private fun animarFila(
        fila: Int,
        colores: IntArray,
        alTerminar: () -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        val retrasoPorCelda = 220L

        for (col in 0 until longitud) {
            handler.postDelayed({
                val celda = celdas[fila][col]

                celda.animate()
                    .rotationX(90f)
                    .setDuration(150)
                    .withEndAction {
                        celda.background = crearFondo(
                            colorHex(colores[col]),
                            8f
                        )

                        celda.animate()
                            .rotationX(0f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }, col * retrasoPorCelda)
        }

        handler.postDelayed(
            { alTerminar() },
            longitud * retrasoPorCelda + 320L
        )
    }

    private fun colorHex(estadoLetra: Int): String {
        return when (estadoLetra) {
            PalabraDelDiaManager.VERDE -> colorVerde
            PalabraDelDiaManager.AMARILLO -> colorAmarillo
            else -> colorGris
        }
    }

    private fun actualizarTeclado(intento: String, colores: IntArray) {
        intento.forEachIndexed { indice, letra ->
            val nuevoEstado = colores[indice]
            val estadoActual = estadoTeclas[letra] ?: -1

            if (nuevoEstado > estadoActual) {
                estadoTeclas[letra] = nuevoEstado
                teclas[letra]?.background = crearFondo(
                    colorHex(nuevoEstado),
                    6f
                )
            }
        }
    }

    private fun deshabilitarTeclado() {
        teclas.values.forEach { it.isEnabled = false }
        teclasEspeciales.forEach { it.isEnabled = false }
    }

    private fun habilitarTeclado() {
        teclas.values.forEach { it.isEnabled = true }
        teclasEspeciales.forEach { it.isEnabled = true }
    }

    private fun restaurarPartida() {
        estado.intentos.forEachIndexed { fila, palabra ->
            val colores = PalabraDelDiaManager.evaluarIntento(
                palabra,
                palabraObjetivo
            )

            palabra.forEachIndexed { columna, letra ->
                celdas[fila][columna].text = letra.toString()
                celdas[fila][columna].background = crearFondo(
                    colorHex(colores[columna]),
                    8f
                )
            }

            actualizarTeclado(palabra, colores)
        }

        filaActual = estado.intentos.size

        when {
            estado.terminado -> deshabilitarTeclado()
            filaActual < 6 -> actualizarFilaActual()
        }
    }

    private fun terminarJuego(gano: Boolean) {
        estado.terminado = true
        estado.gano = gano

        PalabraDelDiaManager.guardarEstado(requireContext(), estado)
        PalabraDelDiaManager.registrarResultado(
            requireContext(),
            gano,
            estado.intentos.size
        )

        deshabilitarTeclado()
        mostrarDialogoFinal(gano)
    }

    private fun mostrarDialogoFinal(gano: Boolean) {
        val mensaje = if (gano) {
            "🎉 ¡Correcto!\n\nHas descubierto la palabra del día.\n\n" +
                    "Intentos utilizados: ${estado.intentos.size} de 6"
        } else {
            "Has perdido, intenta nuevamente mañana"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (gano) "¡Ganaste!" else "Fin del reto")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Volver") { dialog, _ ->
                dialog.dismiss()
                parentFragmentManager.popBackStack()
            }
            .show()
    }

    private fun crearFondo(
        colorRelleno: String?,
        radioDp: Float,
        colorBorde: String? = null,
        anchoBordeDp: Int = 0
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(radioDp.toInt()).toFloat()

            setColor(
                if (colorRelleno != null) {
                    Color.parseColor(colorRelleno)
                } else {
                    Color.TRANSPARENT
                }
            )

            if (colorBorde != null) {
                setStroke(
                    dpToPx(anchoBordeDp),
                    Color.parseColor(colorBorde)
                )
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}