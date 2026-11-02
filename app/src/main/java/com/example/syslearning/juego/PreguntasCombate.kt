package com.example.syslearning.juego

import android.content.Context
import com.example.syslearning.actividades.EvaluadorEjercicio
import com.example.syslearning.model.Ejercicio
import com.example.syslearning.model.TipoEjercicio
import com.example.syslearning.util.LecturaRapida
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Preguntas para el combate: los ejercicios de selección de las micro
 * actividades (colección "ejercicios") más un banco propio, para que el
 * juego funcione aunque todavía haya pocos ejercicios creados.
 * Ordenar instrucciones no se usa porque no cabe en un turno corto.
 */
object PreguntasCombate {

    fun obtener(context: Context, onLista: (List<Ejercicio>) -> Unit) {

        LecturaRapida.consulta(
            context = context,
            query = FirebaseFirestore.getInstance()
                .collection("ejercicios")
                .whereEqualTo("activo", true),
            refrescar = false,
            onDatos = { documentos, _ ->
                val deActividades = documentos.mapNotNull { doc ->
                    runCatching {
                        doc.toObject(Ejercicio::class.java).apply { id = doc.id }
                    }.getOrNull()
                }.filter { esUtil(it) }

                onLista((deActividades + banco).shuffled())
            },
            onError = { onLista(banco.shuffled()) }
        )
    }

    private fun esUtil(e: Ejercicio): Boolean =
        e.tipoEjercicio() != TipoEjercicio.ORDENAR_INSTRUCCIONES &&
                EvaluadorEjercicio.validarParaGuardar(e) == null

    /** Opciones que se muestran (verdadero o falso tiene opciones fijas). */
    fun opciones(e: Ejercicio): List<String> =
        if (e.tipoEjercicio() == TipoEjercicio.VERDADERO_FALSO) listOf("Verdadero", "Falso") else e.opciones

    private fun pregunta(
        tipo: TipoEjercicio,
        enunciado: String,
        opciones: List<String>,
        correcta: Int,
        codigo: String = "",
        explicacion: String = ""
    ) = Ejercicio(
        id = "banco_${enunciado.hashCode()}",
        tipo = tipo.name,
        enunciado = enunciado,
        codigo = codigo,
        opciones = opciones,
        respuestaCorrecta = correcta,
        explicacion = explicacion
    )

    private val banco = listOf(
        pregunta(
            TipoEjercicio.CUESTIONARIO,
            "¿Qué es un algoritmo?",
            listOf("Un programa que solo usa números", "Una lista de pasos finitos y ordenados", "Un tipo de variable", "Un error del código"),
            1, explicacion = "Un algoritmo es una secuencia finita y ordenada de pasos."
        ),
        pregunta(
            TipoEjercicio.PREDECIR_RESULTADO,
            "¿Qué valor se escribe?",
            listOf("5", "6", "8", "2"),
            2, codigo = "x <- 2\nx <- x * 4\nEscribir x",
            explicacion = "x vale 2 y luego 2 × 4 = 8."
        ),
        pregunta(
            TipoEjercicio.PREDECIR_RESULTADO,
            "¿Cuánto da la expresión?",
            listOf("20", "14", "10", "24"),
            1, codigo = "2 + 3 * 4",
            explicacion = "La multiplicación va primero: 3 × 4 = 12, y 12 + 2 = 14."
        ),
        pregunta(
            TipoEjercicio.VERDADERO_FALSO,
            "La condición 5 >= 5 es verdadera.",
            emptyList(), 0, explicacion = ">= incluye el igual: 5 es mayor o igual que 5."
        ),
        pregunta(
            TipoEjercicio.PREDECIR_RESULTADO,
            "¿Cuántas veces se escribe «Hola»?",
            listOf("2", "3", "4", "Ninguna"),
            1, codigo = "Para i <- 1 Hasta 3 Hacer\n    Escribir \"Hola\"\nFinPara",
            explicacion = "i toma los valores 1, 2 y 3."
        ),
        pregunta(
            TipoEjercicio.COMPLETAR_CODIGO,
            "¿Qué falta para que el ciclo termine?",
            listOf("n <- n + 1", "n <- 0", "Escribir n", "n <- n - 1"),
            0, codigo = "n <- 1\nMientras n <= 3 Hacer\n    Escribir n\n    ____\nFinMientras",
            explicacion = "n debe aumentar para que la condición llegue a ser falsa."
        ),
        pregunta(
            TipoEjercicio.CUESTIONARIO,
            "¿Qué tipo de dato es «Sys Learning»?",
            listOf("Entero", "Real", "Texto (cadena)", "Lógico"),
            2, explicacion = "Un texto entre comillas es una cadena de caracteres."
        ),
        pregunta(
            TipoEjercicio.PREDECIR_RESULTADO,
            "¿Qué se escribe si nota = 2.5?",
            listOf("Aprobó", "Reprobó", "Nada", "Error"),
            1, codigo = "Si nota >= 3.0 Entonces\n    Escribir \"Aprobó\"\nSiNo\n    Escribir \"Reprobó\"\nFinSi",
            explicacion = "2.5 no es mayor o igual que 3.0, así que se ejecuta el SiNo."
        ),
        pregunta(
            TipoEjercicio.VERDADERO_FALSO,
            "Una variable puede cambiar de valor durante el algoritmo.",
            emptyList(), 0, explicacion = "Por eso se llama variable."
        ),
        pregunta(
            TipoEjercicio.CUESTIONARIO,
            "¿Para qué sirve una función?",
            listOf("Para repetir el mismo código en varias partes sin reescribirlo", "Para borrar variables", "Para que el programa sea más lento", "Solo para mostrar mensajes"),
            0, explicacion = "Las funciones agrupan instrucciones reutilizables."
        ),
        pregunta(
            TipoEjercicio.PREDECIR_RESULTADO,
            "¿Qué valor tiene notas[2]?",
            listOf("4.0", "3.5", "2.8", "No existe"),
            1, codigo = "notas <- [4.0, 3.5, 2.8]\n// Los índices empiezan en 1",
            explicacion = "Con índices desde 1, la posición 2 es 3.5."
        ),
        pregunta(
            TipoEjercicio.VERDADERO_FALSO,
            "Un ciclo Mientras siempre se ejecuta al menos una vez.",
            emptyList(), 1, explicacion = "Si la condición es falsa desde el inicio, no se ejecuta ninguna vez."
        )
    )
}
