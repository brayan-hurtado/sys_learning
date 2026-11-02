package com.example.syslearning.actividades

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Registro de eventos de aprendizaje (RF 14).
 *
 * Cada evento se guarda en usuarios/{uid}/eventos. Firestore en Android
 * tiene la persistencia sin conexión activada por defecto: si no hay
 * internet, el documento queda en la caché del dispositivo y se sube
 * solo cuando vuelve la conexión (RF 13).
 *
 * Marcas de tiempo:
 *  - timestampLocal: hora del dispositivo cuando ocurrió el evento.
 *  - timestampServidor: la pone Firestore al sincronizar. Si está vacío,
 *    el evento todavía no ha subido.
 */
object RegistroEventos {

    const val INICIO_ACTIVIDAD = "INICIO_ACTIVIDAD"
    const val PASO_VISTO = "PASO_VISTO"
    const val RESPUESTA = "RESPUESTA"
    const val FIN_ACTIVIDAD = "FIN_ACTIVIDAD"
    const val ABANDONO = "ABANDONO"

    fun registrar(tipoEvento: String, datos: Map<String, Any?>) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val evento = HashMap<String, Any?>(datos)
        evento["usuarioId"] = uid
        evento["tipoEvento"] = tipoEvento
        evento["timestampLocal"] = Timestamp.now()
        evento["timestampServidor"] = FieldValue.serverTimestamp()

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection("eventos")
            .add(evento)
    }

    fun respuesta(
        moduloId: Int,
        actividadId: String,
        ejercicioId: String,
        tipoEjercicio: String,
        paso: String,
        intento: Int,
        correcto: Boolean,
        respuesta: String,
        pistaVista: Boolean,
        tiempoMs: Long
    ) {
        registrar(
            RESPUESTA,
            mapOf(
                "moduloId" to moduloId,
                "actividadId" to actividadId,
                "ejercicioId" to ejercicioId,
                "tipoEjercicio" to tipoEjercicio,
                "paso" to paso,
                "intento" to intento,
                "correcto" to correcto,
                "respuesta" to respuesta,
                "pistaVista" to pistaVista,
                "tiempoMs" to tiempoMs
            )
        )
    }
}
