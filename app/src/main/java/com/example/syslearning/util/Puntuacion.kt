package com.example.syslearning.util

/**
 * Reglas de gamificación (RF 11): puntos del ranking, niveles y resumen
 * de actividades. No depende de Android ni de Firebase, así se puede
 * probar con JUnit (PuntuacionTest).
 *
 * Puntos del ranking:
 *   📚 Curso leído                      100 c/u
 *   🧩 Actividad dominada                50 c/u
 *   ⭐ XP de las micro actividades       1 punto por XP
 *   🎯 Precisión (dominadas / intentos)  hasta 100 (100 % = 100 puntos)
 *   🔤 La Palabra del Día                racha × 15 + ganadas × 50 + bonus
 */
object Puntuacion {

    const val PUNTOS_CURSO = 100
    const val PUNTOS_ACTIVIDAD = 50
    const val PUNTOS_RACHA_PALABRA = 15
    const val PUNTOS_PALABRA_GANADA = 50
    const val XP_POR_NIVEL = 200

    data class ResumenActividades(
        /** Actividades con al menos un intento terminado. */
        val realizadas: Int,
        /** Actividades dominadas (completado = true). */
        val completadas: Int,
        /** Veces que terminó una micro actividad (todas las actividades). */
        val intentos: Int,
        /** Ids de las actividades dominadas. */
        val idsCompletadas: Set<String>
    ) {
        /** Porcentaje de intentos que terminaron en dominio (0 a 100). */
        val precision: Int
            get() = if (intentos > 0) ((completadas * 100) / intentos).coerceIn(0, 100) else 0
    }

    /**
     * Lee el mapa usuarios/{uid}.actividadesProgreso que escribe
     * SesionActividadesManager. Las entradas del quiz anterior no tienen
     * "veces": se cuentan como 1 intento.
     */
    fun resumirActividades(actividadesProgreso: Map<*, *>?): ResumenActividades {

        if (actividadesProgreso == null) return ResumenActividades(0, 0, 0, emptySet())

        var realizadas = 0
        var intentos = 0
        val completadas = mutableSetOf<String>()

        actividadesProgreso.forEach { (clave, valor) ->

            val datos = valor as? Map<*, *> ?: return@forEach
            val id = clave as? String ?: return@forEach

            val veces = (datos["veces"] as? Number)?.toInt() ?: 1

            realizadas++
            intentos += veces.coerceAtLeast(1)

            if (datos["completado"] == true) completadas.add(id)
        }

        return ResumenActividades(realizadas, completadas.size, intentos, completadas)
    }

    fun bonusPalabraDelDia(totalJugados: Int, totalIntentos: Int): Int {
        if (totalJugados <= 0) return 0
        return ((7.0 - (totalIntentos.toDouble() / totalJugados)) * 20)
            .coerceAtLeast(0.0)
            .toInt()
    }

    fun puntos(
        cursosCompletados: Int,
        actividades: ResumenActividades,
        xpActividades: Int,
        rachaPalabra: Int,
        palabrasGanadas: Int,
        palabrasJugadas: Int,
        intentosPalabra: Int
    ): Int {
        return cursosCompletados * PUNTOS_CURSO +
                actividades.completadas * PUNTOS_ACTIVIDAD +
                xpActividades.coerceAtLeast(0) +
                actividades.precision +
                rachaPalabra * PUNTOS_RACHA_PALABRA +
                palabrasGanadas * PUNTOS_PALABRA_GANADA +
                bonusPalabraDelDia(palabrasJugadas, intentosPalabra)
    }

    data class Nivel(val numero: Int, val xpEnNivel: Int, val xpParaSubir: Int)

    /** Nivel 1 con 0 XP; se sube cada [XP_POR_NIVEL] XP. */
    fun nivel(xp: Int): Nivel {
        val total = xp.coerceAtLeast(0)
        return Nivel(
            numero = total / XP_POR_NIVEL + 1,
            xpEnNivel = total % XP_POR_NIVEL,
            xpParaSubir = XP_POR_NIVEL - total % XP_POR_NIVEL
        )
    }

    /** Porcentaje de avance general: cursos y actividades cuentan igual. */
    fun porcentajeGeneral(completados: Int, total: Int): Int {
        if (total <= 0) return 0
        return ((completados * 100) / total).coerceIn(0, 100)
    }
}
