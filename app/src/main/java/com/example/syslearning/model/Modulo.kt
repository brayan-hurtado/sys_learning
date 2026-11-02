package com.example.syslearning.model

/**
 * Módulo de aprendizaje (colección "modulos" en Firestore).
 * Las actividades se asignan a un módulo con su campo "modulo" (= numero).
 * El número no se puede cambiar después de crear el módulo, para no
 * desconectar las actividades que ya tiene.
 */
data class Modulo(
    var id: String = "",
    var numero: Int = 1,
    var titulo: String = "",
    var descripcion: String = "",
    var activo: Boolean = true
) {
    fun nombreCompleto(): String = "Módulo $numero · $titulo"

    companion object {

        /**
         * Módulos de la ficha de diseño pedagógico. Se usan mientras el
         * administrador no haya creado los suyos, y como sugerencia al crear.
         */
        val SUGERIDOS = listOf(
            Modulo("", 1, "Introducción a la programación",
                "Algoritmos, variables, operadores, entrada y salida."),
            Modulo("", 2, "Estructuras de control",
                "Condicionales y ciclos para tomar decisiones y repetir instrucciones."),
            Modulo("", 3, "Funciones y procedimientos",
                "Dividir un problema en subprogramas reutilizables."),
            Modulo("", 4, "Arreglos y vectores",
                "Guardar colecciones de datos y recorrerlas."),
            Modulo("", 5, "Matrices",
                "Datos en filas y columnas con ciclos anidados.")
        )

        /**
         * Une los módulos de Firestore con los sugeridos: si existe uno con
         * el mismo número en Firestore, gana ese. Los inactivos se quitan.
         */
        fun combinar(deFirestore: List<Modulo>): List<Modulo> {
            val porNumero = SUGERIDOS.associateBy { it.numero }.toMutableMap()
            deFirestore.forEach { porNumero[it.numero] = it }
            return porNumero.values.filter { it.activo }.sortedBy { it.numero }
        }
    }
}
