package com.example.syslearning.actividades

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class EstadisticasActividades(
    val totalSesiones: Int,
    val totalCompletadas: Int,
    val rachaActual: Int,
    val rachaMaxima: Int
)

/**
 * Racha diaria + contador de sesiones para las actividades tipo quiz.
 * Mismo patrón que PalabraDelDiaManager: guarda localmente (por si no
 * hay internet) y sincroniza a Firestore para que Estadísticas/Progreso
 * puedan usarlo.
 */
object SesionActividadesManager {

    private const val PREFS = "racha_actividades"

    // % de aciertos necesario para que la sesión cuente como "aprobada"
    // (afecta racha, insignias, y si la actividad se marca completada).
    private const val UMBRAL_APROBACION = 0.6

    private fun uidActual() =
        FirebaseAuth.getInstance().currentUser?.uid ?: "sin_sesion"

    private fun prefs(context: Context) =
        context.getSharedPreferences("${PREFS}_${uidActual()}", Context.MODE_PRIVATE)

    private fun fechaHoy(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formato.format(Date())
    }

    private fun fechaAyer(): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, -1)
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formato.format(calendario.time)
    }

    /**
     * Llamar al terminar cualquier sesión de 10 preguntas (se haya
     * aprobado o no). Devuelve la racha actual, útil para el diálogo
     * de resultados.
     */
    fun registrarSesion(
        context: Context,
        actividadId: String,
        actividadTitulo: String,
        aciertos: Int,
        totalPreguntas: Int,
        xpGanado: Int
    ): Int {

        val prefsLocal = prefs(context)
        val hoy = fechaHoy()
        val ayer = fechaAyer()

        val aprobo = totalPreguntas > 0 &&
                (aciertos.toDouble() / totalPreguntas) >= UMBRAL_APROBACION

        val totalSesiones = prefsLocal.getInt("totalSesiones", 0) + 1
        var totalCompletadas = prefsLocal.getInt("totalCompletadas", 0)
        var rachaActual = prefsLocal.getInt("rachaActual", 0)
        var rachaMaxima = prefsLocal.getInt("rachaMaxima", 0)

        if (aprobo) {

            totalCompletadas += 1

            val ultimoDiaAprobado = prefsLocal.getString("ultimoDiaAprobado", null)

            rachaActual = when (ultimoDiaAprobado) {
                hoy -> rachaActual          // ya se había aprobado una sesión hoy
                ayer -> rachaActual + 1     // día consecutivo
                else -> 1                   // primera vez, o se rompió la racha
            }

            rachaMaxima = maxOf(rachaMaxima, rachaActual)

            prefsLocal.edit()
                .putString("ultimoDiaAprobado", hoy)
                .apply()
        }

        prefsLocal.edit()
            .putInt("totalSesiones", totalSesiones)
            .putInt("totalCompletadas", totalCompletadas)
            .putInt("rachaActual", rachaActual)
            .putInt("rachaMaxima", rachaMaxima)
            .apply()

        sincronizarFirestore(
            actividadId = actividadId,
            actividadTitulo = actividadTitulo,
            aprobo = aprobo,
            xpGanado = xpGanado,
            racha = rachaActual,
            rachaMaxima = rachaMaxima,
            totalSesiones = totalSesiones,
            totalCompletadas = totalCompletadas
        )

        return rachaActual
    }

    private fun sincronizarFirestore(
        actividadId: String,
        actividadTitulo: String,
        aprobo: Boolean,
        xpGanado: Int,
        racha: Int,
        rachaMaxima: Int,
        totalSesiones: Int,
        totalCompletadas: Int
    ) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val datos = mutableMapOf<String, Any>(
            "actividadesStats" to mapOf(
                "racha" to racha,
                "rachaMaxima" to rachaMaxima,
                "totalSesiones" to totalSesiones,
                "totalCompletadas" to totalCompletadas
            )
        )

        if (aprobo) {
            // Mapa anidado: con SetOptions.merge() esto solo actualiza
            // la entrada de ESTA actividad, sin borrar las demás que ya
            // existan dentro de "actividadesProgreso".
            datos["actividadesProgreso"] = mapOf(
                actividadId to mapOf(
                    "titulo" to actividadTitulo,
                    "completado" to true,
                    "xp" to xpGanado,
                    "fecha" to Timestamp.now()
                )
            )
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .set(datos, SetOptions.merge())
    }

    // ==================================================================
    // Micro actividades (7 pasos)
    // ==================================================================

    /**
     * Llamar al terminar una micro actividad (paso 7, recompensa y
     * registro). La racha sube con cualquier micro actividad terminada
     * en el día; "totalCompletadas" y el estado COMPLETADA solo cuentan
     * si la actividad quedó dominada (comprobación en máx. 2 intentos).
     *
     * Devuelve la racha actual.
     */
    fun registrarMicroActividad(
        context: Context,
        actividadId: String,
        actividadTitulo: String,
        moduloId: Int,
        dominada: Boolean,
        xpGanado: Int,
        semaforo: String,
        intentosComprobacion: Int
    ): Int {

        val prefsLocal = prefs(context)
        val hoy = fechaHoy()
        val ayer = fechaAyer()

        val totalSesiones = prefsLocal.getInt("totalSesiones", 0) + 1
        var totalCompletadas = prefsLocal.getInt("totalCompletadas", 0)
        var rachaActual = prefsLocal.getInt("rachaActual", 0)
        var rachaMaxima = prefsLocal.getInt("rachaMaxima", 0)

        if (dominada) totalCompletadas += 1

        val ultimoDia = prefsLocal.getString("ultimoDiaAprobado", null)

        rachaActual = when (ultimoDia) {
            hoy -> maxOf(rachaActual, 1)
            ayer -> rachaActual + 1
            else -> 1
        }

        rachaMaxima = maxOf(rachaMaxima, rachaActual)

        prefsLocal.edit()
            .putString("ultimoDiaAprobado", hoy)
            .putInt("totalSesiones", totalSesiones)
            .putInt("totalCompletadas", totalCompletadas)
            .putInt("rachaActual", rachaActual)
            .putInt("rachaMaxima", rachaMaxima)
            .apply()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return rachaActual

        // Entrada de esta actividad dentro de "actividadesProgreso".
        // "completado" solo se escribe en true: con merge, un intento
        // posterior que no se domine no borra una actividad ya completada.
        val entrada = mutableMapOf<String, Any>(
            "titulo" to actividadTitulo,
            "moduloId" to moduloId,
            "ultimoSemaforo" to semaforo,
            "intentosComprobacion" to intentosComprobacion,
            "veces" to FieldValue.increment(1L),
            "xp" to FieldValue.increment(xpGanado.toLong()),
            "fecha" to Timestamp.now()
        )

        if (dominada) {
            entrada["completado"] = true
        } else {
            entrada["porReforzar"] = true
        }

        val datos = mapOf(
            "xpTotal" to FieldValue.increment(xpGanado.toLong()),
            "actividadesStats" to mapOf(
                "racha" to rachaActual,
                "rachaMaxima" to rachaMaxima,
                "totalSesiones" to totalSesiones,
                "totalCompletadas" to totalCompletadas
            ),
            "actividadesProgreso" to mapOf(actividadId to entrada)
        )

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .set(datos, SetOptions.merge())

        return rachaActual
    }

    fun obtenerEstadisticas(context: Context): EstadisticasActividades {

        val prefsLocal = prefs(context)

        return EstadisticasActividades(
            totalSesiones = prefsLocal.getInt("totalSesiones", 0),
            totalCompletadas = prefsLocal.getInt("totalCompletadas", 0),
            rachaActual = prefsLocal.getInt("rachaActual", 0),
            rachaMaxima = prefsLocal.getInt("rachaMaxima", 0)
        )
    }
}