package com.example.syslearning.juego

import android.content.Context
import com.example.syslearning.juego.ReglasGamificacion.Componente
import com.example.syslearning.juego.ReglasGamificacion.Compra
import com.example.syslearning.juego.ReglasGamificacion.Enemigo
import com.example.syslearning.juego.ReglasGamificacion.EstadoRacha
import com.example.syslearning.juego.ReglasGamificacion.Material
import com.example.syslearning.juego.ReglasGamificacion.Recompensa
import com.example.syslearning.util.LecturaRapida
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Monedas, materiales, mejoras de SYS-01, enemigos y racha, guardados en
 * usuarios/{uid}.gamificacion para que se conserven al cerrar sesión o
 * cambiar de dispositivo:
 *
 *   gamificacion: {
 *     monedas: 120,
 *     materiales: { circuitos: 2, nucleos: 0, placas: 1, chips: 2 },
 *     robot: { mandibula: 1, armadura: 0, patas: 0, nucleo: 0 },
 *     enemigos: { basico: { victorias: 3, primeraVictoria: <fecha> } },
 *     repeticiones: { fecha: "2026-11-02", cantidad: 1 },
 *     racha: { actual, maxima, ultimoDia, inicioCiclo, hitosCobrados: [3] },
 *     pagosActividades: { <actividadId>: "dominio" | "participacion" }
 *   }
 *
 * Las monedas por actividades se escriben sin transacción (funciona sin
 * internet y se sincroniza después). Las compras y las recompensas de
 * combate usan transacciones, para que el saldo no quede inconsistente y
 * una victoria no se cobre dos veces; por eso necesitan conexión.
 */
object Gamificacion {

    private const val CAMPO = "gamificacion"

    data class EstadoJuego(
        val monedas: Int = 0,
        val materiales: Map<Material, Int> = emptyMap(),
        val niveles: Map<Componente, Int> = emptyMap(),
        val victorias: Map<String, Int> = emptyMap(),
        val repeticionesFecha: String? = null,
        val repeticionesCantidad: Int = 0,
        val racha: EstadoRacha = EstadoRacha(),
        val pagosActividades: Map<String, String> = emptyMap()
    ) {
        val derrotados: Set<String> get() = victorias.filterValues { it > 0 }.keys

        fun repeticionesHoy(): Int = if (repeticionesFecha == fechaHoy()) repeticionesCantidad else 0

        fun atributos() = ReglasGamificacion.atributos(niveles)
    }

    // ==================================================================
    // Fechas (día local del dispositivo)
    // ==================================================================

    fun fechaHoy(): String = formatear(Calendar.getInstance())

    fun fechaAyer(): String = formatear(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })

    private fun formatear(calendario: Calendar): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendario.time)

    // ==================================================================
    // Lectura
    // ==================================================================

    private fun referenciaUsuario() =
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseFirestore.getInstance().collection("usuarios").document(it)
        }

    private fun numero(valor: Any?): Int = (valor as? Number)?.toInt() ?: 0

    fun desdeDocumento(doc: DocumentSnapshot): EstadoJuego {

        val g = doc.get(CAMPO) as? Map<*, *> ?: return EstadoJuego()

        val materiales = (g["materiales"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
        val robot = (g["robot"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
        val enemigos = (g["enemigos"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
        val repeticiones = (g["repeticiones"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
        val racha = (g["racha"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()
        val pagos = (g["pagosActividades"] as? Map<*, *>) ?: emptyMap<Any?, Any?>()

        return EstadoJuego(
            monedas = numero(g["monedas"]),
            materiales = Material.values().associateWith { numero(materiales[it.clave]) },
            niveles = Componente.values().associateWith {
                numero(robot[it.clave]).coerceIn(0, Componente.NIVEL_MAXIMO)
            },
            victorias = enemigos.entries.associate { (clave, valor) ->
                clave.toString() to numero((valor as? Map<*, *>)?.get("victorias"))
            },
            repeticionesFecha = repeticiones["fecha"] as? String,
            repeticionesCantidad = numero(repeticiones["cantidad"]),
            racha = EstadoRacha(
                actual = numero(racha["actual"]),
                maxima = numero(racha["maxima"]),
                ultimoDia = racha["ultimoDia"] as? String,
                inicioCiclo = racha["inicioCiclo"] as? String,
                hitosCobrados = (racha["hitosCobrados"] as? List<*>)
                    ?.mapNotNull { (it as? Number)?.toInt() }?.toSet() ?: emptySet()
            ),
            pagosActividades = pagos.entries.associate { (k, v) -> k.toString() to v.toString() }
        )
    }

    /** Sin internet usa la copia del dispositivo, sin esperar. */
    fun leer(context: Context, onEstado: (EstadoJuego) -> Unit, onError: (Exception) -> Unit) {

        val ref = referenciaUsuario()

        if (ref == null) {
            onEstado(EstadoJuego())
            return
        }

        LecturaRapida.documento(
            context = context,
            referencia = ref,
            onDatos = { doc, _ -> onEstado(desdeDocumento(doc)) },
            onError = onError
        )
    }

    // ==================================================================
    // Monedas por actividades y racha
    // ==================================================================

    data class ResultadoActividad(
        val monedasActividad: Int,
        val motivoActividad: String,
        val racha: Int,
        val rachaAumento: Boolean,
        val monedasRacha: Int,
        val hitosNuevos: List<Int>,
        val monedasHitos: Int
    ) {
        val total get() = monedasActividad + monedasRacha + monedasHitos
    }

    /**
     * Llamar al terminar una micro actividad (paso 7). Es la única forma
     * de sumar a la racha: abrir la app no cuenta.
     */
    fun registrarActividad(
        context: Context,
        actividadId: String,
        dominada: Boolean,
        intentoAcierto: Int?,
        esReto: Boolean = false,
        onResultado: (ResultadoActividad) -> Unit
    ) {

        val ref = referenciaUsuario() ?: return

        LecturaRapida.documento(
            context = context,
            referencia = ref,
            onDatos = { doc, _ ->

                val estado = desdeDocumento(doc)

                val pago = ReglasGamificacion.pagoActividad(
                    estadoPrevio = estado.pagosActividades[actividadId],
                    dominada = dominada,
                    intentoAcierto = intentoAcierto,
                    esReto = esReto
                )

                val racha = ReglasGamificacion.registrarDiaDeRacha(estado.racha, fechaHoy(), fechaAyer())

                val total = pago.monedas + racha.monedasTotales

                val cambios = mutableMapOf<String, Any>(
                    "racha" to mapOf(
                        "actual" to racha.estado.actual,
                        "maxima" to racha.estado.maxima,
                        "ultimoDia" to (racha.estado.ultimoDia ?: fechaHoy()),
                        "inicioCiclo" to (racha.estado.inicioCiclo ?: fechaHoy()),
                        "hitosCobrados" to racha.estado.hitosCobrados.sorted()
                    )
                )

                if (pago.nuevoEstado != null) {
                    cambios["pagosActividades"] = mapOf(actividadId to pago.nuevoEstado)
                }

                if (total > 0) {
                    cambios["monedas"] = FieldValue.increment(total.toLong())
                }

                ref.set(mapOf(CAMPO to cambios), SetOptions.merge())

                onResultado(
                    ResultadoActividad(
                        monedasActividad = pago.monedas,
                        motivoActividad = pago.motivo,
                        racha = racha.estado.actual,
                        rachaAumento = racha.aumento,
                        monedasRacha = racha.monedasDiarias,
                        hitosNuevos = racha.hitosNuevos,
                        monedasHitos = racha.monedasHitos
                    )
                )
            },
            onError = { }
        )
    }

    // ==================================================================
    // La Palabra del Día
    // ==================================================================

    /**
     * Entrega monedas y XP por la palabra del día, una sola vez por día
     * (gamificacion.palabraCobrada guarda la fecha). El XP se suma a
     * usuarios/{uid}.xpTotal: cuenta para el nivel y el ranking.
     * Funciona sin conexión: se sincroniza después.
     */
    fun registrarPalabraDelDia(
        context: Context,
        gano: Boolean,
        intentos: Int,
        onResultado: (ReglasGamificacion.RecompensaPalabra) -> Unit
    ) {

        val ref = referenciaUsuario() ?: return
        val hoy = fechaHoy()

        LecturaRapida.documento(
            context = context,
            referencia = ref,
            onDatos = { doc, _ ->

                val g = doc.get(CAMPO) as? Map<*, *>
                val yaCobrada = g?.get("palabraCobrada") == hoy

                val recompensa = ReglasGamificacion.recompensaPalabraDelDia(gano, intentos, yaCobrada)

                if (recompensa.monedas > 0 || recompensa.xp > 0) {
                    ref.set(
                        mapOf(
                            CAMPO to mapOf(
                                "monedas" to FieldValue.increment(recompensa.monedas.toLong()),
                                "palabraCobrada" to hoy
                            ),
                            "xpTotal" to FieldValue.increment(recompensa.xp.toLong())
                        ),
                        SetOptions.merge()
                    )
                }

                onResultado(recompensa)
            },
            onError = { onResultado(ReglasGamificacion.RecompensaPalabra(0, 0, "")) }
        )
    }

    // ==================================================================
    // Compra de mejoras
    // ==================================================================

    sealed class ResultadoCompra {
        data class Exito(val componente: Componente, val nivel: Int, val estado: EstadoJuego) : ResultadoCompra()
        data class Error(val mensaje: String) : ResultadoCompra()
    }

    fun comprarMejora(context: Context, componente: Componente, onResultado: (ResultadoCompra) -> Unit) {

        val ref = referenciaUsuario()

        if (ref == null) {
            onResultado(ResultadoCompra.Error("Inicia sesión para mejorar a SYS-01."))
            return
        }

        if (!LecturaRapida.hayConexion(context)) {
            onResultado(ResultadoCompra.Error("Necesitas conexión a internet para comprar mejoras."))
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.runTransaction { transaccion ->

            val estado = desdeDocumento(transaccion.get(ref))
            val nivelActual = estado.niveles[componente] ?: 0

            when (val compra = ReglasGamificacion.evaluarCompra(
                componente, nivelActual, nivelActual + 1, estado.monedas, estado.materiales
            )) {
                is Compra.Rechazada -> ResultadoCompra.Error(compra.motivo)

                is Compra.Permitida -> {

                    val cambios = mutableMapOf<String, Any>(
                        "$CAMPO.monedas" to (estado.monedas - compra.costo.monedas),
                        "$CAMPO.robot.${componente.clave}" to compra.nivelNuevo
                    )

                    val materiales = estado.materiales.toMutableMap()
                    compra.costo.materiales.forEach { (material, cantidad) ->
                        val restante = (materiales[material] ?: 0) - cantidad
                        materiales[material] = restante
                        cambios["$CAMPO.materiales.${material.clave}"] = restante
                    }

                    transaccion.update(ref, cambios)

                    ResultadoCompra.Exito(
                        componente,
                        compra.nivelNuevo,
                        estado.copy(
                            monedas = estado.monedas - compra.costo.monedas,
                            materiales = materiales,
                            niveles = estado.niveles + (componente to compra.nivelNuevo)
                        )
                    )
                }
            }
        }
            .addOnSuccessListener { onResultado(it) }
            .addOnFailureListener {
                onResultado(ResultadoCompra.Error("No se pudo completar la compra. Revisa tu conexión."))
            }
    }

    // ==================================================================
    // Recompensas de combate
    // ==================================================================

    data class ResultadoVictoria(val recompensa: Recompensa, val estado: EstadoJuego)

    /**
     * Registra la victoria y entrega la recompensa en una transacción:
     * la primera victoria se paga completa una sola vez; las repetidas
     * tienen recompensa reducida y un límite diario.
     */
    fun reclamarVictoria(
        enemigo: Enemigo,
        onResultado: (ResultadoVictoria) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val ref = referenciaUsuario() ?: return
        val db = FirebaseFirestore.getInstance()
        val hoy = fechaHoy()

        db.runTransaction { transaccion ->

            val estado = desdeDocumento(transaccion.get(ref))
            val victoriasPrevias = estado.victorias[enemigo.clave] ?: 0
            val repeticionesHoy = estado.repeticionesHoy()

            val recompensa = ReglasGamificacion.recompensaVictoria(
                enemigo = enemigo,
                yaDerrotado = victoriasPrevias > 0,
                repeticionesPremiadasHoy = repeticionesHoy
            )

            val cambios = mutableMapOf<String, Any>(
                "$CAMPO.enemigos.${enemigo.clave}.victorias" to victoriasPrevias + 1,
                "$CAMPO.enemigos.${enemigo.clave}.ultimaVictoria" to Timestamp(Date())
            )

            if (recompensa.primeraVictoria) {
                cambios["$CAMPO.enemigos.${enemigo.clave}.primeraVictoria"] = Timestamp(Date())
            } else if (recompensa.monedas > 0 || recompensa.materiales.isNotEmpty()) {
                cambios["$CAMPO.repeticiones.fecha"] = hoy
                cambios["$CAMPO.repeticiones.cantidad"] = repeticionesHoy + 1
            }

            val monedas = estado.monedas + recompensa.monedas
            cambios["$CAMPO.monedas"] = monedas

            val materiales = estado.materiales.toMutableMap()
            recompensa.materiales.forEach { (material, cantidad) ->
                val total = (materiales[material] ?: 0) + cantidad
                materiales[material] = total
                cambios["$CAMPO.materiales.${material.clave}"] = total
            }

            transaccion.update(ref, cambios)

            ResultadoVictoria(
                recompensa,
                estado.copy(
                    monedas = monedas,
                    materiales = materiales,
                    victorias = estado.victorias + (enemigo.clave to victoriasPrevias + 1)
                )
            )
        }
            .addOnSuccessListener { onResultado(it) }
            .addOnFailureListener { onError(it) }
    }
}
