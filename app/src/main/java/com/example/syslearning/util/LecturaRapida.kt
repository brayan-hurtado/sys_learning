package com.example.syslearning.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

/**
 * Lecturas de Firestore que no se quedan esperando cuando no hay internet
 * (RF 13 y RNF de rendimiento).
 *
 * Problema: get() normal intenta primero el servidor y, sin conexión,
 * espera a que se agote el tiempo (10 s o más) antes de usar la copia
 * guardada en el dispositivo.
 *
 * Solución:
 *  - Sin conexión: se lee directo de la caché (Source.CACHE), al instante.
 *  - Con conexión y [refrescar] = true: se muestra primero la caché y luego
 *    se actualiza con el servidor (el callback se llama dos veces).
 *  - Con conexión y [refrescar] = false: se lee del servidor una sola vez;
 *    si falla, se usa la caché.
 */
object LecturaRapida {

    /** true si hay una red con internet verificada (no solo wifi sin salida). */
    fun hayConexion(context: Context): Boolean {

        val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true

        val capacidades = gestor.getNetworkCapabilities(gestor.activeNetwork) ?: return false

        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * [onDatos] recibe el resultado y si vino de la caché (true) o del servidor (false).
     */
    fun consulta(
        context: Context,
        query: Query,
        refrescar: Boolean,
        onDatos: (QuerySnapshot, Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (!hayConexion(context)) {
            query.get(Source.CACHE)
                .addOnSuccessListener { onDatos(it, true) }
                .addOnFailureListener { onError(it) }
            return
        }

        if (!refrescar) {
            query.get()
                .addOnSuccessListener { onDatos(it, it.metadata.isFromCache) }
                .addOnFailureListener { error ->
                    query.get(Source.CACHE)
                        .addOnSuccessListener { onDatos(it, true) }
                        .addOnFailureListener { onError(error) }
                }
            return
        }

        var yaMostroCache = false

        query.get(Source.CACHE)
            .addOnSuccessListener { cache ->
                if (!cache.isEmpty) {
                    yaMostroCache = true
                    onDatos(cache, true)
                }
            }
            .addOnCompleteListener {
                query.get(Source.SERVER)
                    .addOnSuccessListener { onDatos(it, false) }
                    .addOnFailureListener { error ->
                        // Si ya se mostró la caché, el error del servidor no importa.
                        if (!yaMostroCache) onError(error)
                    }
            }
    }

    fun documento(
        context: Context,
        referencia: DocumentReference,
        onDatos: (DocumentSnapshot, Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (!hayConexion(context)) {
            referencia.get(Source.CACHE)
                .addOnSuccessListener { onDatos(it, true) }
                .addOnFailureListener { onError(it) }
            return
        }

        referencia.get()
            .addOnSuccessListener { onDatos(it, it.metadata.isFromCache) }
            .addOnFailureListener { error ->
                referencia.get(Source.CACHE)
                    .addOnSuccessListener { onDatos(it, true) }
                    .addOnFailureListener { onError(error) }
            }
    }
}
