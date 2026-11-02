package com.example.syslearning.game

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class EstadoJuego(
    val fecha: String,
    val palabra: String,
    val intentos: MutableList<String>,
    var terminado: Boolean,
    var gano: Boolean
)

data class EstadisticasJuego(
    val totalJugados: Int,
    val totalGanados: Int,
    val rachaActual: Int,
    val rachaMaxima: Int
)

object PalabraDelDiaManager {

    const val GRIS = 0
    const val AMARILLO = 1
    const val VERDE = 2

    private const val PREFS_JUEGO = "wordle_juego"
    private const val PREFS_STATS = "wordle_stats"
    private const val CLAVE_FECHA_TERMINADA = "fechaTerminada"

    private fun obtenerUidActual(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "sin_sesion"
    }

    private fun preferenciasJuego(context: Context) =
        context.getSharedPreferences(
            "${PREFS_JUEGO}_${obtenerUidActual()}",
            Context.MODE_PRIVATE
        )

    private fun preferenciasEstadisticas(context: Context) =
        context.getSharedPreferences(
            "${PREFS_STATS}_${obtenerUidActual()}",
            Context.MODE_PRIVATE
        )

    fun obtenerFechaHoy(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formato.format(Date())
    }

    private fun obtenerFechaAyer(): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, -1)

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formato.format(calendario.time)
    }

    /*
     * Si la última partida no fue hoy ni ayer, el usuario perdió su racha.
     */
    private fun actualizarRachaVencida(context: Context) {
        val prefs = preferenciasEstadisticas(context)
        val ultimaFechaJugada = prefs.getString("ultimaFechaJugada", null) ?: return

        val hoy = obtenerFechaHoy()
        val ayer = obtenerFechaAyer()

        // La racha sigue vigente si jugó hoy o ayer.
        if (ultimaFechaJugada == hoy || ultimaFechaJugada == ayer) {
            return
        }

        val rachaMaxima = prefs.getInt("rachaMaxima", 0)
        val totalJugados = prefs.getInt("totalJugados", 0)
        val totalGanados = prefs.getInt("totalGanados", 0)
        val totalIntentos = prefs.getInt("totalIntentos", 0)

        prefs.edit()
            .putInt("rachaActual", 0)
            .apply()

        sincronizarEstadisticas(
            racha = 0,
            rachaMaxima = rachaMaxima,
            totalJugados = totalJugados,
            totalGanados = totalGanados,
            totalIntentos = totalIntentos,
            ultimaFechaJugada = ultimaFechaJugada
        )
    }

    private fun obtenerIndiceDelDia(cantidad: Int): Int {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val diaDelAnio = calendario.get(Calendar.DAY_OF_YEAR)

        val clave = anio * 1000 + diaDelAnio
        return clave % cantidad
    }

    fun obtenerPalabraDelDia(): String {
        val indice = obtenerIndiceDelDia(Diccionario.palabras.size)
        return Diccionario.palabras[indice]
    }

    fun evaluarIntento(intento: String, objetivo: String): IntArray {
        val resultado = IntArray(intento.length) { GRIS }
        val disponibles = objetivo.toMutableList()

        for (i in intento.indices) {
            if (intento[i] == objetivo[i]) {
                resultado[i] = VERDE
                disponibles[i] = '#'
            }
        }

        for (i in intento.indices) {
            if (resultado[i] == VERDE) continue

            val indice = disponibles.indexOf(intento[i])

            if (indice != -1) {
                resultado[i] = AMARILLO
                disponibles[indice] = '#'
            }
        }

        return resultado
    }

    fun cargarEstado(context: Context): EstadoJuego {
        actualizarRachaVencida(context)

        val prefs = preferenciasJuego(context)
        val fechaGuardada = prefs.getString("fecha", null)
        val fechaHoy = obtenerFechaHoy()
        val palabraHoy = obtenerPalabraDelDia()

        if (fechaGuardada != fechaHoy) {
            val nuevoEstado = EstadoJuego(
                fecha = fechaHoy,
                palabra = palabraHoy,
                intentos = mutableListOf(),
                terminado = false,
                gano = false
            )

            guardarEstado(context, nuevoEstado)
            return nuevoEstado
        }

        val fechaTerminada = prefs.getString(CLAVE_FECHA_TERMINADA, null)

        if (fechaTerminada == fechaHoy) {
            return EstadoJuego(
                fecha = fechaHoy,
                palabra = palabraHoy,
                intentos = mutableListOf(),
                terminado = true,
                gano = prefs.getBoolean("gano", false)
            )
        }

        val intentosJson = JSONArray(
            prefs.getString("intentos", "[]")
        )

        val intentos = mutableListOf<String>()

        for (i in 0 until intentosJson.length()) {
            intentos.add(intentosJson.getString(i))
        }

        return EstadoJuego(
            fecha = fechaGuardada,
            palabra = prefs.getString("palabra", palabraHoy) ?: palabraHoy,
            intentos = intentos,
            terminado = prefs.getBoolean("terminado", false),
            gano = prefs.getBoolean("gano", false)
        )
    }

    fun guardarEstado(context: Context, estado: EstadoJuego) {
        val prefs = preferenciasJuego(context)
        val intentosJson = JSONArray()

        estado.intentos.forEach { intento ->
            intentosJson.put(intento)
        }

        val editor = prefs.edit()
            .putString("fecha", estado.fecha)
            .putString("palabra", estado.palabra)
            .putString("intentos", intentosJson.toString())
            .putBoolean("terminado", estado.terminado)
            .putBoolean("gano", estado.gano)

        if (estado.terminado) {
            // Bloquea el juego para este usuario durante el día actual.
            editor.putString(CLAVE_FECHA_TERMINADA, estado.fecha)
        } else {
            // Al iniciar un día nuevo, permite jugar otra vez.
            editor.remove(CLAVE_FECHA_TERMINADA)
        }

        editor.apply()
    }

    fun registrarResultado(
        context: Context,
        gano: Boolean,
        intentosUsados: Int
    ) {
        // Primero verifica si se perdió una racha por no jugar.
        actualizarRachaVencida(context)

        val prefs = preferenciasEstadisticas(context)
        val fechaHoy = obtenerFechaHoy()
        val ultimaFecha = prefs.getString("ultimaFechaJugada", null)

        if (ultimaFecha == fechaHoy) return

        val totalJugados = prefs.getInt("totalJugados", 0) + 1
        val totalGanados = prefs.getInt("totalGanados", 0) + if (gano) 1 else 0
        val totalIntentos = prefs.getInt("totalIntentos", 0) + intentosUsados

        val rachaActual = if (gano) {
            prefs.getInt("rachaActual", 0) + 1
        } else {
            0
        }

        val rachaMaxima = maxOf(
            prefs.getInt("rachaMaxima", 0),
            rachaActual
        )

        prefs.edit()
            .putInt("totalJugados", totalJugados)
            .putInt("totalGanados", totalGanados)
            .putInt("totalIntentos", totalIntentos)
            .putInt("rachaActual", rachaActual)
            .putInt("rachaMaxima", rachaMaxima)
            .putString("ultimaFechaJugada", fechaHoy)
            .apply()

        sincronizarEstadisticas(
            racha = rachaActual,
            rachaMaxima = rachaMaxima,
            totalJugados = totalJugados,
            totalGanados = totalGanados,
            totalIntentos = totalIntentos,
            ultimaFechaJugada = fechaHoy
        )
    }

    private fun sincronizarEstadisticas(
        racha: Int,
        rachaMaxima: Int,
        totalJugados: Int,
        totalGanados: Int,
        totalIntentos: Int,
        ultimaFechaJugada: String
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val datos = mapOf(
            "palabraDelDia" to mapOf(
                "racha" to racha,
                "rachaMaxima" to rachaMaxima,
                "totalJugados" to totalJugados,
                "totalGanados" to totalGanados,
                "totalIntentos" to totalIntentos,
                "ultimaFechaJugada" to ultimaFechaJugada
            )
        )

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .set(datos, SetOptions.merge())
    }

    fun obtenerEstadisticas(context: Context): EstadisticasJuego {
        actualizarRachaVencida(context)

        val prefs = preferenciasEstadisticas(context)

        return EstadisticasJuego(
            totalJugados = prefs.getInt("totalJugados", 0),
            totalGanados = prefs.getInt("totalGanados", 0),
            rachaActual = prefs.getInt("rachaActual", 0),
            rachaMaxima = prefs.getInt("rachaMaxima", 0)
        )
    }

    fun yaJugoHoy(context: Context): Boolean {
        val prefs = preferenciasJuego(context)

        return prefs.getString(CLAVE_FECHA_TERMINADA, null) == obtenerFechaHoy()
    }
}