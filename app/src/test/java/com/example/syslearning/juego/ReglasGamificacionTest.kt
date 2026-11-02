package com.example.syslearning.juego

import com.example.syslearning.juego.ReglasGamificacion.Componente
import com.example.syslearning.juego.ReglasGamificacion.Compra
import com.example.syslearning.juego.ReglasGamificacion.Enemigo
import com.example.syslearning.juego.ReglasGamificacion.EstadoRacha
import com.example.syslearning.juego.ReglasGamificacion.Material
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReglasGamificacionTest {

    private val r = ReglasGamificacion

    // ---------------- Monedas por actividad ----------------

    @Test
    fun menosIntentosDanMasMonedas() {
        assertEquals(50, r.pagoActividad(null, dominada = true, intentoAcierto = 1).monedas)
        assertEquals(35, r.pagoActividad(null, dominada = true, intentoAcierto = 2).monedas)
        assertEquals(10, r.pagoActividad(null, dominada = false, intentoAcierto = null).monedas)
    }

    @Test
    fun elRetoFinalPagaElDoble() {
        assertEquals(100, r.pagoActividad(null, dominada = true, intentoAcierto = 1, esReto = true).monedas)
        assertEquals(70, r.pagoActividad(null, dominada = true, intentoAcierto = 2, esReto = true).monedas)
        assertEquals(10, r.pagoActividad(null, dominada = false, intentoAcierto = null, esReto = true).monedas)
    }

    @Test
    fun repetirUnaActividadDominadaNoDaMonedas() {
        val pago = r.pagoActividad(r.PAGO_DOMINIO, dominada = true, intentoAcierto = 1)
        assertEquals(0, pago.monedas)
        assertEquals(r.PAGO_DOMINIO, pago.nuevoEstado)
    }

    @Test
    fun participacionSePagaUnaVezYLuegoSePuedeDominar() {
        assertEquals(0, r.pagoActividad(r.PAGO_PARTICIPACION, dominada = false, intentoAcierto = null).monedas)
        assertEquals(35, r.pagoActividad(r.PAGO_PARTICIPACION, dominada = true, intentoAcierto = 2).monedas)
    }

    // ---------------- La Palabra del Día ----------------

    @Test
    fun palabraDelDiaDa50MonedasYXpSegunIntentos() {
        val rapida = r.recompensaPalabraDelDia(gano = true, intentos = 2, yaCobradaHoy = false)
        assertEquals(50, rapida.monedas)
        assertEquals(30, rapida.xp)
        assertEquals(20, r.recompensaPalabraDelDia(true, 4, false).xp)
        assertEquals(10, r.recompensaPalabraDelDia(true, 6, false).xp)
    }

    @Test
    fun palabraDelDiaNoPagaSiPierdeOSiYaCobro() {
        assertEquals(0, r.recompensaPalabraDelDia(gano = false, intentos = 6, yaCobradaHoy = false).monedas)
        val repetida = r.recompensaPalabraDelDia(gano = true, intentos = 1, yaCobradaHoy = true)
        assertEquals(0, repetida.monedas)
        assertEquals(0, repetida.xp)
    }

    // ---------------- Racha ----------------

    @Test
    fun primerDiaEmpiezaEnUnoYDaMonedas() {
        val res = r.registrarDiaDeRacha(EstadoRacha(), hoy = "2026-11-02", ayer = "2026-11-01")
        assertEquals(1, res.estado.actual)
        assertTrue(res.aumento)
        assertEquals(10, res.monedasDiarias)
    }

    @Test
    fun segundaActividadDelMismoDiaNoAumenta() {
        val dia1 = r.registrarDiaDeRacha(EstadoRacha(), "2026-11-02", "2026-11-01").estado
        val otra = r.registrarDiaDeRacha(dia1, "2026-11-02", "2026-11-01")
        assertFalse(otra.aumento)
        assertEquals(1, otra.estado.actual)
        assertEquals(0, otra.monedasTotales)
    }

    @Test
    fun diaConsecutivoSubeYUnDiaPerdidoReinicia() {
        val dia1 = r.registrarDiaDeRacha(EstadoRacha(), "2026-11-02", "2026-11-01").estado
        val dia2 = r.registrarDiaDeRacha(dia1, "2026-11-03", "2026-11-02")
        assertEquals(2, dia2.estado.actual)
        assertEquals(15, dia2.monedasDiarias)

        // Se salta el 4: el 5 empieza un ciclo nuevo.
        val dia5 = r.registrarDiaDeRacha(dia2.estado, "2026-11-05", "2026-11-04")
        assertEquals(1, dia5.estado.actual)
        assertEquals("2026-11-05", dia5.estado.inicioCiclo)
        assertEquals(2, dia5.estado.maxima)
    }

    @Test
    fun hitoSeCobraUnaVezPorCiclo() {
        // Día 3 de la racha: cobra el hito de 3 días.
        val dia2 = EstadoRacha(actual = 2, maxima = 2, ultimoDia = "2026-11-02", inicioCiclo = "2026-11-01")
        val dia3 = r.registrarDiaDeRacha(dia2, "2026-11-03", "2026-11-02")
        assertEquals(listOf(3), dia3.hitosNuevos)
        assertEquals(50, dia3.monedasHitos)

        // Otra actividad el mismo día: no se vuelve a cobrar.
        assertEquals(0, r.registrarDiaDeRacha(dia3.estado, "2026-11-03", "2026-11-02").monedasHitos)

        // Día 4: el hito de 3 ya está cobrado en este ciclo.
        val dia4 = r.registrarDiaDeRacha(dia3.estado, "2026-11-04", "2026-11-03")
        assertTrue(dia4.hitosNuevos.isEmpty())

        // Ciclo nuevo tras perder la racha: el hito de 3 vuelve a estar disponible.
        var estado = r.registrarDiaDeRacha(dia4.estado, "2026-11-10", "2026-11-09").estado
        estado = r.registrarDiaDeRacha(estado, "2026-11-11", "2026-11-10").estado
        val otraVez = r.registrarDiaDeRacha(estado, "2026-11-12", "2026-11-11")
        assertEquals(listOf(3), otraVez.hitosNuevos)
    }

    @Test
    fun monedasDiariasCrecenHastaUnTope() {
        assertEquals(10, r.monedasPorDiaDeRacha(1))
        assertEquals(25, r.monedasPorDiaDeRacha(4))
        assertEquals(40, r.monedasPorDiaDeRacha(30))
    }

    @Test
    fun rachaVigenteSePierdeSiPasoUnDia() {
        val estado = EstadoRacha(actual = 5, ultimoDia = "2026-11-01")
        assertEquals(5, r.rachaVigente(estado, hoy = "2026-11-02", ayer = "2026-11-01"))
        assertEquals(0, r.rachaVigente(estado, hoy = "2026-11-03", ayer = "2026-11-02"))
    }

    // ---------------- Mejoras ----------------

    @Test
    fun atributosInicialesYMejorados() {
        val base = r.atributos(emptyMap())
        assertEquals(100, base.vida)
        assertEquals(10, base.ataque)
        assertEquals(5, base.defensa)
        assertEquals(50, base.energia)
        assertFalse(base.tieneHabilidadEspecial)

        val mejorado = r.atributos(mapOf(Componente.MANDIBULA to 2, Componente.ARMADURA to 1, Componente.NUCLEO to 1))
        assertEquals(20, mejorado.ataque)
        assertEquals(9, mejorado.defensa)
        assertEquals(120, mejorado.vida)
        assertEquals(75, mejorado.energia)
        assertTrue(mejorado.tieneHabilidadEspecial)
    }

    @Test
    fun compraEnOrdenConSaldo() {
        val compra = r.evaluarCompra(Componente.MANDIBULA, 0, 1, monedas = 120, materiales = emptyMap())
        assertTrue(compra is Compra.Permitida)
        assertEquals(100, (compra as Compra.Permitida).costo.monedas)
    }

    @Test
    fun noSeSaltaNivelesNiSeRepiten() {
        assertTrue(r.evaluarCompra(Componente.MANDIBULA, 0, 2, 9999, emptyMap()) is Compra.Rechazada)
        assertTrue(r.evaluarCompra(Componente.MANDIBULA, 1, 1, 9999, emptyMap()) is Compra.Rechazada)
        assertTrue(r.evaluarCompra(Componente.MANDIBULA, 3, 4, 9999, emptyMap()) is Compra.Rechazada)
    }

    @Test
    fun sinSaldoOSinMaterialesNoSeCompra() {
        assertTrue(r.evaluarCompra(Componente.ARMADURA, 0, 1, 99, emptyMap()) is Compra.Rechazada)
        assertTrue(r.evaluarCompra(Componente.ARMADURA, 2, 3, 600, mapOf(Material.PLACAS to 2)) is Compra.Rechazada)
        assertTrue(r.evaluarCompra(Componente.ARMADURA, 2, 3, 600, mapOf(Material.PLACAS to 3)) is Compra.Permitida)
    }

    // ---------------- Enemigos ----------------

    @Test
    fun enemigosSeDesbloqueanEnOrden() {
        assertTrue(r.desbloqueado(Enemigo.BASICO, emptySet()))
        assertFalse(r.desbloqueado(Enemigo.CORRUPTOR, emptySet()))
        assertTrue(r.desbloqueado(Enemigo.CORRUPTOR, setOf("basico")))
        assertFalse(r.desbloqueado(Enemigo.DESTRUCTOR, setOf("basico")))
    }

    @Test
    fun primeraVictoriaCompletaYRepeticionesLimitadas() {
        val primera = r.recompensaVictoria(Enemigo.CORRUPTOR, yaDerrotado = false, repeticionesPremiadasHoy = 0)
        assertEquals(70, primera.monedas)
        assertEquals(mapOf(Material.CHIPS to 2, Material.PLACAS to 1), primera.materiales)

        val repetida = r.recompensaVictoria(Enemigo.CORRUPTOR, true, 0, Random(1))
        assertEquals(7, repetida.monedas)
        assertEquals(1, repetida.materiales.values.sum())

        val agotada = r.recompensaVictoria(Enemigo.CORRUPTOR, true, r.REPETICIONES_PREMIADAS_POR_DIA)
        assertEquals(0, agotada.monedas)
        assertTrue(agotada.materiales.isEmpty())
    }

    @Test
    fun soloElJefeDaNucleosEnLaPrimeraVictoria() {
        assertTrue(Enemigo.DESTRUCTOR.esJefe)
        assertEquals(1, Enemigo.DESTRUCTOR.materialesPrimeraVictoria[Material.NUCLEOS])
        assertEquals(null, Enemigo.BASICO.materialesPrimeraVictoria[Material.NUCLEOS])
    }

    // ---------------- Revanchas ----------------

    @Test
    fun primerCombateEsNivel1ConStatsBase() {
        val stats = r.statsVirus(Enemigo.BASICO, victorias = 0)
        assertEquals(1, stats.nivel)
        assertEquals(50, stats.vida)
        assertEquals(5, stats.ataque)
    }

    @Test
    fun cadaVictoriaFortaleceAlVirus() {
        val nivel2 = r.statsVirus(Enemigo.BASICO, victorias = 1)
        assertEquals(2, nivel2.nivel)
        assertEquals(60, nivel2.vida)   // +20 %
        assertEquals(6, nivel2.ataque)  // +15 % redondeado hacia arriba

        val corruptor3 = r.statsVirus(Enemigo.CORRUPTOR, victorias = 2)
        assertEquals(140, corruptor3.vida) // 100 + 40 %
        assertEquals(13, corruptor3.ataque) // 10 + 3

        var anterior = r.statsVirus(Enemigo.DESTRUCTOR, 0)
        for (v in 1..5) {
            val actual = r.statsVirus(Enemigo.DESTRUCTOR, v)
            assertTrue(actual.vida > anterior.vida)
            assertTrue(actual.ataque > anterior.ataque)
            anterior = actual
        }
    }

    @Test
    fun elNivelDelVirusTieneTope() {
        val tope = r.statsVirus(Enemigo.BASICO, victorias = 50)
        assertEquals(r.NIVEL_MAXIMO_VIRUS, tope.nivel)
        assertEquals(r.statsVirus(Enemigo.BASICO, r.NIVEL_MAXIMO_VIRUS - 1), tope)
        assertEquals(140, tope.vida) // 50 + 180 %
    }

    // ---------------- Combate ----------------

    @Test
    fun danoDependeDeLosAtributos() {
        val normal = r.danoRobot(10, pulsoEmp = false, aleatorio = Random(3))
        assertTrue(normal in 10..14)
        assertEquals(normal * 2, r.danoRobot(10, pulsoEmp = true, aleatorio = Random(3)))

        // Velocidad 0: nunca esquiva. Defensa alta: mínimo 1 de daño.
        assertTrue(r.danoEnemigo(5, defensa = 40, velocidad = 0, respuestaIncorrecta = false, aleatorio = Random(5)) >= 1)
        // Velocidad 100: siempre esquiva.
        assertEquals(0, r.danoEnemigo(20, 5, velocidad = 100, respuestaIncorrecta = true))
    }
}
