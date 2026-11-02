package com.example.syslearning.juego

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import com.example.syslearning.R
import com.example.syslearning.juego.ReglasGamificacion.Componente

/**
 * Apariencia de SYS-01.
 *
 * 1. FORMA del cuerpo: evoluciona con el total de mejoras compradas
 *    (0 a 12 niveles):
 *      0–3  Cachorro    · cabeza grande, patas cortas
 *      4–7  Explorador  · cuerpo más largo, orejas en punta
 *      8–11 Guardián    · placas en el lomo, patas largas
 *      12   Omega       · metal violeta, alas de energía y melena dorada
 *
 * 2. CAPAS de cada componente, dibujadas para esa forma:
 *      Armadura: 1 peto · 2 casco con visor y placas · 3 armadura dorada con cresta
 *      Mandíbula: 1 emisor · 2 mandíbula con colmillos · 3 mandíbula dorada con cañón
 *      Patas: 1 rodilleras · 2 botas con propulsores · 3 botas doradas con turbo
 *      Núcleo: 1 núcleo brillante · 2 anillo y venas de energía · 3 plasma y aura
 *
 * Orden de las capas (de atrás hacia adelante):
 *   aura Omega · aura del núcleo · cuerpo · patas · armadura · mandíbula · núcleo
 */
object RobotVisual {

    enum class Forma(val numero: Int, val nombre: String, val descripcion: String) {
        CACHORRO(0, "Cachorro", "Compra 4 mejoras para que SYS-01 crezca"),
        EXPLORADOR(1, "Explorador", "Con 8 mejoras se convierte en Guardián"),
        GUARDIAN(2, "Guardián", "Lleva todo al nivel 3 para la forma Omega"),
        OMEGA(3, "Omega", "Forma final: todas las mejoras al máximo")
    }

    fun totalMejoras(niveles: Map<Componente, Int>): Int =
        Componente.values().sumOf { (niveles[it] ?: 0).coerceIn(0, Componente.NIVEL_MAXIMO) }

    fun forma(niveles: Map<Componente, Int>): Forma {
        val total = totalMejoras(niveles)
        val maximo = Componente.values().size * Componente.NIVEL_MAXIMO
        return when {
            total >= maximo -> Forma.OMEGA
            total >= 8 -> Forma.GUARDIAN
            total >= 4 -> Forma.EXPLORADOR
            else -> Forma.CACHORRO
        }
    }

    fun nombre(niveles: Map<Componente, Int>): String = when (forma(niveles)) {
        Forma.OMEGA -> "SYS-01 Omega"
        Forma.GUARDIAN -> "SYS-01 Guardián"
        Forma.EXPLORADOR -> "SYS-01 Explorador"
        Forma.CACHORRO -> "SYS-01"
    }

    /** Imagen compuesta de SYS-01 con su forma y todas sus mejoras. */
    fun imagen(context: Context, niveles: Map<Componente, Int>): Drawable {

        val f = forma(niveles).numero
        val nivel = { c: Componente -> (niveles[c] ?: 0).coerceIn(0, Componente.NIVEL_MAXIMO) }

        val nombres = mutableListOf<String>()

        if (f == 3) nombres.add("robot_omega_aura")
        if (nivel(Componente.NUCLEO) == 3) nombres.add("robot_f${f}_nucleo_aura")

        nombres.add("robot_forma_$f")

        listOf(Componente.PATAS, Componente.ARMADURA, Componente.MANDIBULA, Componente.NUCLEO)
            .forEach { componente ->
                val n = nivel(componente)
                if (n > 0) nombres.add("robot_f${f}_${componente.clave}_$n")
            }

        val capas = nombres.mapNotNull { nombre ->
            val id = context.resources.getIdentifier(nombre, "drawable", context.packageName)
            if (id == 0) null else ContextCompat.getDrawable(context, id)?.mutate()
        }

        if (capas.isEmpty()) {
            return ContextCompat.getDrawable(context, R.drawable.robot_sys01)!!
        }

        return LayerDrawable(capas.toTypedArray())
    }

    /** Qué cambia visualmente con cada nivel (se muestra al comprar). */
    fun cambioVisual(componente: Componente, nivel: Int): String = when (componente) {
        Componente.MANDIBULA -> when (nivel) {
            1 -> "Su hocico ahora tiene un emisor láser."
            2 -> "Mandíbula blindada con colmillos y láser rojo."
            else -> "¡Evolución! Mandíbula dorada con cañón láser sobre el hocico."
        }
        Componente.ARMADURA -> when (nivel) {
            1 -> "Peto y hombrera protectores."
            2 -> "Casco con visor y placas en la espalda."
            else -> "¡Evolución! Armadura dorada completa, casco con cresta y escudo."
        }
        Componente.PATAS -> when (nivel) {
            1 -> "Rodilleras de energía."
            2 -> "Botas con propulsores."
            else -> "¡Evolución! Botas doradas con turbo de fuego y estela de velocidad."
        }
        Componente.NUCLEO -> when (nivel) {
            1 -> "Núcleo brillante en el pecho."
            2 -> "Anillo de energía y venas encendidas por todo el cuerpo."
            else -> "¡Evolución! Núcleo de plasma, ojos de energía y aura alrededor de SYS-01."
        }
    }
}
