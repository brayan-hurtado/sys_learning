package com.example.syslearning

data class RankingUsuario(
    val nombre: String,
    val racha: Int,
    val cursosCompletados: Int,
    val puntos: Int,
    val uid: String = "",
    /** Micro actividades dominadas. */
    val actividadesCompletadas: Int = 0,
    /** Veces que terminó una micro actividad. */
    val intentosActividades: Int = 0,
    /** Dominadas / intentos, de 0 a 100. */
    val precision: Int = 0,
    /** XP acumulado en micro actividades. */
    val xp: Int = 0
)
