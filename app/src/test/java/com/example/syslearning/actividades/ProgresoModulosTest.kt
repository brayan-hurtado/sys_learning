package com.example.syslearning.actividades

import com.example.syslearning.model.Actividad
import com.example.syslearning.model.EstadoActividad
import com.example.syslearning.model.Modulo
import com.example.syslearning.model.NivelActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgresoModulosTest {

    private val modulos = listOf(
        Modulo("m1", 1, "Introducción"),
        Modulo("m2", 2, "Estructuras de control"),
        Modulo("m3", 3, "Funciones")
    )

    private fun act(id: String, modulo: Int, orden: Int, reto: Boolean = false) = Actividad(
        id = id, titulo = id, modulo = modulo, orden = orden,
        nivel = if (reto) NivelActividad.INTEGRACION.name else NivelActividad.RECONOCIMIENTO.name
    )

    private fun actividades() = listOf(
        act("a1", 1, 1), act("a2", 1, 2), act("r1", 1, 3, reto = true),
        act("b1", 2, 1), act("r2", 2, 2, reto = true)
    )

    @Test
    fun alInicioSoloLaPrimeraActividadDelModulo1EstaDisponible() {
        val estado = ProgresoModulos.calcular(modulos, actividades(), emptySet())
        assertEquals(2, estado.size) // el módulo 3 no tiene actividades: no se muestra
        val m1 = estado[0]
        assertTrue(m1.desbloqueado)
        assertEquals(EstadoActividad.DISPONIBLE, m1.actividades[0].estado)
        assertEquals(EstadoActividad.BLOQUEADA, m1.actividades[1].estado)
        assertEquals(EstadoActividad.BLOQUEADA, m1.reto!!.estado)
        assertFalse(estado[1].desbloqueado)
        assertEquals(EstadoActividad.BLOQUEADA, estado[1].actividades[0].estado)
    }

    @Test
    fun elRetoSeAbreAlCompletarLasDemas() {
        val estado = ProgresoModulos.calcular(modulos, actividades(), setOf("a1", "a2"))
        val m1 = estado[0]
        assertEquals(EstadoActividad.DISPONIBLE, m1.reto!!.estado)
        assertFalse(m1.completado)
        assertEquals(66, m1.porcentaje) // 2 de 3
        assertFalse(estado[1].desbloqueado)
    }

    @Test
    fun superarElRetoCompletaElModuloYAbreElSiguiente() {
        val estado = ProgresoModulos.calcular(modulos, actividades(), setOf("a1", "a2", "r1"))
        assertTrue(estado[0].completado)
        assertEquals(100, estado[0].porcentaje)
        assertTrue(estado[1].desbloqueado)
        assertEquals(EstadoActividad.DISPONIBLE, estado[1].actividades[0].estado)
    }

    @Test
    fun moduloSinRetoSeCompletaConTodasSusActividades() {
        val lista = listOf(act("a1", 1, 1), act("a2", 1, 2), act("b1", 2, 1))
        val estado = ProgresoModulos.calcular(modulos, lista, setOf("a1", "a2"))
        assertNull(estado[0].reto)
        assertTrue(estado[0].completado)
        assertTrue(estado[1].desbloqueado)
    }

    @Test
    fun elRetoSiempreVaAlFinalAunqueTengaMenorOrden() {
        val lista = listOf(act("r1", 1, 0, reto = true), act("a1", 1, 1))
        val m1 = ProgresoModulos.calcular(modulos, lista, emptySet())[0]
        assertEquals("r1", m1.reto!!.id)
        assertEquals(listOf("a1", "r1"), m1.todas.map { it.id })
    }

    @Test
    fun modulosSugeridosSeReemplazanPorLosDeFirestore() {
        val propios = listOf(Modulo("x", 2, "Mi módulo 2"), Modulo("y", 4, "Oculto", activo = false))
        val combinados = Modulo.combinar(propios)
        assertEquals(listOf(1, 2, 3, 5), combinados.map { it.numero })
        assertEquals("Mi módulo 2", combinados[1].titulo)
    }
}
