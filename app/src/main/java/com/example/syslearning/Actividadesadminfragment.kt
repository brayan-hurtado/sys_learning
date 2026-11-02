package com.example.syslearning.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.AulaActual
import com.example.syslearning.R
import com.example.syslearning.actividades.ContenidoEjemplo
import com.example.syslearning.actividades.EjerciciosAdminFragment
import com.example.syslearning.actividades.EjerciciosRepository
import com.example.syslearning.adapters.ActividadAdminAdapter
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.NivelActividad
import com.example.syslearning.model.TipoActividad
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Panel de administrador: micro actividades (colección "actividades").
 *
 *  - Toca una actividad para administrar sus ejercicios.
 *  - Mantén presionado para: editar el contenido, ver la vista previa,
 *    revisar los resultados de los aprendices o eliminarla.
 */
class ActividadesAdminFragment : Fragment(R.layout.fragment_actividades_admin) {

    private lateinit var recycler: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var fabNuevaActividad: FloatingActionButton
    private lateinit var adapter: ActividadAdminAdapter

    private val db = FirebaseFirestore.getInstance()
    private val listaActividades = mutableListOf<Actividad>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvActividadesAdmin)
        tvVacio = view.findViewById(R.id.tvVacioActividadesAdmin)
        fabNuevaActividad = view.findViewById(R.id.fabNuevaActividad)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = ActividadAdminAdapter(
            listaActividades,
            onClick = { actividad -> abrirEjercicios(actividad) },
            onLongClick = { actividad -> mostrarMenuActividad(actividad) }
        )

        recycler.adapter = adapter

        fabNuevaActividad.setOnClickListener {
            mostrarDialogoActividad(null)
        }

        view.findViewById<MaterialButton>(R.id.btnCargarEjemplo).setOnClickListener {
            confirmarCargarEjemplo()
        }

        cargarActividades()
    }

    private fun abrirEjercicios(actividad: Actividad) {

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.frameAdmin,
                EjerciciosAdminFragment.newInstance(actividad.id, actividad.titulo)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun cargarActividades() {

        db.collection("actividades")
            .orderBy("orden")
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                listaActividades.clear()

                for (doc in documentos) {
                    val actividad = runCatching { doc.toObject(Actividad::class.java) }.getOrNull()
                        ?: continue
                    actividad.id = doc.id
                    listaActividades.add(actividad)
                }

                adapter.notifyDataSetChanged()

                tvVacio.visibility = if (listaActividades.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->

                if (!isAdded) return@addOnFailureListener

                Log.e("ACTIVIDADES", "Error", e)
                toast(e.localizedMessage ?: "Error al cargar actividades")
            }
    }

    // ==================================================================
    // Crear / editar (pasos 1 a 3 de la micro actividad)
    // ==================================================================

    private fun mostrarDialogoActividad(actividadExistente: Actividad?) {

        val vista = layoutInflater.inflate(R.layout.dialog_nueva_actividad, null)

        val tvTitulo = vista.findViewById<TextView>(R.id.tvTituloDialogoActividad)
        val etTitulo = vista.findViewById<TextInputEditText>(R.id.etTituloActividad)
        val etDescripcion = vista.findViewById<TextInputEditText>(R.id.etDescripcionActividad)
        val etModulo = vista.findViewById<TextInputEditText>(R.id.etModuloActividad)
        val spNivel = vista.findViewById<Spinner>(R.id.spNivelActividad)
        val etObjetivo = vista.findViewById<TextInputEditText>(R.id.etObjetivoActividad)
        val etReto = vista.findViewById<TextInputEditText>(R.id.etRetoActividad)
        val etConcepto = vista.findViewById<TextInputEditText>(R.id.etConceptoActividad)
        val tvContador = vista.findViewById<TextView>(R.id.tvContadorPalabras)
        val etEjemploCodigo = vista.findViewById<TextInputEditText>(R.id.etEjemploCodigoActividad)
        val etEjemploExplicacion = vista.findViewById<TextInputEditText>(R.id.etEjemploExplicacionActividad)
        val etXp = vista.findViewById<TextInputEditText>(R.id.etXpActividad)
        val etOrden = vista.findViewById<TextInputEditText>(R.id.etOrdenActividad)
        val switchActiva = vista.findViewById<SwitchMaterial>(R.id.switchActivaActividad)
        val btnCancelar = vista.findViewById<Button>(R.id.btnCancelarActividad)
        val btnGuardar = vista.findViewById<Button>(R.id.btnGuardarActividad)

        val niveles = NivelActividad.values()

        spNivel.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            niveles.map { it.etiqueta }
        )

        // Mayer: explicaciones cortas. Se avisa si el concepto pasa de 60 palabras.
        etConcepto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val palabras = contarPalabras(s?.toString() ?: "")
                tvContador.text = "$palabras / $MAX_PALABRAS_CONCEPTO palabras"
                tvContador.setTextColor(
                    if (palabras > MAX_PALABRAS_CONCEPTO) 0xFFC62828.toInt() else 0xFF616161.toInt()
                )
            }
        })

        if (actividadExistente != null) {
            tvTitulo.text = "Editar micro actividad"
            etTitulo.setText(actividadExistente.titulo)
            etDescripcion.setText(actividadExistente.descripcion)
            etModulo.setText(actividadExistente.modulo.toString())
            spNivel.setSelection(NivelActividad.desde(actividadExistente.nivel).ordinal)
            etObjetivo.setText(actividadExistente.objetivo)
            etReto.setText(actividadExistente.reto)
            etConcepto.setText(actividadExistente.concepto)
            etEjemploCodigo.setText(actividadExistente.ejemploCodigo)
            etEjemploExplicacion.setText(actividadExistente.ejemploExplicacion)
            etXp.setText(actividadExistente.xp.toString())
            etOrden.setText(actividadExistente.orden.toString())
            switchActiva.isChecked = actividadExistente.activo
        } else {
            etModulo.setText("1")
            etOrden.setText((listaActividades.size + 1).toString())
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(vista)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {

            val titulo = etTitulo.text.toString().trim()
            val concepto = etConcepto.text.toString().trim()
            val modulo = etModulo.text.toString().toIntOrNull() ?: 0

            if (titulo.isEmpty()) {
                etTitulo.error = "Ingrese un título"
                return@setOnClickListener
            }

            if (modulo !in 1..MAX_MODULOS) {
                etModulo.error = "El módulo va de 1 a $MAX_MODULOS"
                return@setOnClickListener
            }

            if (etReto.text.isNullOrBlank() || concepto.isEmpty()) {
                toast("El reto (paso 1) y el concepto (paso 2) son obligatorios")
                return@setOnClickListener
            }

            val datos = hashMapOf<String, Any>(
                "titulo" to titulo,
                "descripcion" to etDescripcion.text.toString().trim(),
                "tipo" to TipoActividad.OPCION_MULTIPLE.name,
                "xp" to (etXp.text.toString().toIntOrNull() ?: 25),
                "orden" to (etOrden.text.toString().toIntOrNull() ?: (listaActividades.size + 1)),
                "activo" to switchActiva.isChecked,
                "modulo" to modulo,
                "nivel" to niveles[spNivel.selectedItemPosition].name,
                "objetivo" to etObjetivo.text.toString().trim(),
                "reto" to etReto.text.toString().trim(),
                "concepto" to concepto,
                "ejemploCodigo" to etEjemploCodigo.text.toString().trimEnd(),
                "ejemploExplicacion" to etEjemploExplicacion.text.toString().trim()
            )

            val tarea = if (actividadExistente != null) {
                db.collection("actividades").document(actividadExistente.id).update(datos)
            } else {
                db.collection("actividades").document().set(datos)
            }

            tarea
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    toast(if (actividadExistente != null) "Actividad actualizada" else "Actividad creada")
                    cargarActividades()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    if (isAdded) toast("No se pudo guardar la actividad")
                }

            if (contarPalabras(concepto) > MAX_PALABRAS_CONCEPTO) {
                toast("Guardado. Sugerencia: el concepto tiene más de $MAX_PALABRAS_CONCEPTO palabras")
            }
        }

        dialog.show()
    }

    private fun contarPalabras(texto: String): Int =
        texto.trim().split(Regex("\\s+")).count { it.isNotBlank() }

    // ==================================================================
    // Menú: editar, vista previa, resultados, eliminar
    // ==================================================================

    private fun mostrarMenuActividad(actividad: Actividad) {

        val opciones = arrayOf(
            "🧩 Ejercicios",
            "✏️ Editar contenido",
            "👁️ Vista previa",
            "📊 Revisar resultados",
            "🗑️ Eliminar"
        )

        AlertDialog.Builder(requireContext())
            .setTitle(actividad.titulo)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirEjercicios(actividad)
                    1 -> mostrarDialogoActividad(actividad)
                    2 -> EjerciciosAdminFragment.abrirVistaPrevia(parentFragmentManager, actividad.id)
                    3 -> mostrarResultados(actividad)
                    4 -> confirmarEliminarActividad(actividad)
                }
            }
            .show()
    }

    /**
     * Resultados de la actividad (RF 10 y RF 15): cuántos aprendices la
     * intentaron, cuántos la dominaron y el semáforo de cada uno. Si hay
     * un aula seleccionada en el panel, solo cuenta a sus aprendices.
     */
    private fun mostrarResultados(actividad: Actividad) {

        db.collection("usuarios")
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                var intentaron = 0
                var dominaron = 0
                var verdes = 0
                var amarillos = 0
                var rojos = 0
                val porReforzar = mutableListOf<String>()

                documentos.forEach { doc ->

                    if (doc.getString("rol").equals("admin", ignoreCase = true)) return@forEach

                    if (AulaActual.hayFiltro()) {
                        val aulas = doc.get("aulas") as? List<*> ?: emptyList<Any>()
                        if (!aulas.contains(AulaActual.codigo)) return@forEach
                    }

                    val progreso = doc.get("actividadesProgreso") as? Map<*, *> ?: return@forEach
                    val entrada = progreso[actividad.id] as? Map<*, *> ?: return@forEach

                    intentaron++

                    val completado = entrada["completado"] == true
                    if (completado) dominaron++

                    when (entrada["ultimoSemaforo"] as? String) {
                        "VERDE" -> verdes++
                        "AMARILLO" -> amarillos++
                        "ROJO" -> rojos++
                    }

                    if (!completado) {
                        porReforzar.add(doc.getString("nombre") ?: doc.getString("correo") ?: doc.id)
                    }
                }

                val alcance = if (AulaActual.hayFiltro()) "Aula ${AulaActual.nombre}" else "Todos los aprendices"

                val mensaje = buildString {
                    append("$alcance\n\n")
                    if (intentaron == 0) {
                        append("Ningún aprendiz ha realizado esta actividad todavía.")
                    } else {
                        append("Intentaron: $intentaron\n")
                        append("La dominaron: $dominaron (${dominaron * 100 / intentaron} %)\n\n")
                        append("Último resultado (semáforo)\n")
                        append("🟢 Verde: $verdes\n")
                        append("🟡 Amarillo: $amarillos\n")
                        append("🔴 Rojo: $rojos\n")
                        if (porReforzar.isNotEmpty()) {
                            append("\nNecesitan refuerzo:\n")
                            append(porReforzar.take(15).joinToString("\n") { "• $it" })
                            if (porReforzar.size > 15) append("\n… y ${porReforzar.size - 15} más")
                        }
                    }
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Resultados: ${actividad.titulo}")
                    .setMessage(mensaje)
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
            .addOnFailureListener {
                if (isAdded) toast("No se pudieron cargar los resultados")
            }
    }

    private fun confirmarEliminarActividad(actividad: Actividad) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar micro actividad")
            .setMessage(
                "¿Eliminar \"${actividad.titulo}\" y todos sus ejercicios? " +
                        "Esta acción no se puede deshacer. El progreso que ya registraron " +
                        "los aprendices se conserva en su historial."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->

                EjerciciosRepository.eliminarActividadConEjercicios(
                    actividad.id,
                    onSuccess = { cantidad ->
                        if (!isAdded) return@eliminarActividadConEjercicios
                        toast("Actividad eliminada junto con $cantidad ejercicio(s)")
                        cargarActividades()
                    },
                    onError = {
                        if (isAdded) toast("No se pudo eliminar")
                    }
                )
            }
            .show()
    }

    // ==================================================================
    // Contenido de ejemplo
    // ==================================================================

    private fun confirmarCargarEjemplo() {

        AlertDialog.Builder(requireContext())
            .setTitle("Cargar contenido de ejemplo")
            .setMessage(
                "Se crearán 2 micro actividades de las fichas de diseño " +
                        "(1.1 ¿Qué es un algoritmo? y 2.4 Ciclos Para y Mientras) con sus " +
                        "ejercicios. Si ya existen, no se duplican."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cargar") { _, _ ->
                ContenidoEjemplo.cargar(
                    onTerminar = { creadas ->
                        if (!isAdded) return@cargar
                        toast(if (creadas == 0) "El contenido de ejemplo ya estaba cargado" else "Se crearon $creadas actividades")
                        cargarActividades()
                    },
                    onError = {
                        if (isAdded) toast("No se pudo cargar el ejemplo")
                    }
                )
            }
            .show()
    }

    private fun toast(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MAX_PALABRAS_CONCEPTO = 60
        private const val MAX_MODULOS = 5
    }
}
