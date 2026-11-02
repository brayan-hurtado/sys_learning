package com.example.syslearning.actividades

import android.content.Context
import com.bumptech.glide.Glide
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.Modulo
import com.example.syslearning.util.FiltroAula
import com.example.syslearning.util.LecturaRapida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Módulos (colección "modulos") y carga del avance del estudiante por
 * módulo. Sin internet usa la copia del dispositivo.
 */
object ModulosRepository {

    private const val COLECCION = "modulos"

    private val db get() = FirebaseFirestore.getInstance()

    // ==================================================================
    // Módulos
    // ==================================================================

    /** Módulos activos (los de Firestore + los sugeridos que no existan). */
    fun obtenerModulos(context: Context?, onLista: (List<Modulo>) -> Unit) {

        val consulta = db.collection(COLECCION)

        val procesar = { documentos: com.google.firebase.firestore.QuerySnapshot ->
            val propios = documentos.mapNotNull { doc ->
                runCatching { doc.toObject(Modulo::class.java).apply { id = doc.id } }.getOrNull()
            }
            onLista(Modulo.combinar(propios))
        }

        if (context != null) {
            LecturaRapida.consulta(
                context = context,
                query = consulta,
                refrescar = false,
                onDatos = { documentos, _ -> procesar(documentos) },
                // Si no hay reglas o conexión, se usan los módulos sugeridos.
                onError = { onLista(Modulo.combinar(emptyList())) }
            )
        } else {
            consulta.get()
                .addOnSuccessListener { procesar(it) }
                .addOnFailureListener { onLista(Modulo.combinar(emptyList())) }
        }
    }

    /** Todos los módulos guardados en Firestore (incluye inactivos), para el panel. */
    fun obtenerModulosAdmin(onLista: (List<Modulo>) -> Unit, onError: (Exception) -> Unit) {
        db.collection(COLECCION)
            .get()
            .addOnSuccessListener { documentos ->
                onLista(
                    documentos.mapNotNull { doc ->
                        runCatching { doc.toObject(Modulo::class.java).apply { id = doc.id } }.getOrNull()
                    }.sortedBy { it.numero }
                )
            }
            .addOnFailureListener { onError(it) }
    }

    fun guardarModulo(modulo: Modulo, onSuccess: () -> Unit, onError: (Exception) -> Unit) {

        val datos = hashMapOf<String, Any>(
            "numero" to modulo.numero,
            "titulo" to modulo.titulo,
            "descripcion" to modulo.descripcion,
            "activo" to modulo.activo
        )

        val referencia = if (modulo.id.isBlank()) {
            db.collection(COLECCION).document()
        } else {
            db.collection(COLECCION).document(modulo.id)
        }

        referencia.set(datos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun eliminarModulo(moduloId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection(COLECCION).document(moduloId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /** Crea en Firestore los módulos sugeridos que todavía no existan. */
    fun crearSugeridos(existentes: List<Modulo>, onTerminar: (Int) -> Unit, onError: (Exception) -> Unit) {

        val numeros = existentes.map { it.numero }.toSet()
        val faltantes = Modulo.SUGERIDOS.filter { it.numero !in numeros }

        if (faltantes.isEmpty()) {
            onTerminar(0)
            return
        }

        val batch = db.batch()
        faltantes.forEach { m ->
            batch.set(
                db.collection(COLECCION).document(),
                hashMapOf(
                    "numero" to m.numero,
                    "titulo" to m.titulo,
                    "descripcion" to m.descripcion,
                    "activo" to true
                )
            )
        }
        batch.commit()
            .addOnSuccessListener { onTerminar(faltantes.size) }
            .addOnFailureListener { onError(it) }
    }

    // ==================================================================
    // Avance del estudiante
    // ==================================================================

    data class Avance(
        val modulos: List<ProgresoModulos.EstadoModulo>,
        val desdeCache: Boolean
    ) {
        val total: Int get() = modulos.sumOf { it.total }
        val completadas: Int get() = modulos.sumOf { it.completadas }
    }

    /**
     * Pide al mismo tiempo los módulos, las actividades activas y el
     * progreso del estudiante, filtra por sus aulas y calcula el estado de
     * cada módulo y actividad.
     */
    fun cargarAvance(context: Context, onAvance: (Avance) -> Unit, onError: (Exception) -> Unit) {

        var modulos: List<Modulo>? = null
        var actividades: List<Actividad>? = null
        var desdeCache = false
        var usuario: com.google.firebase.firestore.DocumentSnapshot? = null
        var usuarioListo = false
        var fallo = false

        fun intentar() {
            if (fallo) return
            val m = modulos ?: return
            val a = actividades ?: return
            if (!usuarioListo) return

            val misAulas = usuario?.let { FiltroAula.aulasDe(it) } ?: emptySet()
            val visibles = a.filter { FiltroAula.esVisible(it.aulasAsignadas, misAulas) }

            val progreso = usuario?.get("actividadesProgreso") as? Map<*, *>
            val idsCompletadas = progreso
                ?.filterValues { valor -> (valor as? Map<*, *>)?.get("completado") == true }
                ?.keys?.mapNotNull { it as? String }?.toSet()
                ?: emptySet()

            onAvance(Avance(ProgresoModulos.calcular(m, visibles, idsCompletadas), desdeCache))
        }

        obtenerModulos(context) { lista ->
            modulos = lista
            intentar()
        }

        LecturaRapida.consulta(
            context = context,
            query = db.collection("actividades").whereEqualTo("activo", true),
            refrescar = false,
            onDatos = { documentos, cache ->
                desdeCache = cache
                actividades = documentos.mapNotNull { doc ->
                    runCatching { doc.toObject(Actividad::class.java).apply { id = doc.id } }.getOrNull()
                }
                intentar()
            },
            onError = { error ->
                fallo = true
                onError(error)
            }
        )

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            usuarioListo = true
            intentar()
        } else {
            LecturaRapida.documento(
                context = context,
                referencia = db.collection("usuarios").document(uid),
                onDatos = { doc, _ ->
                    usuario = doc
                    usuarioListo = true
                    intentar()
                },
                onError = {
                    usuarioListo = true
                    intentar()
                }
            )
        }
    }

    /**
     * Con conexión, deja guardados en el dispositivo los ejercicios y las
     * portadas, para poder abrir las actividades sin internet.
     */
    fun descargarParaUsoSinConexion(context: Context, actividades: List<Actividad>) {

        db.collection("ejercicios").whereEqualTo("activo", true).get()

        val contextoApp = context.applicationContext
        actividades
            .filter { it.imagen.isNotBlank() }
            .forEach { Glide.with(contextoApp).load(it.imagen).preload() }
    }
}
