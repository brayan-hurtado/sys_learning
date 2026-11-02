package com.example.syslearning

import com.example.syslearning.model.Pregunta
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Trae las preguntas de una actividad y arma una sesión aleatoria.
 */
object PreguntasRepository {

    fun obtenerSesion(
        db: FirebaseFirestore,
        actividadId: String,
        cantidad: Int = 10,
        onSuccess: (List<Pregunta>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection("preguntas")
            .whereEqualTo("actividadId", actividadId)
            .whereEqualTo("estado", true)
            .get()
            .addOnSuccessListener { documentos ->

                val todas = documentos.mapNotNull { doc ->
                    doc.toObject(Pregunta::class.java).apply { id = doc.id }
                }

                // Aleatoriza y toma como máximo "cantidad" preguntas,
                // así cada sesión se siente distinta.
                val sesion = todas.shuffled().take(cantidad)

                onSuccess(sesion)

            }
            .addOnFailureListener { onError(it) }

    }

}