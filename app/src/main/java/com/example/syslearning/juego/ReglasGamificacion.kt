package com.example.syslearning.juego

import kotlin.random.Random

/**
 * Reglas de la economía de monedas y del minijuego SYS ROBOT.
 * No depende de Android ni de Firebase: se prueba con JUnit
 * (ReglasGamificacionTest).
 *
 * Las monedas 🪙 son distintas de los puntos del ranking: los puntos
 * miden el aprendizaje (Puntuacion.kt); las monedas se gastan en mejoras
 * de SYS-01. Hay una sola moneda para toda la app.
 */
object ReglasGamificacion {

    // ==================================================================
    // 1. Monedas por actividades
    // ==================================================================

    /** Estado de pago guardado por actividad, para no pagar dos veces. */
    const val PAGO_PARTICIPACION = "participacion"
    const val PAGO_DOMINIO = "dominio"

    const val MONEDAS_DOMINIO_1_INTENTO = 50
    const val MONEDAS_DOMINIO_2_INTENTOS = 35
    const val MONEDAS_PARTICIPACION = 10

    data class PagoActividad(val monedas: Int, val nuevoEstado: String?, val motivo: String)

    /**
     * @param estadoPrevio lo que ya se pagó por esta actividad (null = nada).
     * @param intentoAcierto intento en que acertó la comprobación, o null.
     *
     *  - Primera vez que la domina: 50 (1 intento) o 35 (2 intentos).
     *  - La termina sin dominarla: 10 monedas, una sola vez por actividad.
     *  - Si ya la había dominado: 0 (repetirla no da monedas).
     */
    fun pagoActividad(
        estadoPrevio: String?,
        dominada: Boolean,
        intentoAcierto: Int?,
        /** El reto final de un módulo paga el doble al dominarlo. */
        esReto: Boolean = false
    ): PagoActividad {

        if (estadoPrevio == PAGO_DOMINIO) {
            return PagoActividad(0, PAGO_DOMINIO, "Ya la habías dominado: repetirla no da monedas")
        }

        if (dominada) {
            val base = if (intentoAcierto == 1) MONEDAS_DOMINIO_1_INTENTO else MONEDAS_DOMINIO_2_INTENTOS
            val detalle = if (intentoAcierto == 1) "dominada al primer intento" else "dominada en 2 intentos"
            return if (esReto) {
                PagoActividad(base * 2, PAGO_DOMINIO, "🏆 Reto final $detalle (doble)")
            } else {
                PagoActividad(base, PAGO_DOMINIO, "Actividad $detalle")
            }
        }

        if (estadoPrevio == PAGO_PARTICIPACION) {
            return PagoActividad(0, PAGO_PARTICIPACION, "Domínala para ganar más monedas")
        }

        return PagoActividad(MONEDAS_PARTICIPACION, PAGO_PARTICIPACION, "Actividad terminada")
    }

    // ---- La Palabra del Día ----

    const val MONEDAS_PALABRA_DEL_DIA = 50

    data class RecompensaPalabra(val monedas: Int, val xp: Int, val motivo: String)

    /**
     * Adivinar la palabra del día: 50 monedas y XP según los intentos
     * (1–2: 30 XP, 3–4: 20 XP, 5–6: 10 XP). Perder no da nada.
     * Se cobra una sola vez por día.
     */
    fun recompensaPalabraDelDia(gano: Boolean, intentos: Int, yaCobradaHoy: Boolean): RecompensaPalabra {
        if (yaCobradaHoy) return RecompensaPalabra(0, 0, "La recompensa de hoy ya fue entregada")
        if (!gano) return RecompensaPalabra(0, 0, "Adivina la palabra para ganar monedas y XP")
        val xp = when {
            intentos <= 2 -> 30
            intentos <= 4 -> 20
            else -> 10
        }
        return RecompensaPalabra(MONEDAS_PALABRA_DEL_DIA, xp, "Palabra adivinada en $intentos intento(s)")
    }

    // ==================================================================
    // 2. Racha diaria de actividades
    // ==================================================================

    /** Días de racha que entregan un premio especial, y su valor. */
    val HITOS_RACHA = linkedMapOf(3 to 50, 7 to 120, 14 to 250, 30 to 500)

    data class EstadoRacha(
        val actual: Int = 0,
        val maxima: Int = 0,
        /** Último día con una actividad válida, "yyyy-MM-dd". */
        val ultimoDia: String? = null,
        /** Día en que empezó el ciclo actual de la racha. */
        val inicioCiclo: String? = null,
        /** Hitos ya cobrados en ESTE ciclo. */
        val hitosCobrados: Set<Int> = emptySet()
    )

    data class ResultadoRacha(
        val estado: EstadoRacha,
        /** false si ya había una actividad válida hoy (regla 3). */
        val aumento: Boolean,
        val monedasDiarias: Int,
        /** Hitos alcanzados ahora (cada uno se cobra una vez por ciclo). */
        val hitosNuevos: List<Int>,
        val monedasHitos: Int
    ) {
        val monedasTotales get() = monedasDiarias + monedasHitos
    }

    /** Monedas por mantener la racha: 10 el día 1 y +5 por día, hasta 40. */
    fun monedasPorDiaDeRacha(dia: Int): Int = (10 + (dia - 1) * 5).coerceIn(0, 40)

    /**
     * Se llama SOLO cuando el estudiante termina una actividad válida
     * (abrir la app no cuenta, regla 1 y 2).
     *
     *  - Ya hubo actividad hoy: la racha no cambia (regla 3).
     *  - La última fue ayer: la racha sube 1.
     *  - Pasó un día completo o más: empieza un ciclo nuevo en 1 (regla 4)
     *    y los hitos vuelven a estar disponibles para ese ciclo (regla 5).
     */
    fun registrarDiaDeRacha(estado: EstadoRacha, hoy: String, ayer: String): ResultadoRacha {

        if (estado.ultimoDia == hoy) {
            return ResultadoRacha(estado, aumento = false, monedasDiarias = 0, hitosNuevos = emptyList(), monedasHitos = 0)
        }

        val continua = estado.ultimoDia == ayer && estado.actual > 0

        val actual = if (continua) estado.actual + 1 else 1
        val inicioCiclo = if (continua) estado.inicioCiclo ?: hoy else hoy
        val cobrados = if (continua) estado.hitosCobrados else emptySet()

        val hitosNuevos = HITOS_RACHA.keys.filter { it <= actual && it !in cobrados }
        val monedasHitos = hitosNuevos.sumOf { HITOS_RACHA.getValue(it) }

        val nuevo = EstadoRacha(
            actual = actual,
            maxima = maxOf(estado.maxima, actual),
            ultimoDia = hoy,
            inicioCiclo = inicioCiclo,
            hitosCobrados = cobrados + hitosNuevos
        )

        return ResultadoRacha(nuevo, true, monedasPorDiaDeRacha(actual), hitosNuevos, monedasHitos)
    }

    /** La racha que se muestra: si el último día no fue hoy ni ayer, ya se perdió. */
    fun rachaVigente(estado: EstadoRacha, hoy: String, ayer: String): Int =
        if (estado.ultimoDia == hoy || estado.ultimoDia == ayer) estado.actual else 0

    // ==================================================================
    // 3. Materiales y componentes de SYS-01
    // ==================================================================

    enum class Material(val clave: String, val nombre: String, val icono: String) {
        CIRCUITOS("circuitos", "Circuitos", "🔌"),
        NUCLEOS("nucleos", "Núcleos digitales", "💠"),
        PLACAS("placas", "Placas de armadura", "🛡️"),
        CHIPS("chips", "Chips de energía", "🔋")
    }

    data class Costo(val monedas: Int, val materiales: Map<Material, Int> = emptyMap())

    enum class Componente(
        val clave: String,
        val nombre: String,
        val icono: String,
        val descripcion: String,
        /** Costo de los niveles 1, 2 y 3. El nivel 3 es una mejora especial con materiales. */
        val costos: List<Costo>
    ) {
        MANDIBULA(
            "mandibula", "Mandíbula láser", "⚡", "+5 de ataque por nivel",
            listOf(
                Costo(100),
                Costo(250),
                Costo(500, mapOf(Material.CIRCUITOS to 3, Material.NUCLEOS to 1))
            )
        ),
        ARMADURA(
            "armadura", "Armadura reforzada", "🛡️", "+4 de defensa y +20 de vida por nivel",
            listOf(
                Costo(100),
                Costo(250),
                Costo(500, mapOf(Material.PLACAS to 3))
            )
        ),
        PATAS(
            "patas", "Patas turbo", "💨", "+5 de velocidad por nivel (esquivar ataques)",
            listOf(
                Costo(100),
                Costo(250),
                Costo(500, mapOf(Material.CIRCUITOS to 2, Material.CHIPS to 2))
            )
        ),
        NUCLEO(
            "nucleo", "Núcleo de energía", "🔆", "+25 de energía por nivel; desbloquea el Pulso EMP",
            listOf(
                Costo(150),
                Costo(300, mapOf(Material.CHIPS to 1)),
                Costo(600, mapOf(Material.CHIPS to 3, Material.NUCLEOS to 1))
            )
        );

        companion object {
            const val NIVEL_MAXIMO = 3
        }
    }

    data class Atributos(
        val vida: Int,
        val ataque: Int,
        val defensa: Int,
        val energia: Int,
        val velocidad: Int,
        /** El Pulso EMP se desbloquea con el Núcleo de energía nivel 1. */
        val tieneHabilidadEspecial: Boolean
    )

    /** Atributos iniciales: vida 100, ataque 10, defensa 5, energía 50, velocidad 10. */
    fun atributos(niveles: Map<Componente, Int>): Atributos {
        val mandibula = niveles[Componente.MANDIBULA] ?: 0
        val armadura = niveles[Componente.ARMADURA] ?: 0
        val patas = niveles[Componente.PATAS] ?: 0
        val nucleo = niveles[Componente.NUCLEO] ?: 0
        return Atributos(
            vida = 100 + armadura * 20,
            ataque = 10 + mandibula * 5,
            defensa = 5 + armadura * 4,
            energia = 50 + nucleo * 25,
            velocidad = 10 + patas * 5,
            tieneHabilidadEspecial = nucleo >= 1
        )
    }

    sealed class Compra {
        data class Permitida(val nivelNuevo: Int, val costo: Costo) : Compra()
        data class Rechazada(val motivo: String) : Compra()
    }

    /**
     * Reglas de compra: un nivel a la vez, en orden, sin repetir, y con
     * saldo y materiales suficientes.
     */
    fun evaluarCompra(
        componente: Componente,
        nivelActual: Int,
        nivelDeseado: Int,
        monedas: Int,
        materiales: Map<Material, Int>
    ): Compra {

        if (nivelDeseado <= nivelActual) {
            return Compra.Rechazada("Ya tienes el nivel $nivelDeseado de ${componente.nombre}.")
        }
        if (nivelDeseado > Componente.NIVEL_MAXIMO) {
            return Compra.Rechazada("${componente.nombre} ya está al nivel máximo.")
        }
        if (nivelDeseado != nivelActual + 1) {
            return Compra.Rechazada("Primero compra el nivel ${nivelActual + 1}.")
        }

        val costo = componente.costos[nivelDeseado - 1]

        if (monedas < costo.monedas) {
            return Compra.Rechazada("Te faltan ${costo.monedas - monedas} monedas.")
        }

        val faltantes = costo.materiales.filter { (material, cantidad) ->
            (materiales[material] ?: 0) < cantidad
        }
        if (faltantes.isNotEmpty()) {
            val texto = faltantes.entries.joinToString(", ") { (material, cantidad) ->
                "${cantidad - (materiales[material] ?: 0)} ${material.nombre.lowercase()}"
            }
            return Compra.Rechazada("Te faltan materiales: $texto. Consíguelos venciendo virus.")
        }

        return Compra.Permitida(nivelDeseado, costo)
    }

    // ==================================================================
    // 4. Enemigos y recompensas
    // ==================================================================

    enum class Enemigo(
        val clave: String,
        val nombre: String,
        val dificultad: String,
        val vida: Int,
        val ataque: Int,
        val descripcion: String,
        /** Primera victoria. */
        val monedas: Int,
        val materialesPrimeraVictoria: Map<Material, Int>,
        /** Materiales posibles en victorias repetidas (se entrega 1). */
        val botinRepeticion: List<Material>,
        val esJefe: Boolean,
        /** Enemigo que hay que vencer antes para desbloquearlo. */
        val requiere: String?
    ) {
        BASICO(
            "basico", "Virus básico", "Fácil", 50, 5,
            "Un virus simple que se copia en archivos sueltos.",
            30, mapOf(Material.CIRCUITOS to 2),
            listOf(Material.CIRCUITOS), esJefe = false, requiere = null
        ),
        CORRUPTOR(
            "corruptor", "Virus corruptor", "Media", 100, 10,
            "Daña los datos y desordena el código a su paso.",
            70, mapOf(Material.CHIPS to 2, Material.PLACAS to 1),
            listOf(Material.CHIPS, Material.PLACAS), esJefe = false, requiere = "basico"
        ),
        DESTRUCTOR(
            "destructor", "Virus destructor", "Difícil · Jefe", 200, 20,
            "El jefe de la invasión: borra sistemas enteros.",
            150, mapOf(Material.NUCLEOS to 1, Material.PLACAS to 2, Material.CHIPS to 1),
            listOf(Material.NUCLEOS, Material.PLACAS, Material.CIRCUITOS), esJefe = true, requiere = "corruptor"
        );

        companion object {
            fun desde(clave: String?): Enemigo? = values().firstOrNull { it.clave == clave }
        }
    }

    // ---- Revanchas: el virus se fortalece con cada derrota ----

    /** Nivel máximo que alcanza un virus (después ya no crece). */
    const val NIVEL_MAXIMO_VIRUS = 10

    /** Por cada nivel por encima del 1: +20 % de vida y +15 % de ataque (sobre la base). */
    const val AUMENTO_VIDA_POR_NIVEL = 20
    const val AUMENTO_ATAQUE_POR_NIVEL = 15

    data class StatsVirus(val nivel: Int, val vida: Int, val ataque: Int)

    /**
     * El primer combate es de nivel 1. Cada victoria sube un nivel al virus,
     * así cada revancha es más difícil. Perder no lo fortalece.
     */
    fun statsVirus(enemigo: Enemigo, victorias: Int): StatsVirus {
        val nivel = (victorias + 1).coerceIn(1, NIVEL_MAXIMO_VIRUS)
        val extra = nivel - 1
        val vida = enemigo.vida + (enemigo.vida * AUMENTO_VIDA_POR_NIVEL * extra) / 100
        // El ataque se redondea hacia arriba para que siempre suba al menos 1.
        val ataque = enemigo.ataque + (enemigo.ataque * AUMENTO_ATAQUE_POR_NIVEL * extra + 99) / 100
        return StatsVirus(nivel, vida, ataque)
    }

    fun desbloqueado(enemigo: Enemigo, derrotados: Set<String>): Boolean =
        enemigo.requiere == null || enemigo.requiere in derrotados

    /** Victorias repetidas que dan algo por día, sumando todos los enemigos. */
    const val REPETICIONES_PREMIADAS_POR_DIA = 3

    data class Recompensa(
        val monedas: Int,
        val materiales: Map<Material, Int>,
        val primeraVictoria: Boolean,
        val mensaje: String
    )

    /**
     * Anti-farmeo:
     *  - Primera victoria: monedas completas + materiales garantizados.
     *  - Victorias repetidas: 10 % de las monedas + 1 material, solo las
     *    primeras [REPETICIONES_PREMIADAS_POR_DIA] del día. Después, 0.
     */
    fun recompensaVictoria(
        enemigo: Enemigo,
        yaDerrotado: Boolean,
        repeticionesPremiadasHoy: Int,
        aleatorio: Random = Random.Default
    ): Recompensa {

        if (!yaDerrotado) {
            return Recompensa(
                enemigo.monedas, enemigo.materialesPrimeraVictoria, true,
                if (enemigo.esJefe) "¡Derrotaste al jefe! Recompensa especial desbloqueada."
                else "¡Primera victoria contra ${enemigo.nombre}!"
            )
        }

        if (repeticionesPremiadasHoy >= REPETICIONES_PREMIADAS_POR_DIA) {
            return Recompensa(
                0, emptyMap(), false,
                "Ya recibiste las recompensas de hoy por combates repetidos. Vuelve mañana o aprende en las actividades para ganar más monedas."
            )
        }

        val material = enemigo.botinRepeticion[aleatorio.nextInt(enemigo.botinRepeticion.size)]

        return Recompensa(
            monedas = enemigo.monedas / 10,
            materiales = mapOf(material to 1),
            primeraVictoria = false,
            mensaje = "Victoria repetida: recompensa reducida " +
                    "(${repeticionesPremiadasHoy + 1} de $REPETICIONES_PREMIADAS_POR_DIA hoy)."
        )
    }

    // ==================================================================
    // 5. Combate
    // ==================================================================

    const val COSTO_PULSO_EMP = 30
    const val ENERGIA_POR_TURNO = 10

    /**
     * Respuesta correcta: SYS-01 ataca con su ataque + 0 a 4 de variación.
     * Con el Pulso EMP, el daño se duplica.
     */
    fun danoRobot(ataque: Int, pulsoEmp: Boolean, aleatorio: Random = Random.Default): Int {
        val base = ataque + aleatorio.nextInt(0, 5)
        return if (pulsoEmp) base * 2 else base
    }

    /**
     * Turno del virus: su ataque menos la mitad de la defensa, mínimo 1.
     * Si el estudiante respondió mal, el golpe es crítico (×1,5).
     * SYS-01 puede esquivar con una probabilidad igual a su velocidad (%).
     */
    fun danoEnemigo(
        ataqueEnemigo: Int,
        defensa: Int,
        velocidad: Int,
        respuestaIncorrecta: Boolean,
        aleatorio: Random = Random.Default
    ): Int {
        if (aleatorio.nextInt(100) < velocidad) return 0
        val base = (ataqueEnemigo - defensa / 2 + aleatorio.nextInt(0, 4)).coerceAtLeast(1)
        return if (respuestaIncorrecta) (base * 3) / 2 else base
    }
}
