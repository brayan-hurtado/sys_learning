package com.example.syslearning.actividades

import com.example.syslearning.actividades.EvaluadorEjercicio.Respuesta
import com.example.syslearning.actividades.EvaluadorEjercicio.Semaforo
import com.example.syslearning.model.Ejercicio
import com.example.syslearning.model.TipoEjercicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la lógica de las micro actividades. Se ejecutan en Android
 * Studio con clic derecho sobre el archivo > Run 'EvaluadorEjercicioTest'.
 */
class EvaluadorEjercicioTest {

    private val seleccion = Ejercicio(
        tipo = TipoEjercicio.PREDECIR_RESULTADO.name,
        enunciado = "¿Cuántas veces se escribe Hola?",
        opciones = listOf("3", "4", "5"),
        respuestaCorrecta = 1,
        pista1 = "Cuenta los valores de i.",
        pista2 = "i va de 1 a 4.",
        explicacion = "Son 4 vueltas."
    )

    private val ordenar = Ejercicio(
        tipo = TipoEjercicio.ORDENAR_INSTRUCCIONES.name,
        enunciado = "Ordena",
        opciones = listOf("Leer base", "Leer altura", "area <- base * altura", "Escribir area")
    )

    @Test
    fun seleccionCorrectaEsCorrecta() {
        assertTrue(EvaluadorEjercicio.esCorrecta(seleccion, Respuesta.Seleccion(1)))
        assertFalse(EvaluadorEjercicio.esCorrecta(seleccion, Respuesta.Seleccion(0)))
    }

    @Test
    fun ordenSoloEsCorrectoEnLaSecuenciaOriginal() {
        assertTrue(EvaluadorEjercicio.esCorrecta(ordenar, Respuesta.Orden(listOf(0, 1, 2, 3))))
        assertFalse(EvaluadorEjercicio.esCorrecta(ordenar, Respuesta.Orden(listOf(1, 0, 2, 3))))
        assertFalse(EvaluadorEjercicio.esCorrecta(ordenar, Respuesta.Orden(listOf(0, 1, 2))))
    }

    @Test
    fun tiposNoSeMezclan() {
        // Una respuesta de orden no vale para un ejercicio de selección y viceversa.
        assertFalse(EvaluadorEjercicio.esCorrecta(seleccion, Respuesta.Orden(listOf(0, 1, 2))))
        assertFalse(EvaluadorEjercicio.esCorrecta(ordenar, Respuesta.Seleccion(0)))
    }

    @Test
    fun retroalimentacionEnTresNiveles() {

        val primero = EvaluadorEjercicio.retroalimentar(seleccion, Respuesta.Seleccion(0), 1)
        assertFalse(primero.terminado)
        assertEquals("Cuenta los valores de i.", primero.mensaje)

        val segundo = EvaluadorEjercicio.retroalimentar(seleccion, Respuesta.Seleccion(0), 2)
        assertFalse(segundo.terminado)
        assertEquals("i va de 1 a 4.", segundo.mensaje)

        val tercero = EvaluadorEjercicio.retroalimentar(seleccion, Respuesta.Seleccion(0), 3)
        assertTrue(tercero.terminado)
        assertTrue(tercero.mostroSolucion)
        assertTrue(tercero.mensaje.contains("La respuesta correcta es: 4"))
    }

    @Test
    fun sinPistaUsaUnaPistaGenericaQueNoDaLaRespuesta() {

        val retro = EvaluadorEjercicio.retroalimentar(ordenar, Respuesta.Orden(listOf(0, 2, 1, 3)), 1)

        assertEquals("Revisa el paso 2: ¿qué debe ocurrir antes de esa instrucción?", retro.mensaje)
        assertFalse(retro.mostroSolucion)
    }

    @Test
    fun primeraPosicionIncorrecta() {
        assertEquals(3, EvaluadorEjercicio.primeraPosicionIncorrecta(listOf(0, 1, 3, 2)))
        assertNull(EvaluadorEjercicio.primeraPosicionIncorrecta(listOf(0, 1, 2)))
    }

    @Test
    fun xpDominioYSemaforoSiguenLaFicha() {

        assertEquals(25, EvaluadorEjercicio.xpTotal(1))
        assertEquals(15, EvaluadorEjercicio.xpTotal(2))
        assertEquals(5, EvaluadorEjercicio.xpTotal(null))

        assertTrue(EvaluadorEjercicio.dominada(1))
        assertTrue(EvaluadorEjercicio.dominada(2))
        assertFalse(EvaluadorEjercicio.dominada(3))
        assertFalse(EvaluadorEjercicio.dominada(null))

        assertEquals(Semaforo.VERDE, EvaluadorEjercicio.semaforo(1))
        assertEquals(Semaforo.AMARILLO, EvaluadorEjercicio.semaforo(2))
        assertEquals(Semaforo.ROJO, EvaluadorEjercicio.semaforo(null))
    }

    @Test
    fun validacionAlGuardar() {

        assertNull(EvaluadorEjercicio.validarParaGuardar(seleccion))
        assertNotNull(EvaluadorEjercicio.validarParaGuardar(seleccion.copy(respuestaCorrecta = 5)))
        assertNotNull(EvaluadorEjercicio.validarParaGuardar(seleccion.copy(enunciado = " ")))
        assertNotNull(EvaluadorEjercicio.validarParaGuardar(ordenar.copy(opciones = listOf("a", "b"))))
        assertNull(EvaluadorEjercicio.validarParaGuardar(ordenar))
    }

    @Test
    fun tipoDesconocidoNoRompe() {
        assertEquals(TipoEjercicio.CUESTIONARIO, Ejercicio(tipo = "OTRO").tipoEjercicio())
    }
}
