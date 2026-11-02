package com.example.syslearning.model

/**
 * Micro actividad de aprendizaje (colección "actividades" en Firestore).
 *
 * Sigue la secuencia de 7 pasos de las fichas de diseño pedagógico
 * (Gagné + Mayer):
 *  1. reto           -> Presentar el reto
 *  2. concepto       -> Explicar el concepto (máx. ~60 palabras)
 *  3. ejemploCodigo + ejemploExplicacion -> Mostrar un ejemplo hecho
 *  4. Actividad práctica   -> ejercicios con paso = PRACTICA
 *  5. Retroalimentación    -> pistas / explicación de cada ejercicio
 *  6. Comprobación         -> ejercicios con paso = COMPROBACION
 *  7. Recompensa y registro -> XP, racha, semáforo, evento
 *
 * Los ejercicios viven en la colección "ejercicios" (ver Ejercicio.kt).
 */
data class Actividad(

    var id: String = "",

    var titulo: String = "",

    var descripcion: String = "",

    var tipo: TipoActividad = TipoActividad.OPCION_MULTIPLE,

    /** XP de referencia que se muestra en la tarjeta. */
    var xp: Int = 0,

    var orden: Int = 0,

    var estado: EstadoActividad = EstadoActividad.BLOQUEADA,

    var activo: Boolean = true,

    // ---- Contenido pedagógico de la micro actividad ----

    /** Número del módulo (1 Introducción, 2 Estructuras de control, ...). */
    var modulo: Int = 1,

    /** Resultado de aprendizaje: qué debe saber hacer al terminar. */
    var objetivo: String = "",

    /** Nivel de la progresión: RECONOCIMIENTO, APLICACION o INTEGRACION. */
    var nivel: String = NivelActividad.RECONOCIMIENTO.name,

    /** Paso 1: pregunta o situación cotidiana. */
    var reto: String = "",

    /** Paso 2: explicación corta del concepto. */
    var concepto: String = "",

    /** Paso 3: fragmento de código del ejemplo (pseudocódigo). */
    var ejemploCodigo: String = "",

    /** Paso 3: qué pasa al ejecutar el ejemplo, paso a paso. */
    var ejemploExplicacion: String = "",

    // ---- Pantalla de teoría (mismo diseño que fragment_contenido) ----

    /** URL de la imagen de portada (Firebase Storage). Vacío = imagen por defecto. */
    var imagen: String = "",

    /** Tarjeta "¿Sabías que?" (opcional). */
    var sabiasQue: String = "",

    /** Tarjeta "Consejo" (opcional). */
    var consejo: String = "",

    /**
     * Códigos de las aulas a las que pertenece (ej. "SYS-SMQM9"), igual que
     * en CursoFirestore. Lista vacía = actividad GLOBAL, visible para todos.
     * Las actividades creadas antes de este campo se leen como globales.
     */
    var aulasAsignadas: List<String> = emptyList()

)

enum class NivelActividad(val etiqueta: String) {

    RECONOCIMIENTO("Reconocimiento"),
    APLICACION("Aplicación"),
    INTEGRACION("🏆 Reto final del módulo (integración)");

    companion object {
        fun desde(valor: String?): NivelActividad =
            values().firstOrNull { it.name == valor } ?: RECONOCIMIENTO
    }
}
