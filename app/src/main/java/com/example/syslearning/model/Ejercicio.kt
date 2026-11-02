package com.example.syslearning.model

/**
 * Ejercicio de una micro actividad (colección "ejercicios" en Firestore).
 *
 * Un mismo modelo cubre los 6 tipos de actividad de la documentación.
 * Cómo se interpretan "opciones" y "respuestaCorrecta" depende del tipo:
 *
 *  - CUESTIONARIO, COMPLETAR_CODIGO, PREDECIR_RESULTADO, RETO:
 *      opciones = alternativas; respuestaCorrecta = índice (0 = la primera).
 *  - VERDADERO_FALSO:
 *      opciones = ["Verdadero", "Falso"]; respuestaCorrecta = 0 o 1.
 *  - ENCONTRAR_ERROR:
 *      opciones = líneas del algoritmo; respuestaCorrecta = índice de la
 *      línea que tiene el error.
 *  - ORDENAR_INSTRUCCIONES:
 *      opciones = instrucciones EN EL ORDEN CORRECTO; la app las desordena.
 *      respuestaCorrecta no se usa.
 *
 * "tipo" y "paso" se guardan como texto (nombre del enum) para que un
 * documento mal escrito en Firestore no rompa la deserialización.
 */
data class Ejercicio(

    var id: String = "",

    /** id del documento de la actividad en "actividades". */
    var actividadId: String = "",

    /** PRACTICA (paso 4) o COMPROBACION (paso 6). */
    var paso: String = PasoEjercicio.PRACTICA.name,

    var tipo: String = TipoEjercicio.CUESTIONARIO.name,

    var enunciado: String = "",

    /** Fragmento de código que acompaña al enunciado (opcional). */
    var codigo: String = "",

    var opciones: List<String> = emptyList(),

    var respuestaCorrecta: Int = 0,

    /** Pista del primer error: dice qué revisar, sin dar la respuesta. */
    var pista1: String = "",

    /** Pista del segundo error: más concreta o un ejemplo parecido. */
    var pista2: String = "",

    /** Por qué la respuesta correcta es correcta. */
    var explicacion: String = "",

    var orden: Int = 0,

    var activo: Boolean = true

) {

    fun tipoEjercicio(): TipoEjercicio = TipoEjercicio.desde(tipo)

    fun pasoEjercicio(): PasoEjercicio = PasoEjercicio.desde(paso)
}

enum class TipoEjercicio(
    val etiqueta: String,
    val sigla: String,
    /** Texto de ayuda que ve el administrador al crear el ejercicio. */
    val ayudaOpciones: String
) {

    CUESTIONARIO(
        "Cuestionario de comprensión", "CUE",
        "Escribe una alternativa por línea."
    ),
    VERDADERO_FALSO(
        "Verdadero o falso", "V/F",
        "Las opciones son fijas: Verdadero y Falso."
    ),
    COMPLETAR_CODIGO(
        "Completar código", "COM",
        "Escribe en el código ____ donde falta la instrucción. Una alternativa por línea."
    ),
    PREDECIR_RESULTADO(
        "Predecir el resultado", "PRE",
        "Escribe el algoritmo en Código. Una salida posible por línea."
    ),
    ENCONTRAR_ERROR(
        "Encontrar el error", "ERR",
        "Escribe UNA LÍNEA DEL ALGORITMO por línea. La respuesta es el número de la línea con el error."
    ),
    ORDENAR_INSTRUCCIONES(
        "Ordenar instrucciones", "ORD",
        "Escribe las instrucciones EN EL ORDEN CORRECTO, una por línea. La app las desordena."
    ),
    RETO(
        "Reto de resolución de problemas", "RET",
        "Escribe una solución posible por línea."
    );

    /** Tipos en los que el aprendiz elige una opción de una lista. */
    fun esSeleccion(): Boolean = this != ORDENAR_INSTRUCCIONES

    companion object {
        fun desde(valor: String?): TipoEjercicio =
            values().firstOrNull { it.name == valor } ?: CUESTIONARIO
    }
}

enum class PasoEjercicio(val etiqueta: String) {

    PRACTICA("Actividad práctica"),
    COMPROBACION("Comprobación del aprendizaje");

    companion object {
        fun desde(valor: String?): PasoEjercicio =
            values().firstOrNull { it.name == valor } ?: PRACTICA
    }
}
