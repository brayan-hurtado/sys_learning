package com.example.syslearning.actividades

import com.example.syslearning.model.Ejercicio
import com.example.syslearning.model.TipoEjercicio

/**
 * Lógica de evaluación de las micro actividades (capa de dominio).
 * No depende de Android, así se puede probar con JUnit
 * (ver app/src/test/.../EvaluadorEjercicioTest.kt).
 *
 * Reglas tomadas de la ficha de diseño pedagógico:
 *  - Retroalimentación en 3 niveles: 1er error -> pista; 2do error ->
 *    pista más concreta; 3er error -> solución explicada.
 *  - XP: comprobación al 1er intento 20, al 2do 10; completar 5.
 *  - Dominio: comprobación correcta en máximo 2 intentos.
 *  - Semáforo: verde 1er intento, amarillo 2do, rojo si no lo logró.
 */
object EvaluadorEjercicio {

    const val MAX_INTENTOS = 3
    const val INTENTOS_PARA_DOMINIO = 2

    const val XP_PRIMER_INTENTO = 20
    const val XP_SEGUNDO_INTENTO = 10
    const val XP_COMPLETAR = 5

    /** Respuesta que da el aprendiz. */
    sealed class Respuesta {

        /** Índice de la opción (o de la línea, en ENCONTRAR_ERROR). */
        data class Seleccion(val indice: Int) : Respuesta()

        /**
         * Orden elegido, expresado con los índices de las instrucciones
         * originales (que están guardadas en el orden correcto).
         * Correcto = [0, 1, 2, ...].
         */
        data class Orden(val indices: List<Int>) : Respuesta()
    }

    enum class Semaforo(val etiqueta: String) {
        VERDE("Verde"),
        AMARILLO("Amarillo"),
        ROJO("Rojo")
    }

    data class Retroalimentacion(
        val correcta: Boolean,
        val titulo: String,
        val mensaje: String,
        /** true cuando ya no se permiten más intentos. */
        val terminado: Boolean,
        /** true si el mensaje incluye la solución (3er error). */
        val mostroSolucion: Boolean
    )

    // ==================================================================
    // Evaluación
    // ==================================================================

    fun esCorrecta(ejercicio: Ejercicio, respuesta: Respuesta): Boolean {

        return when (respuesta) {

            is Respuesta.Seleccion ->
                ejercicio.tipoEjercicio().esSeleccion() &&
                        respuesta.indice == ejercicio.respuestaCorrecta

            is Respuesta.Orden ->
                ejercicio.tipoEjercicio() == TipoEjercicio.ORDENAR_INSTRUCCIONES &&
                        respuesta.indices.size == ejercicio.opciones.size &&
                        respuesta.indices == ejercicio.opciones.indices.toList()
        }
    }

    /**
     * Posición (desde 1) de la primera instrucción mal ubicada, o null si
     * el orden es correcto. Sirve para la pista "Revisa el paso N".
     */
    fun primeraPosicionIncorrecta(orden: List<Int>): Int? {
        val indice = orden.indices.firstOrNull { orden[it] != it }
        return indice?.plus(1)
    }

    /**
     * Arma la retroalimentación para el intento número [intento]
     * (1, 2 o 3) según si la respuesta fue correcta.
     */
    fun retroalimentar(
        ejercicio: Ejercicio,
        respuesta: Respuesta,
        intento: Int
    ): Retroalimentacion {

        val correcta = esCorrecta(ejercicio, respuesta)

        if (correcta) {
            return Retroalimentacion(
                correcta = true,
                titulo = if (intento == 1) "¡Correcto!" else "¡Bien, lo lograste!",
                mensaje = ejercicio.explicacion.ifBlank { "Tu respuesta es correcta." },
                terminado = true,
                mostroSolucion = false
            )
        }

        val pistaGenerica = pistaGenerica(ejercicio, respuesta)

        return when {

            intento <= 1 -> Retroalimentacion(
                correcta = false,
                titulo = "Todavía no",
                mensaje = ejercicio.pista1.ifBlank { pistaGenerica },
                terminado = false,
                mostroSolucion = false
            )

            intento == 2 -> Retroalimentacion(
                correcta = false,
                titulo = "Casi, una pista más",
                mensaje = ejercicio.pista2
                    .ifBlank { ejercicio.pista1 }
                    .ifBlank { pistaGenerica },
                terminado = false,
                mostroSolucion = false
            )

            else -> Retroalimentacion(
                correcta = false,
                titulo = "Veamos la solución",
                mensaje = buildString {
                    append(solucion(ejercicio))
                    if (ejercicio.explicacion.isNotBlank()) {
                        append("\n\n")
                        append(ejercicio.explicacion)
                    }
                },
                terminado = true,
                mostroSolucion = true
            )
        }
    }

    private fun pistaGenerica(ejercicio: Ejercicio, respuesta: Respuesta): String {

        return when (ejercicio.tipoEjercicio()) {

            TipoEjercicio.ORDENAR_INSTRUCCIONES -> {
                val posicion = (respuesta as? Respuesta.Orden)
                    ?.let { primeraPosicionIncorrecta(it.indices) }
                if (posicion != null) {
                    "Revisa el paso $posicion: ¿qué debe ocurrir antes de esa instrucción?"
                } else {
                    "Revisa el orden de las instrucciones."
                }
            }

            TipoEjercicio.ENCONTRAR_ERROR ->
                "Sigue el algoritmo línea por línea y anota el valor de cada variable."

            TipoEjercicio.PREDECIR_RESULTADO ->
                "Haz la traza: escribe el valor de las variables después de cada instrucción."

            TipoEjercicio.COMPLETAR_CODIGO ->
                "Piensa qué necesita el algoritmo justo en la línea que falta."

            else -> "Vuelve a leer el concepto y revisa tu respuesta."
        }
    }

    /** Texto con la respuesta correcta, para el tercer error. */
    fun solucion(ejercicio: Ejercicio): String {

        val opciones = ejercicio.opciones

        return when (ejercicio.tipoEjercicio()) {

            TipoEjercicio.ORDENAR_INSTRUCCIONES ->
                "El orden correcto es:\n" +
                        opciones.mapIndexed { i, paso -> "${i + 1}. $paso" }
                            .joinToString("\n")

            TipoEjercicio.ENCONTRAR_ERROR -> {
                val linea = opciones.getOrNull(ejercicio.respuestaCorrecta) ?: ""
                "El error está en la línea ${ejercicio.respuestaCorrecta + 1}: $linea"
            }

            else -> {
                val opcion = opciones.getOrNull(ejercicio.respuestaCorrecta) ?: ""
                "La respuesta correcta es: $opcion"
            }
        }
    }

    // ==================================================================
    // Recompensa y seguimiento
    // ==================================================================

    /**
     * [intentoAcierto] = intento en el que acertó la comprobación, o null
     * si no la acertó en los 3 intentos.
     */
    fun xpComprobacion(intentoAcierto: Int?): Int {
        return when (intentoAcierto) {
            1 -> XP_PRIMER_INTENTO
            2 -> XP_SEGUNDO_INTENTO
            else -> 0
        }
    }

    fun xpTotal(intentoAcierto: Int?): Int = XP_COMPLETAR + xpComprobacion(intentoAcierto)

    fun dominada(intentoAcierto: Int?): Boolean =
        intentoAcierto != null && intentoAcierto <= INTENTOS_PARA_DOMINIO

    fun semaforo(intentoAcierto: Int?): Semaforo {
        return when (intentoAcierto) {
            1 -> Semaforo.VERDE
            2 -> Semaforo.AMARILLO
            else -> Semaforo.ROJO
        }
    }

    /** Validación del ejercicio cuando el administrador lo guarda. */
    fun validarParaGuardar(ejercicio: Ejercicio): String? {

        val tipo = ejercicio.tipoEjercicio()
        val opciones = ejercicio.opciones

        if (ejercicio.enunciado.isBlank()) return "Escribe el enunciado."

        return when (tipo) {

            TipoEjercicio.VERDADERO_FALSO ->
                if (ejercicio.respuestaCorrecta !in 0..1) "Elige si es verdadero o falso." else null

            TipoEjercicio.ORDENAR_INSTRUCCIONES ->
                if (opciones.size < 3) "Escribe al menos 3 instrucciones." else null

            else -> when {
                opciones.size < 2 -> "Escribe al menos 2 opciones (una por línea)."
                ejercicio.respuestaCorrecta !in opciones.indices ->
                    "La respuesta correcta debe estar entre 1 y ${opciones.size}."
                else -> null
            }
        }
    }
}
