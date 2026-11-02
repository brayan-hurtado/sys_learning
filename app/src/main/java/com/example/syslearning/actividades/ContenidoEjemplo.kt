package com.example.syslearning.actividades

import com.example.syslearning.model.NivelActividad
import com.example.syslearning.model.PasoEjercicio
import com.example.syslearning.model.TipoEjercicio
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Contenido de ejemplo tomado de las fichas de diseño pedagógico
 * (micro actividades 1.1 y 2.4). Sirve para probar la sesión de 7 pasos
 * y como plantilla para que el administrador cree las demás.
 *
 * Se carga desde el panel de administrador ("Cargar ejemplo"). Si ya
 * existe una actividad con el mismo título, no se vuelve a crear.
 */
object ContenidoEjemplo {

    private data class EjercicioSemilla(
        val paso: PasoEjercicio,
        val tipo: TipoEjercicio,
        val enunciado: String,
        val codigo: String = "",
        val opciones: List<String>,
        val correcta: Int = 0,
        val pista1: String,
        val pista2: String = "",
        val explicacion: String
    )

    private data class ActividadSemilla(
        val titulo: String,
        val descripcion: String,
        val modulo: Int,
        val nivel: NivelActividad,
        val orden: Int,
        val objetivo: String,
        val reto: String,
        val concepto: String,
        val ejemploCodigo: String,
        val ejemploExplicacion: String,
        val ejercicios: List<EjercicioSemilla>
    )

    private val actividades = listOf(

        ActividadSemilla(
            titulo = "¿Qué es un algoritmo?",
            descripcion = "Módulo 1 · Introducción a la programación",
            modulo = 1,
            nivel = NivelActividad.RECONOCIMIENTO,
            orden = 1,
            objetivo = "Reconocer un algoritmo y ordenar sus pasos.",
            reto = "¿Cómo le explicarías a alguien que nunca lo ha hecho cómo preparar un café?",
            concepto = "Un algoritmo es una lista de pasos finitos, ordenados y precisos que resuelve " +
                    "un problema. Finito: termina. Ordenado: cada paso va en su lugar. Preciso: no deja " +
                    "dudas. Un programa es un algoritmo escrito en un lenguaje que el computador entiende.",
            ejemploCodigo = """
                Algoritmo SumarDosNumeros
                    Leer a
                    Leer b
                    suma <- a + b
                    Escribir suma
                FinAlgoritmo
            """.trimIndent(),
            ejemploExplicacion = "1. Lee el primer número y lo guarda en a.\n" +
                    "2. Lee el segundo y lo guarda en b.\n" +
                    "3. Suma a y b, y guarda el resultado en suma.\n" +
                    "4. Muestra suma. Si a = 4 y b = 3, escribe 7.",
            ejercicios = listOf(
                EjercicioSemilla(
                    paso = PasoEjercicio.PRACTICA,
                    tipo = TipoEjercicio.ORDENAR_INSTRUCCIONES,
                    enunciado = "Ordena los pasos para preparar un café.",
                    opciones = listOf(
                        "Calentar el agua",
                        "Poner el café en la taza",
                        "Servir el agua caliente en la taza",
                        "Revolver",
                        "Tomar el café"
                    ),
                    pista1 = "¿Puedes servir el agua antes de calentarla?",
                    pista2 = "El café debe estar en la taza antes de servir el agua.",
                    explicacion = "Cada paso depende del anterior. Por eso el orden importa en un algoritmo."
                ),
                EjercicioSemilla(
                    paso = PasoEjercicio.COMPROBACION,
                    tipo = TipoEjercicio.CUESTIONARIO,
                    enunciado = "¿Cuál de estas listas es un algoritmo?",
                    opciones = listOf(
                        "Hacer algo rico de comer",
                        "1. Leer la nota. 2. Si es 3.0 o más, escribir «Aprobó». 3. Terminar.",
                        "Repetir para siempre: mirar el reloj",
                        "Estudiar mucho"
                    ),
                    correcta = 1,
                    pista1 = "Recuerda las tres condiciones: finito, ordenado y preciso.",
                    pista2 = "Descarta las opciones que no terminan o que no dicen exactamente qué hacer.",
                    explicacion = "Es la única lista con pasos precisos, en orden y que termina."
                ),
                EjercicioSemilla(
                    paso = PasoEjercicio.COMPROBACION,
                    tipo = TipoEjercicio.ORDENAR_INSTRUCCIONES,
                    enunciado = "Ordena el algoritmo que calcula el área de un rectángulo.",
                    opciones = listOf(
                        "Leer base",
                        "Leer altura",
                        "area <- base * altura",
                        "Escribir area"
                    ),
                    pista1 = "¿Se puede calcular el área sin conocer la base y la altura?",
                    pista2 = "Primero se leen los datos, luego se calcula y al final se muestra.",
                    explicacion = "Entrada, proceso y salida: se leen los datos, se calcula el área y se muestra."
                )
            )
        ),

        ActividadSemilla(
            titulo = "Ciclos Para y Mientras",
            descripcion = "Módulo 2 · Estructuras de control",
            modulo = 2,
            nivel = NivelActividad.APLICACION,
            orden = 2,
            objetivo = "Usar un ciclo con contador y acumulador, y reconocer cuándo termina.",
            reto = "Tienes que sumar las notas de 5 compañeros. ¿Escribirías la suma 5 veces?",
            concepto = "Un ciclo repite instrucciones. «Para» se usa cuando sabes cuántas vueltas dar; " +
                    "«Mientras» repite mientras una condición sea verdadera. Un contador cuenta las " +
                    "vueltas y un acumulador va sumando valores. Algo dentro del ciclo debe hacer que " +
                    "la condición llegue a ser falsa.",
            ejemploCodigo = """
                Algoritmo SumarNotas
                    suma <- 0
                    Para i <- 1 Hasta 3 Hacer
                        Leer nota
                        suma <- suma + nota
                    FinPara
                    Escribir suma
                FinAlgoritmo
            """.trimIndent(),
            ejemploExplicacion = "suma empieza en 0.\n" +
                    "Vuelta 1: lee 4 y suma = 4.\n" +
                    "Vuelta 2: lee 3 y suma = 7.\n" +
                    "Vuelta 3: lee 5 y suma = 12.\n" +
                    "Cuando i pasa de 3, el ciclo termina y se escribe 12.",
            ejercicios = listOf(
                EjercicioSemilla(
                    paso = PasoEjercicio.PRACTICA,
                    tipo = TipoEjercicio.PREDECIR_RESULTADO,
                    enunciado = "¿Cuántas veces se escribe «Hola»?",
                    codigo = """
                        Para i <- 1 Hasta 4 Hacer
                            Escribir "Hola"
                        FinPara
                    """.trimIndent(),
                    opciones = listOf("3", "4", "5", "Infinitas"),
                    correcta = 1,
                    pista1 = "Cuenta los valores que toma i: empieza en 1 y llega hasta 4.",
                    explicacion = "i toma los valores 1, 2, 3 y 4: son 4 vueltas."
                ),
                EjercicioSemilla(
                    paso = PasoEjercicio.COMPROBACION,
                    tipo = TipoEjercicio.ENCONTRAR_ERROR,
                    enunciado = "Este algoritmo nunca termina. ¿En qué línea está el error?",
                    opciones = listOf(
                        "contador <- 1",
                        "Mientras contador <= 5 Hacer",
                        "    Escribir contador",
                        "    contador <- contador - 1",
                        "FinMientras"
                    ),
                    correcta = 3,
                    pista1 = "¿Qué instrucción hace que la condición llegue a ser falsa?",
                    pista2 = "Mira cómo cambia contador en cada vuelta: ¿se acerca a 5 o se aleja?",
                    explicacion = "contador debe aumentar (contador <- contador + 1). Si disminuye, " +
                            "siempre es menor o igual a 5 y el ciclo nunca termina."
                ),
                EjercicioSemilla(
                    paso = PasoEjercicio.COMPROBACION,
                    tipo = TipoEjercicio.PREDECIR_RESULTADO,
                    enunciado = "¿Qué valor se escribe al final?",
                    codigo = """
                        total <- 0
                        Para i <- 1 Hasta 3 Hacer
                            total <- total + i
                        FinPara
                        Escribir total
                    """.trimIndent(),
                    opciones = listOf("3", "5", "6", "0"),
                    correcta = 2,
                    pista1 = "Haz la traza: anota el valor de total después de cada vuelta.",
                    pista2 = "Vuelta 1: total = 1. Vuelta 2: total = 3. ¿Y en la vuelta 3?",
                    explicacion = "total = 0 + 1 + 2 + 3 = 6."
                ),
                EjercicioSemilla(
                    paso = PasoEjercicio.COMPROBACION,
                    tipo = TipoEjercicio.COMPLETAR_CODIGO,
                    enunciado = "Completa el código para que muestre los números del 1 al 5.",
                    codigo = """
                        n <- 1
                        Mientras n <= 5 Hacer
                            Escribir n
                            ____
                        FinMientras
                    """.trimIndent(),
                    opciones = listOf("n <- n + 1", "n <- n - 1", "n <- 5", "Escribir n + 1"),
                    correcta = 0,
                    pista1 = "Sin esa línea, ¿cambia n alguna vez?",
                    pista2 = "n tiene que acercarse a 6 para que la condición sea falsa.",
                    explicacion = "n <- n + 1 avanza el contador. Cuando n llega a 6, la condición es " +
                            "falsa y el ciclo termina."
                )
            )
        )
    )

    /**
     * Crea las actividades de ejemplo que todavía no existan.
     * onTerminar recibe cuántas actividades se crearon.
     */
    fun cargar(onTerminar: (Int) -> Unit, onError: (Exception) -> Unit) {

        val db = FirebaseFirestore.getInstance()

        db.collection("actividades")
            .get()
            .addOnSuccessListener { documentos ->

                val existentes = documentos.mapNotNull { it.getString("titulo") }.toSet()
                val nuevas = actividades.filter { it.titulo !in existentes }

                if (nuevas.isEmpty()) {
                    onTerminar(0)
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                nuevas.forEach { semilla ->

                    val refActividad = db.collection("actividades").document()

                    batch.set(
                        refActividad,
                        hashMapOf(
                            "titulo" to semilla.titulo,
                            "descripcion" to semilla.descripcion,
                            "tipo" to "OPCION_MULTIPLE",
                            "xp" to EvaluadorEjercicio.XP_COMPLETAR + EvaluadorEjercicio.XP_PRIMER_INTENTO,
                            "orden" to semilla.orden,
                            "activo" to true,
                            "modulo" to semilla.modulo,
                            "objetivo" to semilla.objetivo,
                            "nivel" to semilla.nivel.name,
                            "reto" to semilla.reto,
                            "concepto" to semilla.concepto,
                            "ejemploCodigo" to semilla.ejemploCodigo,
                            "ejemploExplicacion" to semilla.ejemploExplicacion
                        )
                    )

                    semilla.ejercicios.forEachIndexed { indice, ej ->
                        batch.set(
                            db.collection("ejercicios").document(),
                            hashMapOf(
                                "actividadId" to refActividad.id,
                                "paso" to ej.paso.name,
                                "tipo" to ej.tipo.name,
                                "enunciado" to ej.enunciado,
                                "codigo" to ej.codigo,
                                "opciones" to ej.opciones,
                                "respuestaCorrecta" to ej.correcta,
                                "pista1" to ej.pista1,
                                "pista2" to ej.pista2,
                                "explicacion" to ej.explicacion,
                                "orden" to indice + 1,
                                "activo" to true
                            )
                        )
                    }
                }

                batch.commit()
                    .addOnSuccessListener { onTerminar(nuevas.size) }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }
}
