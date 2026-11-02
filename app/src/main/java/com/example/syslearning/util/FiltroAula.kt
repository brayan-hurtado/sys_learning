package com.example.syslearning.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Regla única de visibilidad por aula para cursos y actividades.
 *
 *  - aulasAsignadas vacío  -> contenido GLOBAL: lo ven todos.
 *  - aulasAsignadas con códigos -> solo lo ven los estudiantes que
 *    pertenecen a alguna de esas aulas (usuarios/{uid}.aulas).
 *
 * El administrador asigna el aula al crear el contenido con un aula
 * seleccionada en el panel (AulaActual).
 */
object FiltroAula {

    fun esVisible(aulasAsignadas: List<String>, misAulas: Collection<String>): Boolean {
        return aulasAsignadas.isEmpty() || aulasAsignadas.any { it in misAulas }
    }

    /** Códigos de aula guardados en el documento del usuario. */
    fun aulasDe(documentoUsuario: DocumentSnapshot): Set<String> {
        return (documentoUsuario.get("aulas") as? List<*>)
            ?.mapNotNull { it as? String }
            ?.toSet()
            ?: emptySet()
    }

    /** Lee las aulas del usuario actual. Si falla, devuelve vacío (solo verá lo global). */
    fun obtenerMisAulas(onResultado: (Set<String>) -> Unit) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onResultado(emptySet())
            return
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { onResultado(aulasDe(it)) }
            .addOnFailureListener { onResultado(emptySet()) }
    }
}
