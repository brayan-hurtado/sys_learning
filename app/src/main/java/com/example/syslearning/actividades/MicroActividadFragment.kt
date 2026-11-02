package com.example.syslearning.actividades

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.syslearning.R
import com.example.syslearning.actividades.EvaluadorEjercicio.Respuesta
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.Ejercicio
import com.example.syslearning.model.PasoEjercicio
import com.example.syslearning.model.TipoEjercicio
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Sesión de una micro actividad de 3 a 5 minutos, con los 7 pasos de la
 * ficha de diseño pedagógico:
 *
 *  1. Presentar el reto
 *  2. Explicar el concepto
 *  3. Mostrar un ejemplo hecho
 *  4. Actividad práctica           (ejercicios con paso PRACTICA)
 *  5. Retroalimentación            (tarjeta inferior, después de responder)
 *  6. Comprobación del aprendizaje (un ejercicio COMPROBACION al azar)
 *  7. Recompensa y registro
 *
 * Con vistaPrevia = true (desde el panel de administrador) se recorre la
 * actividad completa sin registrar eventos ni XP.
 */
class MicroActividadFragment : Fragment(R.layout.fragment_micro_actividad) {

    private enum class Etapa(val numero: Int, val nombre: String) {
        CARGANDO(0, "Cargando"),
        RETO(1, "Presentar el reto"),
        CONCEPTO(2, "Explicar el concepto"),
        EJEMPLO(3, "Ejemplo resuelto"),
        PRACTICA(4, "Actividad práctica"),
        COMPROBACION(6, "Comprobación del aprendizaje"),
        RECOMPENSA(7, "Recompensa y registro")
    }

    // ---- Vistas ----
    private lateinit var progresoPasos: ProgressBar
    private lateinit var tvPaso: TextView
    private lateinit var tvXp: TextView
    private lateinit var scroll: ScrollView
    private lateinit var contenedor: LinearLayout
    private lateinit var cardRetro: MaterialCardView
    private lateinit var tvRetroTitulo: TextView
    private lateinit var tvRetroMensaje: TextView
    private lateinit var btnPrincipal: MaterialButton

    // ---- Datos ----
    private var actividadId = ""
    private var vistaPrevia = false
    private var actividad: Actividad? = null
    private var practicas: List<Ejercicio> = emptyList()
    private var comprobaciones: List<Ejercicio> = emptyList()

    // ---- Estado de la sesión ----
    private var etapa = Etapa.CARGANDO
    private var indicePractica = 0
    private var comprobacionActual: Ejercicio? = null
    private var ejercicioActual: Ejercicio? = null
    private var intento = 1
    private var pistaVista = false
    private var seleccion: Int? = null
    private val ordenActual = mutableListOf<Int>()
    private var tiempoInicioEjercicio = 0L
    private var tiempoInicioActividad = 0L
    private var botonesOpciones = mutableListOf<TextView>()

    /** Intento en que acertó la comprobación (null = no la acertó). */
    private var intentoAciertoComprobacion: Int? = null
    private var intentosComprobacion = 0
    private var intentoAciertoUltimaPractica: Int? = null
    private var terminada = false

    // ---- Colores ----
    private val azul = Color.parseColor("#143C8A")
    private val texto = Color.parseColor("#212121")
    private val textoSuave = Color.parseColor("#616161")
    private val verde = Color.parseColor("#2E7D32")
    private val verdeFondo = Color.parseColor("#E8F5E9")
    private val rojo = Color.parseColor("#C62828")
    private val rojoFondo = Color.parseColor("#FFEBEE")
    private val ambar = Color.parseColor("#B26A00")
    private val ambarFondo = Color.parseColor("#FFF3E0")
    private val bordeNeutro = Color.parseColor("#D0D7E2")
    private val seleccionFondo = Color.parseColor("#E3ECFA")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actividadId = arguments?.getString(ARG_ID) ?: ""
        vistaPrevia = arguments?.getBoolean(ARG_VISTA_PREVIA) ?: false

        progresoPasos = view.findViewById(R.id.progresoPasos)
        tvPaso = view.findViewById(R.id.tvPasoMicro)
        tvXp = view.findViewById(R.id.tvXpMicro)
        scroll = view.findViewById(R.id.scrollMicro)
        contenedor = view.findViewById(R.id.contenedorPaso)
        cardRetro = view.findViewById(R.id.cardRetro)
        tvRetroTitulo = view.findViewById(R.id.tvRetroTitulo)
        tvRetroMensaje = view.findViewById(R.id.tvRetroMensaje)
        btnPrincipal = view.findViewById(R.id.btnPrincipalMicro)

        view.findViewById<TextView>(R.id.tvVistaPrevia).visibility =
            if (vistaPrevia) View.VISIBLE else View.GONE

        view.findViewById<TextView>(R.id.btnSalirMicro).setOnClickListener {
            confirmarSalida()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmarSalida()
                }
            }
        )

        // La barra de navegación flotante del aprendiz taparía el botón
        // principal; se oculta mientras dura la actividad.
        requireActivity().findViewById<View>(R.id.cardBarraInferior)?.visibility = View.GONE

        cargar()
    }

    override fun onDestroyView() {
        activity?.findViewById<View>(R.id.cardBarraInferior)?.visibility = View.VISIBLE
        super.onDestroyView()
    }

    // ==================================================================
    // Carga
    // ==================================================================

    private fun cargar() {

        etapa = Etapa.CARGANDO
        actualizarEncabezado()
        contenedor.removeAllViews()
        contenedor.addView(textoCuerpo("Cargando la actividad…"))
        btnPrincipal.visibility = View.GONE

        FirebaseFirestore.getInstance()
            .collection("actividades")
            .document(actividadId)
            .get()
            .addOnSuccessListener { doc ->

                if (!isAdded) return@addOnSuccessListener

                val datos = doc.toObject(Actividad::class.java)

                if (datos == null) {
                    mostrarError("La actividad ya no existe.")
                    return@addOnSuccessListener
                }

                datos.id = doc.id
                actividad = datos
                cargarEjercicios()
            }
            .addOnFailureListener {
                if (isAdded) mostrarError("No se pudo cargar la actividad.")
            }
    }

    private fun cargarEjercicios() {

        EjerciciosRepository.obtenerDeActividad(
            actividadId = actividadId,
            soloActivos = true,
            onSuccess = { lista ->

                if (!isAdded) return@obtenerDeActividad

                practicas = lista.filter { it.pasoEjercicio() == PasoEjercicio.PRACTICA }
                comprobaciones = lista.filter { it.pasoEjercicio() == PasoEjercicio.COMPROBACION }

                if (practicas.isEmpty() && comprobaciones.isEmpty()) {
                    mostrarError("Esta actividad todavía no tiene ejercicios.")
                    return@obtenerDeActividad
                }

                iniciarActividad()
            },
            onError = {
                if (isAdded) mostrarError("No se pudieron cargar los ejercicios.")
            }
        )
    }

    private fun iniciarActividad() {

        tiempoInicioActividad = System.currentTimeMillis()
        terminada = false
        intentoAciertoComprobacion = null
        intentosComprobacion = 0
        intentoAciertoUltimaPractica = null

        // Una comprobación distinta en cada intento de la actividad.
        comprobacionActual = comprobaciones.randomOrNull()

        val act = actividad ?: return

        if (!vistaPrevia) {
            RegistroEventos.registrar(
                RegistroEventos.INICIO_ACTIVIDAD,
                mapOf(
                    "moduloId" to act.modulo,
                    "actividadId" to act.id,
                    "comprobacionId" to (comprobacionActual?.id ?: "")
                )
            )
        }

        irA(Etapa.RETO)
    }

    // ==================================================================
    // Navegación entre pasos
    // ==================================================================

    private fun irA(destino: Etapa) {

        val act = actividad ?: return

        // Los pasos sin contenido se saltan.
        val siguiente = when {
            destino == Etapa.RETO && act.reto.isBlank() -> Etapa.CONCEPTO
            destino == Etapa.CONCEPTO && act.concepto.isBlank() -> Etapa.EJEMPLO
            destino == Etapa.EJEMPLO &&
                    act.ejemploCodigo.isBlank() && act.ejemploExplicacion.isBlank() -> Etapa.PRACTICA
            destino == Etapa.PRACTICA && practicas.isEmpty() -> Etapa.COMPROBACION
            destino == Etapa.COMPROBACION && comprobacionActual == null -> Etapa.RECOMPENSA
            else -> destino
        }

        if (siguiente != destino) {
            irA(siguiente)
            return
        }

        etapa = destino
        ocultarRetro()
        contenedor.removeAllViews()
        scroll.scrollTo(0, 0)
        btnPrincipal.visibility = View.VISIBLE

        when (destino) {
            Etapa.CARGANDO -> Unit
            Etapa.RETO -> mostrarReto(act)
            Etapa.CONCEPTO -> mostrarConcepto(act)
            Etapa.EJEMPLO -> mostrarEjemplo(act)
            Etapa.PRACTICA -> {
                indicePractica = 0
                mostrarEjercicio(practicas[0])
            }
            Etapa.COMPROBACION -> comprobacionActual?.let { mostrarEjercicio(it) }
            Etapa.RECOMPENSA -> mostrarRecompensa(act)
        }

        actualizarEncabezado()

        if (!vistaPrevia && destino.numero in 1..3) {
            RegistroEventos.registrar(
                RegistroEventos.PASO_VISTO,
                mapOf(
                    "moduloId" to act.modulo,
                    "actividadId" to act.id,
                    "paso" to destino.name
                )
            )
        }
    }

    private fun actualizarEncabezado(pasoRetro: Boolean = false) {

        val numero = if (pasoRetro) 5 else etapa.numero
        val nombre = if (pasoRetro) "Retroalimentación" else etapa.nombre

        progresoPasos.progress = numero

        tvPaso.text = when (etapa) {
            Etapa.CARGANDO -> "Cargando…"
            Etapa.PRACTICA -> {
                val extra = if (practicas.size > 1) " (${indicePractica + 1} de ${practicas.size})" else ""
                "Paso $numero de 7 · $nombre$extra"
            }
            else -> "Paso $numero de 7 · $nombre"
        }
    }

    // ==================================================================
    // Pasos 1 a 3: reto, concepto y ejemplo
    // ==================================================================

    private fun mostrarReto(act: Actividad) {

        contenedor.addView(etiqueta("Módulo ${act.modulo} · ${act.titulo}"))
        contenedor.addView(textoTitulo("🧩 ${act.reto}"))

        if (act.objetivo.isNotBlank()) {
            contenedor.addView(espacio(16))
            contenedor.addView(etiqueta("Al terminar podrás"))
            contenedor.addView(textoCuerpo(act.objetivo))
        }

        configurarBoton("Empezar") { irA(Etapa.CONCEPTO) }
    }

    private fun mostrarConcepto(act: Actividad) {

        contenedor.addView(etiqueta("Concepto"))
        contenedor.addView(textoTitulo(act.titulo))
        contenedor.addView(espacio(8))
        contenedor.addView(textoCuerpo(act.concepto))

        configurarBoton("Ver un ejemplo") { irA(Etapa.EJEMPLO) }
    }

    private fun mostrarEjemplo(act: Actividad) {

        contenedor.addView(etiqueta("Ejemplo resuelto"))

        if (act.ejemploCodigo.isNotBlank()) {
            contenedor.addView(bloqueCodigo(act.ejemploCodigo))
        }

        if (act.ejemploExplicacion.isNotBlank()) {
            contenedor.addView(espacio(12))
            contenedor.addView(etiqueta("¿Qué pasa al ejecutarlo?"))
            contenedor.addView(textoCuerpo(act.ejemploExplicacion))
        }

        configurarBoton("¡A practicar!") { irA(Etapa.PRACTICA) }
    }

    // ==================================================================
    // Pasos 4 y 6: ejercicios
    // ==================================================================

    private fun mostrarEjercicio(ejercicio: Ejercicio) {

        ejercicioActual = ejercicio
        intento = 1
        pistaVista = false
        seleccion = null
        tiempoInicioEjercicio = System.currentTimeMillis()

        if (ejercicio.tipoEjercicio() == TipoEjercicio.ORDENAR_INSTRUCCIONES) {
            ordenActual.clear()
            ordenActual.addAll(desordenar(ejercicio.opciones.size))
        }

        dibujarEjercicio()
    }

    /** Desordena garantizando que no quede ya en el orden correcto. */
    private fun desordenar(cantidad: Int): List<Int> {
        val correcto = (0 until cantidad).toList()
        if (cantidad < 2) return correcto
        var mezcla = correcto.shuffled()
        while (mezcla == correcto) mezcla = correcto.shuffled()
        return mezcla
    }

    private fun dibujarEjercicio(resaltarCorrecta: Boolean = false, bloqueado: Boolean = false) {

        val ejercicio = ejercicioActual ?: return
        val tipo = ejercicio.tipoEjercicio()

        contenedor.removeAllViews()
        botonesOpciones.clear()

        contenedor.addView(etiqueta(tipo.etiqueta))
        contenedor.addView(textoTitulo(ejercicio.enunciado))

        if (ejercicio.codigo.isNotBlank() && tipo != TipoEjercicio.ENCONTRAR_ERROR) {
            contenedor.addView(bloqueCodigo(ejercicio.codigo))
        }

        contenedor.addView(espacio(12))

        when (tipo) {
            TipoEjercicio.ORDENAR_INSTRUCCIONES -> dibujarOrdenar(ejercicio, bloqueado)
            TipoEjercicio.ENCONTRAR_ERROR -> dibujarLineasError(ejercicio, resaltarCorrecta, bloqueado)
            else -> dibujarOpciones(ejercicio, resaltarCorrecta, bloqueado)
        }

        if (!bloqueado) {
            configurarBoton("Comprobar") { comprobarRespuesta() }
            actualizarBotonComprobar()
        }
    }

    private fun opcionesDe(ejercicio: Ejercicio): List<String> {
        return if (ejercicio.tipoEjercicio() == TipoEjercicio.VERDADERO_FALSO) {
            listOf("Verdadero", "Falso")
        } else {
            ejercicio.opciones
        }
    }

    private fun dibujarOpciones(ejercicio: Ejercicio, resaltarCorrecta: Boolean, bloqueado: Boolean) {

        opcionesDe(ejercicio).forEachIndexed { indice, textoOpcion ->

            val opcion = tarjetaOpcion(textoOpcion, monoespaciado = false)

            pintarOpcion(opcion, indice, ejercicio, resaltarCorrecta)

            if (!bloqueado) {
                opcion.setOnClickListener {
                    seleccion = indice
                    botonesOpciones.forEachIndexed { i, vista ->
                        pintarOpcion(vista, i, ejercicio, false)
                    }
                    actualizarBotonComprobar()
                }
            }

            botonesOpciones.add(opcion)
            contenedor.addView(opcion)
        }
    }

    private fun dibujarLineasError(ejercicio: Ejercicio, resaltarCorrecta: Boolean, bloqueado: Boolean) {

        contenedor.addView(textoAyuda("Toca la línea que tiene el error."))

        ejercicio.opciones.forEachIndexed { indice, linea ->

            val vista = tarjetaOpcion("${indice + 1}  $linea", monoespaciado = true)

            pintarOpcion(vista, indice, ejercicio, resaltarCorrecta)

            if (!bloqueado) {
                vista.setOnClickListener {
                    seleccion = indice
                    botonesOpciones.forEachIndexed { i, v -> pintarOpcion(v, i, ejercicio, false) }
                    actualizarBotonComprobar()
                }
            }

            botonesOpciones.add(vista)
            contenedor.addView(vista)
        }
    }

    private fun pintarOpcion(vista: TextView, indice: Int, ejercicio: Ejercicio, resaltarCorrecta: Boolean) {

        val (fondo, borde) = when {
            resaltarCorrecta && indice == ejercicio.respuestaCorrecta -> verdeFondo to verde
            resaltarCorrecta && indice == seleccion -> rojoFondo to rojo
            indice == seleccion -> seleccionFondo to azul
            else -> Color.WHITE to bordeNeutro
        }

        vista.background = fondoRedondeado(fondo, borde, if (borde == bordeNeutro) 1 else 2)
    }

    private fun dibujarOrdenar(ejercicio: Ejercicio, bloqueado: Boolean) {

        contenedor.addView(textoAyuda("Usa las flechas para poner las instrucciones en orden."))

        ordenActual.forEachIndexed { posicion, indiceOriginal ->

            val fila = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = fondoRedondeado(Color.WHITE, bordeNeutro, 1)
                setPadding(dp(12), dp(6), dp(6), dp(6))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
            }

            val tvTexto = TextView(requireContext()).apply {
                text = "${posicion + 1}. ${ejercicio.opciones[indiceOriginal]}"
                setTextColor(texto)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                typeface = Typeface.MONOSPACE
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            fila.addView(tvTexto)

            if (!bloqueado) {
                fila.addView(botonFlecha("▲", "Subir", posicion > 0) { mover(posicion, -1) })
                fila.addView(botonFlecha("▼", "Bajar", posicion < ordenActual.size - 1) { mover(posicion, 1) })
            }

            contenedor.addView(fila)
        }
    }

    private fun mover(posicion: Int, direccion: Int) {
        val destino = posicion + direccion
        if (destino !in ordenActual.indices) return
        val temporal = ordenActual[posicion]
        ordenActual[posicion] = ordenActual[destino]
        ordenActual[destino] = temporal
        dibujarEjercicio()
    }

    private fun actualizarBotonComprobar() {

        val ejercicio = ejercicioActual ?: return

        val listo = ejercicio.tipoEjercicio() == TipoEjercicio.ORDENAR_INSTRUCCIONES || seleccion != null

        btnPrincipal.isEnabled = listo
        btnPrincipal.alpha = if (listo) 1f else 0.5f
    }

    // ==================================================================
    // Paso 5: retroalimentación
    // ==================================================================

    private fun comprobarRespuesta() {

        val ejercicio = ejercicioActual ?: return
        val act = actividad ?: return

        val respuesta: Respuesta =
            if (ejercicio.tipoEjercicio() == TipoEjercicio.ORDENAR_INSTRUCCIONES) {
                Respuesta.Orden(ordenActual.toList())
            } else {
                Respuesta.Seleccion(seleccion ?: return)
            }

        val retro = EvaluadorEjercicio.retroalimentar(ejercicio, respuesta, intento)

        if (!vistaPrevia) {
            RegistroEventos.respuesta(
                moduloId = act.modulo,
                actividadId = act.id,
                ejercicioId = ejercicio.id,
                tipoEjercicio = ejercicio.tipo,
                paso = ejercicio.paso,
                intento = intento,
                correcto = retro.correcta,
                respuesta = describirRespuesta(respuesta),
                pistaVista = pistaVista,
                tiempoMs = System.currentTimeMillis() - tiempoInicioEjercicio
            )
        }

        val esComprobacion = etapa == Etapa.COMPROBACION

        if (esComprobacion) intentosComprobacion = intento

        if (retro.correcta) {
            if (esComprobacion) {
                intentoAciertoComprobacion = intento
                tvXp.text = "⭐ ${EvaluadorEjercicio.xpComprobacion(intento)} XP"
            } else {
                intentoAciertoUltimaPractica = intento
            }
        } else if (!retro.terminado) {
            pistaVista = true
        }

        // Muestra lo elegido en verde/rojo; en el 3er error, también la correcta.
        dibujarEjercicio(resaltarCorrecta = retro.correcta || retro.mostroSolucion, bloqueado = true)

        mostrarRetro(retro)

        if (retro.terminado) {
            configurarBoton("Continuar") { avanzarDespuesDeEjercicio() }
        } else {
            configurarBoton("Intentar de nuevo") {
                intento++
                seleccion = null
                ocultarRetro()
                actualizarEncabezado()
                dibujarEjercicio()
            }
        }
    }

    private fun describirRespuesta(respuesta: Respuesta): String {
        return when (respuesta) {
            is Respuesta.Seleccion -> respuesta.indice.toString()
            is Respuesta.Orden -> respuesta.indices.joinToString(",")
        }
    }

    private fun mostrarRetro(retro: EvaluadorEjercicio.Retroalimentacion) {

        val (color, fondo) = when {
            retro.correcta -> verde to verdeFondo
            retro.mostroSolucion -> ambar to ambarFondo
            else -> rojo to rojoFondo
        }

        val icono = when {
            retro.correcta -> "✅"
            retro.mostroSolucion -> "📘"
            else -> "💡"
        }

        cardRetro.setCardBackgroundColor(fondo)
        cardRetro.strokeColor = color
        tvRetroTitulo.setTextColor(color)
        tvRetroTitulo.text = "$icono ${retro.titulo}"
        tvRetroMensaje.text = retro.mensaje
        cardRetro.visibility = View.VISIBLE

        actualizarEncabezado(pasoRetro = true)
    }

    private fun ocultarRetro() {
        cardRetro.visibility = View.GONE
    }

    private fun avanzarDespuesDeEjercicio() {

        if (etapa == Etapa.PRACTICA && indicePractica < practicas.size - 1) {
            indicePractica++
            ocultarRetro()
            mostrarEjercicio(practicas[indicePractica])
            actualizarEncabezado()
            return
        }

        if (etapa == Etapa.PRACTICA) {
            irA(Etapa.COMPROBACION)
        } else {
            irA(Etapa.RECOMPENSA)
        }
    }

    // ==================================================================
    // Paso 7: recompensa y registro
    // ==================================================================

    private fun mostrarRecompensa(act: Actividad) {

        // Si la actividad no tiene comprobación, se evalúa con la última práctica.
        val intentoAcierto =
            if (comprobacionActual != null) intentoAciertoComprobacion else intentoAciertoUltimaPractica

        val dominada = EvaluadorEjercicio.dominada(intentoAcierto)
        val xp = EvaluadorEjercicio.xpTotal(intentoAcierto)
        val semaforo = EvaluadorEjercicio.semaforo(intentoAcierto)

        var racha = 0

        if (!vistaPrevia && !terminada) {

            terminada = true

            racha = SesionActividadesManager.registrarMicroActividad(
                context = requireContext(),
                actividadId = act.id,
                actividadTitulo = act.titulo,
                moduloId = act.modulo,
                dominada = dominada,
                xpGanado = xp,
                semaforo = semaforo.name,
                intentosComprobacion = intentosComprobacion
            )

            RegistroEventos.registrar(
                RegistroEventos.FIN_ACTIVIDAD,
                mapOf(
                    "moduloId" to act.modulo,
                    "actividadId" to act.id,
                    "dominada" to dominada,
                    "semaforo" to semaforo.name,
                    "xp" to xp,
                    "intentosComprobacion" to intentosComprobacion,
                    "duracionMs" to (System.currentTimeMillis() - tiempoInicioActividad)
                )
            )
        }

        tvXp.text = "⭐ $xp XP"

        contenedor.gravity = Gravity.CENTER_HORIZONTAL

        contenedor.addView(TextView(requireContext()).apply {
            text = if (dominada) "🏆" else "💪"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 56f)
            gravity = Gravity.CENTER
        })

        contenedor.addView(textoTitulo(
            if (dominada) "¡Micro actividad dominada!" else "Actividad terminada"
        ).apply { gravity = Gravity.CENTER })

        contenedor.addView(espacio(12))
        contenedor.addView(filaResumen("XP ganado", "+$xp XP"))

        if (!vistaPrevia) {
            contenedor.addView(filaResumen("Racha", "🔥 $racha día(s)"))
        }

        val (colorSemaforo, textoSemaforo) = when (semaforo) {
            EvaluadorEjercicio.Semaforo.VERDE -> verde to "Verde: lo dominas"
            EvaluadorEjercicio.Semaforo.AMARILLO -> ambar to "Amarillo: vas bien"
            EvaluadorEjercicio.Semaforo.ROJO -> rojo to "Rojo: conviene reforzar"
        }

        contenedor.addView(filaResumen("Desempeño", textoSemaforo, colorSemaforo))

        contenedor.addView(espacio(16))

        contenedor.addView(textoCuerpo(
            if (dominada) {
                "Superaste la comprobación. La siguiente actividad de la ruta ya está disponible."
            } else {
                "Repasa el concepto y vuelve a intentarlo: la comprobación será distinta. " +
                        "Así aprendes la idea y no solo la respuesta."
            }
        ).apply { gravity = Gravity.CENTER })

        if (dominada) {
            configurarBoton("Continuar") { salir() }
        } else {
            configurarBoton("Repasar y reintentar") {
                contenedor.gravity = Gravity.NO_GRAVITY
                tvXp.text = "⭐ 0 XP"
                iniciarActividad()
            }

            contenedor.addView(espacio(8))
            contenedor.addView(TextView(requireContext()).apply {
                text = "Volver a las actividades"
                setTextColor(azul)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(12), dp(12), dp(12))
                setOnClickListener { salir() }
            })
        }
    }

    private fun filaResumen(etiquetaTexto: String, valor: String, colorValor: Int = texto): View {

        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            background = fondoRedondeado(Color.WHITE, bordeNeutro, 1)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }

            addView(TextView(context).apply {
                text = etiquetaTexto
                setTextColor(textoSuave)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(context).apply {
                text = valor
                setTextColor(colorValor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(typeface, Typeface.BOLD)
            })
        }
    }

    // ==================================================================
    // Salida y errores
    // ==================================================================

    private fun confirmarSalida() {

        if (etapa == Etapa.RECOMPENSA || etapa == Etapa.CARGANDO || vistaPrevia) {
            salir()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("¿Salir de la actividad?")
            .setMessage("Tu avance en esta micro actividad no se guardará.")
            .setNegativeButton("Seguir", null)
            .setPositiveButton("Salir") { _, _ ->
                actividad?.let { act ->
                    RegistroEventos.registrar(
                        RegistroEventos.ABANDONO,
                        mapOf(
                            "moduloId" to act.modulo,
                            "actividadId" to act.id,
                            "paso" to etapa.name,
                            "duracionMs" to (System.currentTimeMillis() - tiempoInicioActividad)
                        )
                    )
                }
                salir()
            }
            .show()
    }

    private fun salir() {
        if (isAdded) parentFragmentManager.popBackStack()
    }

    private fun mostrarError(mensaje: String) {
        contenedor.removeAllViews()
        contenedor.addView(textoCuerpo(mensaje))
        configurarBoton("Volver") { salir() }
        btnPrincipal.visibility = View.VISIBLE
        if (isAdded) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    // ==================================================================
    // Componentes de interfaz
    // ==================================================================

    private fun configurarBoton(textoBoton: String, accion: () -> Unit) {
        btnPrincipal.text = textoBoton
        btnPrincipal.isEnabled = true
        btnPrincipal.alpha = 1f
        btnPrincipal.setOnClickListener { accion() }
    }

    private fun dp(valor: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            valor.toFloat(),
            resources.displayMetrics
        ).toInt()

    private fun espacio(alto: Int): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(alto))
    }

    private fun etiqueta(valor: String): TextView = TextView(requireContext()).apply {
        text = valor.uppercase()
        setTextColor(azul)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        letterSpacing = 0.06f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 0, 0, dp(6))
    }

    private fun textoTitulo(valor: String): TextView = TextView(requireContext()).apply {
        text = valor
        setTextColor(texto)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        setTypeface(typeface, Typeface.BOLD)
        setLineSpacing(dp(2).toFloat(), 1f)
    }

    private fun textoCuerpo(valor: String): TextView = TextView(requireContext()).apply {
        text = valor
        setTextColor(texto)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setLineSpacing(dp(3).toFloat(), 1f)
    }

    private fun textoAyuda(valor: String): TextView = TextView(requireContext()).apply {
        text = valor
        setTextColor(textoSuave)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setPadding(0, 0, 0, dp(4))
    }

    /** Bloque de código con fuente monoespaciada y desplazamiento horizontal. */
    private fun bloqueCodigo(codigo: String): View {

        val tvCodigo = TextView(requireContext()).apply {
            text = codigo
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#E2E8F0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setLineSpacing(dp(2).toFloat(), 1f)
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        return HorizontalScrollView(requireContext()).apply {
            background = fondoRedondeado(Color.parseColor("#1E293B"), Color.parseColor("#1E293B"), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
            addView(tvCodigo)
        }
    }

    private fun tarjetaOpcion(valor: String, monoespaciado: Boolean): TextView =
        TextView(requireContext()).apply {
            text = valor
            setTextColor(texto)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (monoespaciado) 14f else 16f)
            if (monoespaciado) typeface = Typeface.MONOSPACE
            minHeight = dp(52)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }

    private fun botonFlecha(simbolo: String, descripcion: String, activo: Boolean, accion: () -> Unit): TextView =
        TextView(requireContext()).apply {
            text = simbolo
            contentDescription = descripcion
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(if (activo) azul else bordeNeutro)
            layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            if (activo) setOnClickListener { accion() }
        }

    private fun fondoRedondeado(colorFondo: Int, colorBorde: Int, grosorDp: Int): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(14).toFloat()
            setColor(colorFondo)
            if (grosorDp > 0) setStroke(dp(grosorDp), colorBorde)
        }

    companion object {

        private const val ARG_ID = "actividadId"
        private const val ARG_VISTA_PREVIA = "vistaPrevia"

        fun newInstance(actividadId: String, vistaPrevia: Boolean = false): MicroActividadFragment {
            return MicroActividadFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, actividadId)
                    putBoolean(ARG_VISTA_PREVIA, vistaPrevia)
                }
            }
        }
    }
}
