package com.example.syslearning.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuntuacionTest {

    private val progreso = mapOf(
        "a1" to mapOf("completado" to true, "veces" to 1L),
        "a2" to mapOf("completado" to true, "veces" to 3L),
        "a3" to mapOf("porReforzar" to true, "veces" to 2L),
        // Entrada del quiz anterior: sin "veces", cuenta como 1 intento.
        "quiz" to mapOf("completado" to true)
    )

    @Test
    fun resumeActividadesEIntentos() {
        val r = Puntuacion.resumirActividades(progreso)
        assertEquals(4, r.realizadas)
        assertEquals(3, r.completadas)
        assertEquals(7, r.intentos)
        assertEquals(setOf("a1", "a2", "quiz"), r.idsCompletadas)
        assertEquals(42, r.precision) // 3 * 100 / 7
    }

    @Test
    fun sinProgresoTodoEnCero() {
        val r = Puntuacion.resumirActividades(null)
        assertEquals(0, r.completadas)
        assertEquals(0, r.precision)
    }

    @Test
    fun puntosSumanCursosActividadesXpYPrecision() {
        val r = Puntuacion.resumirActividades(progreso)
        val puntos = Puntuacion.puntos(
            cursosCompletados = 2,
            actividades = r,
            xpActividades = 60,
            rachaPalabra = 0,
            palabrasGanadas = 0,
            palabrasJugadas = 0,
            intentosPalabra = 0
        )
        // 2*100 + 3*50 + 60 + 42
        assertEquals(452, puntos)
    }

    @Test
    fun masIntentosParaLoMismoDaMenosPuntos() {
        val eficiente = Puntuacion.resumirActividades(mapOf("a" to mapOf("completado" to true, "veces" to 1L)))
        val conMasIntentos = Puntuacion.resumirActividades(mapOf("a" to mapOf("completado" to true, "veces" to 4L)))
        assertTrue(eficiente.precision > conMasIntentos.precision)
    }

    @Test
    fun nivelesCada200Xp() {
        assertEquals(Puntuacion.Nivel(1, 0, 200), Puntuacion.nivel(0))
        assertEquals(Puntuacion.Nivel(1, 199, 1), Puntuacion.nivel(199))
        assertEquals(Puntuacion.Nivel(2, 0, 200), Puntuacion.nivel(200))
        assertEquals(Puntuacion.Nivel(3, 50, 150), Puntuacion.nivel(450))
    }

    @Test
    fun porcentajeGeneralIncluyeCursosYActividades() {
        assertEquals(50, Puntuacion.porcentajeGeneral(completados = 3, total = 6))
        assertEquals(0, Puntuacion.porcentajeGeneral(completados = 0, total = 0))
        assertEquals(100, Puntuacion.porcentajeGeneral(completados = 9, total = 6))
    }
}
