package com.example.syslearning.actividades

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.Ejercicio
import com.example.syslearning.model.EjercicioAdminAdapter
import com.example.syslearning.model.PasoEjercicio
import com.example.syslearning.model.TipoEjercicio
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Administración de los ejercicios de una micro actividad (pasos 4, 5 y 6).
 * Toca un ejercicio para editarlo; mantén presionado para duplicarlo o
 * eliminarlo. "Vista previa" recorre la actividad como la ve el aprendiz.
 */
class EjerciciosAdminFragment : Fragment(R.layout.fragment_ejercicios_admin) {

    private lateinit var recycler: RecyclerView
    private lateinit var tvResumen: TextView
    private lateinit var tvAviso: TextView
    private lateinit var adapter: EjercicioAdminAdapter

    private val lista = mutableListOf<Ejercicio>()

    private var actividadId = ""
    private var actividadTitulo = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actividadId = arguments?.getString(ARG_ID) ?: ""
        actividadTitulo = arguments?.getString(ARG_TITULO) ?: ""

        view.findViewById<TextView>(R.id.tvTituloEjerciciosAdmin).text = "Ejercicios: $actividadTitulo"
        tvResumen = view.findViewById(R.id.tvResumenEjerciciosAdmin)
        tvAviso = view.findViewById(R.id.tvAvisoEjerciciosAdmin)
        recycler = view.findViewById(R.id.rvEjerciciosAdmin)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = EjercicioAdminAdapter(
            lista,
            onClick = { mostrarDialogo(it) },
            onLongClick = { mostrarMenu(it) }
        )
        recycler.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fabNuevoEjercicio).setOnClickListener {
            mostrarDialogo(null)
        }

        view.findViewById<MaterialButton>(R.id.btnVistaPreviaEjercicios).setOnClickListener {
            abrirVistaPrevia(parentFragmentManager, actividadId)
        }

        cargar()
    }

    private fun cargar() {

        EjerciciosRepository.obtenerDeActividad(
            actividadId = actividadId,
            soloActivos = false,
            onSuccess = { ejercicios ->

                if (!isAdded) return@obtenerDeActividad

                lista.clear()
                lista.addAll(ejercicios.sortedWith(compareBy({ it.pasoEjercicio().ordinal }, { it.orden })))
                adapter.notifyDataSetChanged()

                actualizarResumen()
            },
            onError = {
                if (isAdded) toast("No se pudieron cargar los ejercicios")
            }
        )
    }

    /** Revisión rápida: ¿la actividad cumple la estructura de 7 pasos? */
    private fun actualizarResumen() {

        val activos = lista.filter { it.activo }
        val practicas = activos.count { it.pasoEjercicio() == PasoEjercicio.PRACTICA }
        val comprobaciones = activos.count { it.pasoEjercicio() == PasoEjercicio.COMPROBACION }

        tvResumen.text = "$practicas de práctica · $comprobaciones de comprobación (activos)"

        val avisos = mutableListOf<String>()

        if (practicas == 0) avisos.add("Agrega al menos un ejercicio de práctica (paso 4).")
        if (comprobaciones == 0) {
            avisos.add("Agrega al menos un ejercicio de comprobación (paso 6). Sin él, se evalúa con la última práctica.")
        } else if (comprobaciones == 1) {
            avisos.add("Con 2 o más comprobaciones, cada reintento muestra un ejercicio distinto.")
        }
        if (practicas > 2) avisos.add("Más de 2 prácticas puede pasar de los 5 minutos recomendados.")

        tvAviso.visibility = if (avisos.isEmpty()) View.GONE else View.VISIBLE
        tvAviso.text = avisos.joinToString("\n") { "• $it" }
    }

    // ==================================================================
    // Crear / editar
    // ==================================================================

    private fun mostrarDialogo(existente: Ejercicio?) {

        val vista = layoutInflater.inflate(R.layout.dialog_ejercicio, null)

        val tvTitulo = vista.findViewById<TextView>(R.id.tvTituloDialogoEjercicio)
        val spPaso = vista.findViewById<Spinner>(R.id.spPasoEjercicio)
        val spTipo = vista.findViewById<Spinner>(R.id.spTipoEjercicio)
        val etEnunciado = vista.findViewById<TextInputEditText>(R.id.etEnunciadoEjercicio)
        val etCodigo = vista.findViewById<TextInputEditText>(R.id.etCodigoEjercicio)
        val grupoOpciones = vista.findViewById<LinearLayout>(R.id.grupoOpcionesEjercicio)
        val etOpciones = vista.findViewById<TextInputEditText>(R.id.etOpcionesEjercicio)
        val tvAyuda = vista.findViewById<TextView>(R.id.tvAyudaOpciones)
        val etRespuesta = vista.findViewById<TextInputEditText>(R.id.etRespuestaEjercicio)
        val etPista1 = vista.findViewById<TextInputEditText>(R.id.etPista1Ejercicio)
        val etPista2 = vista.findViewById<TextInputEditText>(R.id.etPista2Ejercicio)
        val etExplicacion = vista.findViewById<TextInputEditText>(R.id.etExplicacionEjercicio)
        val etOrden = vista.findViewById<TextInputEditText>(R.id.etOrdenEjercicio)
        val switchActivo = vista.findViewById<SwitchMaterial>(R.id.switchActivoEjercicio)
        val btnCancelar = vista.findViewById<Button>(R.id.btnCancelarEjercicio)
        val btnGuardar = vista.findViewById<Button>(R.id.btnGuardarEjercicio)

        val contenedorRespuesta = vista.findViewById<TextInputLayout>(R.id.tilRespuestaEjercicio)

        val pasos = PasoEjercicio.values()
        val tipos = TipoEjercicio.values()

        spPaso.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            pasos.map { it.etiqueta }
        )

        spTipo.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            tipos.map { "${it.sigla} · ${it.etiqueta}" }
        )

        fun ajustarCamposSegunTipo() {

            val tipo = tipos[spTipo.selectedItemPosition]

            tvAyuda.text = tipo.ayudaOpciones

            grupoOpciones.visibility =
                if (tipo == TipoEjercicio.VERDADERO_FALSO) View.GONE else View.VISIBLE

            contenedorRespuesta.visibility =
                if (tipo == TipoEjercicio.ORDENAR_INSTRUCCIONES) View.GONE else View.VISIBLE

            contenedorRespuesta.hint = when (tipo) {
                TipoEjercicio.VERDADERO_FALSO -> "Respuesta correcta: 1 = Verdadero, 2 = Falso"
                TipoEjercicio.ENCONTRAR_ERROR -> "Número de la línea que tiene el error"
                else -> "Número de la opción correcta (1, 2, 3…)"
            }
        }

        spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ajustarCamposSegunTipo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        if (existente != null) {
            tvTitulo.text = "Editar ejercicio"
            spPaso.setSelection(existente.pasoEjercicio().ordinal)
            spTipo.setSelection(existente.tipoEjercicio().ordinal)
            etEnunciado.setText(existente.enunciado)
            etCodigo.setText(existente.codigo)
            etOpciones.setText(existente.opciones.joinToString("\n"))
            etRespuesta.setText((existente.respuestaCorrecta + 1).toString())
            etPista1.setText(existente.pista1)
            etPista2.setText(existente.pista2)
            etExplicacion.setText(existente.explicacion)
            etOrden.setText(existente.orden.toString())
            switchActivo.isChecked = existente.activo
        } else {
            etOrden.setText((lista.size + 1).toString())
            etRespuesta.setText("1")
        }

        ajustarCamposSegunTipo()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(vista)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {

            val tipo = tipos[spTipo.selectedItemPosition]

            val opciones = if (tipo == TipoEjercicio.VERDADERO_FALSO) {
                listOf("Verdadero", "Falso")
            } else {
                etOpciones.text.toString()
                    .lines()
                    .map { it.trimEnd() }
                    .filter { it.isNotBlank() }
            }

            val respuesta = if (tipo == TipoEjercicio.ORDENAR_INSTRUCCIONES) {
                0
            } else {
                (etRespuesta.text.toString().toIntOrNull() ?: 0) - 1
            }

            val ejercicio = Ejercicio(
                id = existente?.id ?: "",
                actividadId = actividadId,
                paso = pasos[spPaso.selectedItemPosition].name,
                tipo = tipo.name,
                enunciado = etEnunciado.text.toString().trim(),
                codigo = etCodigo.text.toString().trimEnd(),
                opciones = opciones,
                respuestaCorrecta = respuesta,
                pista1 = etPista1.text.toString().trim(),
                pista2 = etPista2.text.toString().trim(),
                explicacion = etExplicacion.text.toString().trim(),
                orden = etOrden.text.toString().toIntOrNull() ?: (lista.size + 1),
                activo = switchActivo.isChecked
            )

            val error = EvaluadorEjercicio.validarParaGuardar(ejercicio)

            if (error != null) {
                toast(error)
                return@setOnClickListener
            }

            EjerciciosRepository.guardar(
                ejercicio,
                onSuccess = {
                    if (!isAdded) return@guardar
                    toast(if (existente == null) "Ejercicio creado" else "Ejercicio actualizado")
                    dialog.dismiss()
                    cargar()
                },
                onError = { if (isAdded) toast("No se pudo guardar el ejercicio") }
            )
        }

        dialog.show()
    }

    // ==================================================================
    // Duplicar / eliminar
    // ==================================================================

    private fun mostrarMenu(ejercicio: Ejercicio) {

        val opciones = arrayOf("✏️ Editar", "📄 Duplicar (para otra comprobación)", "🗑️ Eliminar")

        AlertDialog.Builder(requireContext())
            .setTitle(ejercicio.enunciado.take(60))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogo(ejercicio)
                    1 -> duplicar(ejercicio)
                    2 -> confirmarEliminar(ejercicio)
                }
            }
            .show()
    }

    private fun duplicar(ejercicio: Ejercicio) {

        val copia = ejercicio.copy(
            id = "",
            enunciado = ejercicio.enunciado + " (copia)",
            orden = lista.size + 1,
            activo = false
        )

        EjerciciosRepository.guardar(
            copia,
            onSuccess = {
                if (!isAdded) return@guardar
                toast("Copia creada como oculta: edítala y actívala")
                cargar()
            },
            onError = { if (isAdded) toast("No se pudo duplicar") }
        )
    }

    private fun confirmarEliminar(ejercicio: Ejercicio) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Eliminar \"${ejercicio.enunciado.take(80)}\"? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                EjerciciosRepository.eliminar(
                    ejercicio.id,
                    onSuccess = {
                        if (!isAdded) return@eliminar
                        toast("Ejercicio eliminado")
                        cargar()
                    },
                    onError = { if (isAdded) toast("No se pudo eliminar") }
                )
            }
            .show()
    }

    private fun toast(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    companion object {

        private const val ARG_ID = "actividadId"
        private const val ARG_TITULO = "actividadTitulo"

        fun newInstance(actividadId: String, actividadTitulo: String) =
            EjerciciosAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, actividadId)
                    putString(ARG_TITULO, actividadTitulo)
                }
            }

        /** Abre la actividad tal como la ve el aprendiz, sin registrar nada. */
        fun abrirVistaPrevia(fragmentManager: androidx.fragment.app.FragmentManager, actividadId: String) {
            fragmentManager.beginTransaction()
                .replace(R.id.frameAdmin, MicroActividadFragment.newInstance(actividadId, vistaPrevia = true))
                .addToBackStack(null)
                .commit()
        }
    }
}
