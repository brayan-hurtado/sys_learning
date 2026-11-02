package com.example.syslearning.actividades

import com.example.syslearning.model.Actividad
import com.example.syslearning.model.EstadoActividad
import com.example.syslearning.model.Modulo
import com.example.syslearning.model.NivelActividad

/**
 * Reglas de avance por módulos (RF 12, progresión de la dificultad).
 * No depende de Android: se prueba con JUnit (ProgresoModulosTest).
 *
 *  - Dentro de un módulo, las actividades se hacen en orden: la primera
 *    sin completar está DISPONIBLE y las siguientes BLOQUEADAS.
 *  - El reto final (actividad de nivel INTEGRACION) se desbloquea cuando
 *    se completan todas las demás actividades del módulo.
 *  - Un módulo queda completado al superar su reto final (o, si no tiene
 *    reto, al completar todas sus actividades).
 *  - El módulo 1 siempre está abierto; cada módulo siguiente se abre al
 *    completar el anterior. Los módulos sin actividades no se muestran.
 */
object ProgresoModulos {

    data class EstadoModulo(
        val modulo: Modulo,
        /** Actividades normales, en orden, con su estado ya calculado. */
        val actividades: List<Actividad>,
        /** Reto final del módulo (null si no tiene). */
        val reto: Actividad?,
        val desbloqueado: Boolean,
        val completado: Boolean
    ) {
        val total: Int get() = actividades.size + (if (reto != null) 1 else 0)

        val completadas: Int
            get() = actividades.count { it.estado == EstadoActividad.COMPLETADA } +
                    (if (reto?.estado == EstadoActividad.COMPLETADA) 1 else 0)

        val porcentaje: Int get() = if (total > 0) completadas * 100 / total else 0

        /** Todas las actividades del módulo, con el reto al final. */
        val todas: List<Actividad> get() = actividades + listOfNotNull(reto)
    }

    fun esReto(actividad: Actividad): Boolean =
        NivelActividad.desde(actividad.nivel) == NivelActividad.INTEGRACION

    /**
     * @param actividades actividades visibles para el estudiante (ya filtradas por aula).
     * @param idsCompletadas ids de las actividades que ya dominó.
     */
    fun calcular(
        modulos: List<Modulo>,
        actividades: List<Actividad>,
        idsCompletadas: Set<String>
    ): List<EstadoModulo> {

        val porModulo = actividades.groupBy { it.modulo }
        val resultado = mutableListOf<EstadoModulo>()
        var anteriorCompletado = true

        modulos.sortedBy { it.numero }.forEach { modulo ->

            val delModulo = porModulo[modulo.numero].orEmpty().sortedBy { it.orden }
            if (delModulo.isEmpty()) return@forEach

            // Si hay varios retos, el último en orden es el reto final;
            // los demás se tratan como actividades normales.
            val reto = delModulo.lastOrNull { esReto(it) }
            val normales = delModulo.filter { it !== reto }

            val desbloqueado = anteriorCompletado

            var yaHayDisponible = false
            normales.forEach { actividad ->
                actividad.estado = when {
                    actividad.id in idsCompletadas -> EstadoActividad.COMPLETADA
                    desbloqueado && !yaHayDisponible -> {
                        yaHayDisponible = true
                        EstadoActividad.DISPONIBLE
                    }
                    else -> EstadoActividad.BLOQUEADA
                }
            }

            val normalesCompletas = normales.all { it.estado == EstadoActividad.COMPLETADA }

            if (reto != null) {
                reto.estado = when {
                    reto.id in idsCompletadas -> EstadoActividad.COMPLETADA
                    desbloqueado && normalesCompletas -> EstadoActividad.DISPONIBLE
                    else -> EstadoActividad.BLOQUEADA
                }
            }

            val completado = if (reto != null) {
                reto.estado == EstadoActividad.COMPLETADA
            } else {
                normalesCompletas
            }

            resultado.add(EstadoModulo(modulo, normales, reto, desbloqueado, completado))

            anteriorCompletado = completado
        }

        return resultado
    }
}
