package com.example.syslearning

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.syslearning.actividades.MicroActividadFragment
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.ActividadAdapter
import com.example.syslearning.model.EstadoActividad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de actividades del estudiante. Carga las actividades desde
 * Firestore (colección "actividades") y calcula el estado de cada una
 * (DISPONIBLE / BLOQUEADA / COMPLETADA) según el progreso guardado del
 * usuario actual, en vez de venir fijo desde el documento.
 */
class ActividadesFragment : Fragment(R.layout.fragment_actividades) {

    private lateinit var recycler: RecyclerView
    private lateinit var txtProgreso: TextView
    private lateinit var progresoGeneral: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvActividades)
        txtProgreso = view.findViewById(R.id.txtProgreso)
        progresoGeneral = view.findViewById(R.id.progresoGeneral)

        recycler.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        cargarActividades()
    }

    private fun cargarActividades() {

        // NOTA: esta consulta combina un whereEqualTo + orderBy en campos
        // distintos. La primera vez que corras esto, Firestore puede
        // pedirte crear un índice compuesto (te da un link en el error
        // de Logcat) — solo hay que hacer clic en ese link una vez.
        db.collection("actividades")
            .whereEqualTo("activo", true)
            .orderBy("orden")
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                val actividades = documentos.mapNotNull { doc ->
                    doc.toObject(Actividad::class.java).apply { id = doc.id }
                }

                aplicarProgresoDelUsuario(actividades)
            }
            .addOnFailureListener { e ->

                if (!isAdded) return@addOnFailureListener

                Log.e("ACTIVIDADES", "Error al cargar actividades", e)

                txtProgreso.text = e.localizedMessage

                Toast.makeText(
                    requireContext(),
                    e.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Marca cada actividad como COMPLETADA / DISPONIBLE / BLOQUEADA según
     * el progreso guardado del usuario en
     * usuarios/{uid}.actividadesProgreso (lo escribe SesionActividadesManager
     * al terminar una sesión). La primera actividad sin completar queda
     * como la disponible; las siguientes quedan bloqueadas.
     */
    private fun aplicarProgresoDelUsuario(actividades: List<Actividad>) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            mostrarActividades(marcarEstados(actividades, emptySet()))
            return
        }

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (!isAdded) return@addOnSuccessListener

                val progreso = documento.get("actividadesProgreso") as? Map<*, *>

                val idsCompletadas = progreso
                    ?.filterValues { valor -> (valor as? Map<*, *>)?.get("completado") == true }
                    ?.keys
                    ?.mapNotNull { it as? String }
                    ?.toSet()
                    ?: emptySet()

                mostrarActividades(marcarEstados(actividades, idsCompletadas))
            }
            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                mostrarActividades(marcarEstados(actividades, emptySet()))
            }
    }

    private fun marcarEstados(
        actividades: List<Actividad>,
        idsCompletadas: Set<String>
    ): List<Actividad> {

        var yaHayDisponible = false

        actividades.forEach { actividad ->

            when {
                idsCompletadas.contains(actividad.id) -> {
                    actividad.estado = EstadoActividad.COMPLETADA
                }
                !yaHayDisponible -> {
                    actividad.estado = EstadoActividad.DISPONIBLE
                    yaHayDisponible = true
                }
                else -> {
                    actividad.estado = EstadoActividad.BLOQUEADA
                }
            }
        }

        return actividades
    }

    private fun mostrarActividades(actividades: List<Actividad>) {

        recycler.adapter = ActividadAdapter(actividades) { actividad ->

            // Micro actividad de 7 pasos (reto, concepto, ejemplo, práctica,
            // retroalimentación, comprobación y recompensa).
            val fragment = MicroActividadFragment.newInstance(actividad.id)

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .addToBackStack(null)
                .commit()

        }

        actualizarProgresoEncabezado(actividades)
    }

    private fun actualizarProgresoEncabezado(actividades: List<Actividad>) {

        val completadas = actividades.count { it.estado == EstadoActividad.COMPLETADA }
        val total = actividades.size

        val porcentaje = if (total > 0) (completadas * 100) / total else 0

        progresoGeneral.max = 100
        progresoGeneral.progress = porcentaje
        txtProgreso.text = "$porcentaje% completado"
    }
}