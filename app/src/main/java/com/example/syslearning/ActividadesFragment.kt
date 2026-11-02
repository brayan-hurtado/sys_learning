package com.example.syslearning

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.actividades.ModuloActividadesFragment
import com.example.syslearning.actividades.ModulosRepository
import com.example.syslearning.actividades.ProgresoModulos.EstadoModulo
import com.example.syslearning.model.ModuloAdapter
import com.example.syslearning.util.LecturaRapida

/**
 * Pantalla de actividades del estudiante, organizada por módulos.
 * Cada módulo muestra su barra de avance y el estado de su reto final;
 * al tocarlo se abren sus actividades (ModuloActividadesFragment).
 *
 * Las reglas de desbloqueo están en ProgresoModulos.
 */
class ActividadesFragment : Fragment(R.layout.fragment_actividades) {

    private lateinit var recycler: RecyclerView
    private lateinit var txtProgreso: TextView
    private lateinit var progresoGeneral: ProgressBar
    private lateinit var adapter: ModuloAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvActividades)
        txtProgreso = view.findViewById(R.id.txtProgreso)
        progresoGeneral = view.findViewById(R.id.progresoGeneral)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Al volver con "atrás" la lista empieza arriba.
        recycler.isSaveEnabled = false

        adapter = ModuloAdapter { estado -> abrirModulo(estado) }
        recycler.adapter = adapter

        cargar()
    }

    private fun cargar() {

        txtProgreso.text = "Cargando…"

        ModulosRepository.cargarAvance(
            requireContext(),
            onAvance = { avance ->

                if (!isAdded) return@cargarAvance

                if (avance.modulos.isEmpty()) {
                    txtProgreso.text = if (avance.desdeCache) {
                        "Sin conexión: conéctate una vez para descargar las actividades"
                    } else {
                        "Todavía no hay actividades"
                    }
                    adapter.actualizar(emptyList())
                    return@cargarAvance
                }

                adapter.actualizar(avance.modulos)
                recycler.scrollToPosition(0)

                val porcentaje = if (avance.total > 0) avance.completadas * 100 / avance.total else 0
                progresoGeneral.max = 100
                progresoGeneral.progress = porcentaje

                val modulosCompletos = avance.modulos.count { it.completado }
                txtProgreso.text =
                    "$porcentaje% completado · $modulosCompletos de ${avance.modulos.size} módulos"

                if (!avance.desdeCache && LecturaRapida.hayConexion(requireContext())) {
                    ModulosRepository.descargarParaUsoSinConexion(
                        requireContext(),
                        avance.modulos.flatMap { it.todas }
                    )
                }
            },
            onError = { e ->

                if (!isAdded) return@cargarAvance

                Log.e("ACTIVIDADES", "Error al cargar actividades", e)
                txtProgreso.text = "No se pudieron cargar las actividades"
                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun abrirModulo(estado: EstadoModulo) {

        if (!estado.desbloqueado) {
            Toast.makeText(
                requireContext(),
                "🔒 Supera el reto final del módulo anterior para desbloquear este módulo",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, ModuloActividadesFragment.newInstance(estado.modulo.numero))
            .addToBackStack(null)
            .commit()
    }
}
