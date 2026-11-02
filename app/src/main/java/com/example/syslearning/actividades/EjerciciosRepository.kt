package com.example.syslearning.actividades

import com.example.syslearning.model.Ejercicio
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Acceso a la colección "ejercicios". Las consultas usan un solo filtro
 * (actividadId) y se ordenan en el cliente, así no hace falta crear
 * índices compuestos en Firestore.
 */
object EjerciciosRepository {

    private const val COLECCION = "ejercicios"

    private val db get() = FirebaseFirestore.getInstance()

    /** Todos los ejercicios de una actividad, ordenados por "orden". */
    fun obtenerDeActividad(
        actividadId: String,
        soloActivos: Boolean,
        onSuccess: (List<Ejercicio>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection(COLECCION)
            .whereEqualTo("actividadId", actividadId)
            .get()
            .addOnSuccessListener { documentos ->

                val lista = documentos.mapNotNull { doc ->
                    runCatching {
                        doc.toObject(Ejercicio::class.java).apply { id = doc.id }
                    }.getOrNull()
                }
                    .filter { !soloActivos || it.activo }
                    .sortedBy { it.orden }

                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun guardar(
        ejercicio: Ejercicio,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val datos = hashMapOf<String, Any>(
            "actividadId" to ejercicio.actividadId,
            "paso" to ejercicio.paso,
            "tipo" to ejercicio.tipo,
            "enunciado" to ejercicio.enunciado,
            "codigo" to ejercicio.codigo,
            "opciones" to ejercicio.opciones,
            "respuestaCorrecta" to ejercicio.respuestaCorrecta,
            "pista1" to ejercicio.pista1,
            "pista2" to ejercicio.pista2,
            "explicacion" to ejercicio.explicacion,
            "orden" to ejercicio.orden,
            "activo" to ejercicio.activo
        )

        if (ejercicio.id.isBlank()) {
            db.collection(COLECCION)
                .add(datos)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
        } else {
            db.collection(COLECCION)
                .document(ejercicio.id)
                .set(datos)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
        }
    }

    fun eliminar(
        ejercicioId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLECCION)
            .document(ejercicioId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Elimina la actividad y todos sus ejercicios en una sola operación
     * (batch), para no dejar ejercicios huérfanos.
     */
    fun eliminarActividadConEjercicios(
        actividadId: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection(COLECCION)
            .whereEqualTo("actividadId", actividadId)
            .get()
            .addOnSuccessListener { documentos ->

                val batch = db.batch()

                documentos.forEach { batch.delete(it.reference) }
                batch.delete(db.collection("actividades").document(actividadId))

                batch.commit()
                    .addOnSuccessListener { onSuccess(documentos.size()) }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }
}
