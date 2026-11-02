package com.example.syslearning.actividades

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.syslearning.R
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.ActividadAdapter
import com.example.syslearning.model.EstadoActividad

/**
 * Actividades de un módulo en la cuadrícula estilo bento, con la barra de
 * avance del módulo. El reto final va al final y se desbloquea cuando se
 * completan las demás actividades.
 */
class ModuloActividadesFragment : Fragment(R.layout.fragment_modulo_actividades) {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ActividadAdapter

    private var numeroModulo = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        numeroModulo = arguments?.getInt(ARG_MODULO) ?: 1

        recycler = view.findViewById(R.id.rvActividadesModulo)
        recycler.layoutManager = nuevaCuadricula()
        recycler.isSaveEnabled = false

        adapter = ActividadAdapter(emptyList()) { actividad -> abrirActividad(actividad) }
        recycler.adapter = adapter

        view.findViewById<TextView>(R.id.btnVolverModulo).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cargar()
    }

    private fun cargar() {

        ModulosRepository.cargarAvance(
            requireContext(),
            onAvance = { avance ->

                if (!isAdded) return@cargarAvance

                val estado = avance.modulos.firstOrNull { it.modulo.numero == numeroModulo }

                if (estado == null) {
                    Toast.makeText(requireContext(), "Este módulo ya no tiene actividades", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                    return@cargarAvance
                }

                mostrar(estado)
            },
            onError = {
                if (!isAdded) return@cargarAvance
                Toast.makeText(requireContext(), "No se pudieron cargar las actividades", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun mostrar(estado: ProgresoModulos.EstadoModulo) {

        val raiz = view ?: return

        raiz.findViewById<TextView>(R.id.tvEtiquetaModuloDetalle).text = "MÓDULO ${estado.modulo.numero}"
        raiz.findViewById<TextView>(R.id.tvTituloModuloDetalle).text = estado.modulo.titulo
        raiz.findViewById<ProgressBar>(R.id.progresoModuloDetalle).progress = estado.porcentaje
        raiz.findViewById<TextView>(R.id.tvAvanceModuloDetalle).text =
            "${estado.completadas} de ${estado.total} actividades · ${estado.porcentaje} %" +
                    if (estado.completado) " · 🏅 Módulo completado" else ""

        val tvAviso = raiz.findViewById<TextView>(R.id.tvAvisoReto)
        val reto = estado.reto
        val pendientes = estado.actividades.count { it.estado != EstadoActividad.COMPLETADA }

        tvAviso.visibility = View.VISIBLE
        tvAviso.text = when {
            reto == null -> "Completa todas las actividades para terminar este módulo."
            reto.estado == EstadoActividad.COMPLETADA ->
                "🏆 Superaste el reto final «${reto.titulo}». ¡Módulo completado!"
            reto.estado == EstadoActividad.DISPONIBLE ->
                "🏆 ¡Reto final desbloqueado! Supéralo para abrir el siguiente módulo."
            else ->
                "🔒 Reto final: completa $pendientes actividad(es) más para desbloquearlo."
        }

        adapter.actualizar(estado.todas)

        // Acomodo nuevo para que la cuadrícula siempre empiece arriba.
        recycler.layoutManager = nuevaCuadricula()
    }

    private fun nuevaCuadricula() =
        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }

    private fun abrirActividad(actividad: Actividad) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, MicroActividadFragment.newInstance(actividad.id))
            .addToBackStack(null)
            .commit()
    }

    companion object {

        private const val ARG_MODULO = "modulo"

        fun newInstance(numeroModulo: Int) = ModuloActividadesFragment().apply {
            arguments = Bundle().apply { putInt(ARG_MODULO, numeroModulo) }
        }
    }
}
