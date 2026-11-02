package com.example.syslearning.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.syslearning.AulaActual
import com.example.syslearning.R
import com.example.syslearning.actividades.ContenidoEjemplo
import com.example.syslearning.actividades.EjerciciosAdminFragment
import com.example.syslearning.actividades.EjerciciosRepository
import com.example.syslearning.actividades.ModulosAdminFragment
import com.example.syslearning.actividades.ModulosRepository
import com.example.syslearning.adapters.ActividadAdminAdapter
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.Modulo
import com.example.syslearning.model.NivelActividad
import com.example.syslearning.model.TipoActividad
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

    /** Módulos disponibles para asignar (Firestore + sugeridos). */
    private var modulos: List<Modulo> = Modulo.combinar(emptyList())

    // Imagen de portada elegida en el diálogo (se sube al guardar).
    private var uriImagenSeleccionada: Uri? = null
    private var imgPortadaDialogo: ImageView? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uriImagenSeleccionada = uri
                imgPortadaDialogo?.setImageURI(uri)
            }
        }

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

        view.findViewById<MaterialButton>(R.id.btnModulosAdmin).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameAdmin, ModulosAdminFragment())
                .addToBackStack(null)
                .commit()
        }

        ModulosRepository.obtenerModulos(null) { lista ->
            if (!isAdded) return@obtenerModulos
            modulos = lista
            adapter.nombresModulos = lista.associate { it.numero to it.titulo }
            adapter.notifyDataSetChanged()
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

                // Mismo criterio que CursosFragment: sin aula seleccionada se ven
                // todas; con aula, solo las globales y las de esa aula.
                val codigoAula = if (AulaActual.hayFiltro()) AulaActual.codigo else null

                for (doc in documentos) {
                    val actividad = runCatching { doc.toObject(Actividad::class.java) }.getOrNull()
                        ?: continue
                    actividad.id = doc.id

                    val esGlobal = actividad.aulasAsignadas.isEmpty()
                    val perteneceAlAula = codigoAula != null && actividad.aulasAsignadas.contains(codigoAula)

                    if (codigoAula == null || esGlobal || perteneceAlAula) {
                        listaActividades.add(actividad)
                    }
                }

                listaActividades.sortWith(compareBy({ it.modulo }, { it.orden }))
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
        val spModulo = vista.findViewById<Spinner>(R.id.spModuloActividad)
        val spNivel = vista.findViewById<Spinner>(R.id.spNivelActividad)
        val etObjetivo = vista.findViewById<TextInputEditText>(R.id.etObjetivoActividad)
        val etReto = vista.findViewById<TextInputEditText>(R.id.etRetoActividad)
        val etConcepto = vista.findViewById<TextInputEditText>(R.id.etConceptoActividad)
        val tvContador = vista.findViewById<TextView>(R.id.tvContadorPalabras)
        val etEjemploCodigo = vista.findViewById<TextInputEditText>(R.id.etEjemploCodigoActividad)
        val etEjemploExplicacion = vista.findViewById<TextInputEditText>(R.id.etEjemploExplicacionActividad)
        val imgPortada = vista.findViewById<ImageView>(R.id.imgPortadaActividad)
        val btnElegirImagen = vista.findViewById<MaterialButton>(R.id.btnElegirImagenActividad)
        val etSabiasQue = vista.findViewById<TextInputEditText>(R.id.etSabiasQueActividad)
        val etConsejo = vista.findViewById<TextInputEditText>(R.id.etConsejoActividad)
        val etXp = vista.findViewById<TextInputEditText>(R.id.etXpActividad)
        val etOrden = vista.findViewById<TextInputEditText>(R.id.etOrdenActividad)
        val switchActiva = vista.findViewById<SwitchMaterial>(R.id.switchActivaActividad)
        val btnCancelar = vista.findViewById<Button>(R.id.btnCancelarActividad)
        val btnGuardar = vista.findViewById<Button>(R.id.btnGuardarActividad)

        val niveles = NivelActividad.values()

        // Si la actividad está en un módulo oculto o sin crear, se agrega a
        // la lista para que al editarla no cambie de módulo sin querer.
        val opcionesModulo = modulos.toMutableList()
        if (actividadExistente != null && opcionesModulo.none { it.numero == actividadExistente.modulo }) {
            opcionesModulo.add(Modulo(numero = actividadExistente.modulo, titulo = "(oculto o sin crear)"))
            opcionesModulo.sortBy { it.numero }
        }

        spModulo.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            opcionesModulo.map { it.nombreCompleto() }
        )

        uriImagenSeleccionada = null
        imgPortadaDialogo = imgPortada

        btnElegirImagen.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

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
            seleccionarModulo(spModulo, opcionesModulo, actividadExistente.modulo)
            spNivel.setSelection(NivelActividad.desde(actividadExistente.nivel).ordinal)
            etObjetivo.setText(actividadExistente.objetivo)
            etReto.setText(actividadExistente.reto)
            etConcepto.setText(actividadExistente.concepto)
            etEjemploCodigo.setText(actividadExistente.ejemploCodigo)
            etEjemploExplicacion.setText(actividadExistente.ejemploExplicacion)
            etSabiasQue.setText(actividadExistente.sabiasQue)
            etConsejo.setText(actividadExistente.consejo)
            if (actividadExistente.imagen.isNotBlank()) {
                Glide.with(this)
                    .load(actividadExistente.imagen)
                    .placeholder(R.drawable.img_programacion)
                    .into(imgPortada)
            }
            etXp.setText(actividadExistente.xp.toString())
            etOrden.setText(actividadExistente.orden.toString())
            switchActiva.isChecked = actividadExistente.activo
        } else {
            seleccionarModulo(spModulo, opcionesModulo, opcionesModulo.firstOrNull()?.numero ?: 1)
            etOrden.setText((listaActividades.size + 1).toString())

            if (AulaActual.hayFiltro()) {
                tvTitulo.text = "Nueva micro actividad\n🏫 Solo para el aula ${AulaActual.nombre}"
            } else {
                tvTitulo.text = "Nueva micro actividad\n🌐 Global: la verán todas las aulas"
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(vista)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {

            val titulo = etTitulo.text.toString().trim()
            val concepto = etConcepto.text.toString().trim()
            val modulo = opcionesModulo.getOrNull(spModulo.selectedItemPosition)?.numero ?: 0

            if (titulo.isEmpty()) {
                etTitulo.error = "Ingrese un título"
                return@setOnClickListener
            }

            if (modulo <= 0) {
                toast("Elige el módulo al que pertenece la actividad")
                return@setOnClickListener
            }

            // Solo un reto final por módulo: se avisa si ya hay otro.
            val esReto = niveles[spNivel.selectedItemPosition] == NivelActividad.INTEGRACION
            val otroReto = listaActividades.firstOrNull {
                it.id != actividadExistente?.id && it.modulo == modulo &&
                        NivelActividad.desde(it.nivel) == NivelActividad.INTEGRACION
            }
            if (esReto && otroReto != null) {
                toast("Ojo: el módulo $modulo ya tiene el reto «${otroReto.titulo}». Se usará como reto el último en orden.")
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
                "ejemploExplicacion" to etEjemploExplicacion.text.toString().trim(),
                "sabiasQue" to etSabiasQue.text.toString().trim(),
                "consejo" to etConsejo.text.toString().trim()
            )

            // Al crear sin imagen se guarda vacía (se usa la imagen por defecto).
            // Al editar sin elegir otra, se conserva la que ya tenía.
            if (actividadExistente == null) datos["imagen"] = ""

            // Al crearla con un aula seleccionada, queda solo para esa aula
            // (igual que NuevoCurso). Al editar se conserva su visibilidad;
            // se cambia con la opción "Cambiar visibilidad" del menú.
            if (actividadExistente == null) {
                datos["aulasAsignadas"] =
                    if (AulaActual.hayFiltro()) listOf(AulaActual.codigo) else emptyList<String>()
            }

            btnGuardar.isEnabled = false

            val uri = uriImagenSeleccionada

            if (uri != null) {
                toast("Subiendo imagen…")
                subirImagen(
                    uri,
                    onSuccess = { url ->
                        datos["imagen"] = url
                        guardarActividad(actividadExistente, datos, dialog, btnGuardar)
                    },
                    onError = {
                        btnGuardar.isEnabled = true
                        if (isAdded) toast("No se pudo subir la imagen: ${it.localizedMessage}")
                    }
                )
            } else {
                guardarActividad(actividadExistente, datos, dialog, btnGuardar)
            }

            if (contarPalabras(concepto) > MAX_PALABRAS_CONCEPTO) {
                toast("Guardado. Sugerencia: el concepto tiene más de $MAX_PALABRAS_CONCEPTO palabras")
            }
        }

        dialog.show()
    }

    private fun guardarActividad(
        actividadExistente: Actividad?,
        datos: HashMap<String, Any>,
        dialog: AlertDialog,
        btnGuardar: Button
    ) {

        val tarea = if (actividadExistente != null) {
            db.collection("actividades").document(actividadExistente.id).update(datos)
        } else {
            db.collection("actividades").document().set(datos)
        }

        tarea
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                toast(if (actividadExistente != null) "Actividad actualizada" else "Actividad creada")
                imgPortadaDialogo = null
                cargarActividades()
                dialog.dismiss()
            }
            .addOnFailureListener {
                btnGuardar.isEnabled = true
                if (isAdded) toast("No se pudo guardar la actividad")
            }
    }

    /** Sube la portada a Firebase Storage, en la misma carpeta que las de los cursos. */
    private fun subirImagen(uri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {

        val referencia = FirebaseStorage.getInstance().reference
            .child("imagenesCursos/actividad_${System.currentTimeMillis()}.jpg")

        referencia.putFile(uri)
            .continueWithTask { tarea ->
                if (!tarea.isSuccessful) throw tarea.exception ?: Exception("Error al subir")
                referencia.downloadUrl
            }
            .addOnSuccessListener { url -> onSuccess(url.toString()) }
            .addOnFailureListener { onError(it) }
    }

    private fun seleccionarModulo(spinner: Spinner, opciones: List<Modulo>, numero: Int) {
        val indice = opciones.indexOfFirst { it.numero == numero }
        if (indice >= 0) spinner.setSelection(indice)
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
            "🏫 Cambiar visibilidad",
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
                    4 -> cambiarVisibilidad(actividad)
                    5 -> confirmarEliminarActividad(actividad)
                }
            }
            .show()
    }

    /**
     * Define quién ve la actividad: todas las aulas (global) o solo el aula
     * seleccionada en el panel. Sirve también para corregir actividades
     * creadas antes de que existiera la asignación por aula.
     */
    private fun cambiarVisibilidad(actividad: Actividad) {

        val actual = if (actividad.aulasAsignadas.isEmpty()) {
            "Ahora: 🌐 global (todas las aulas)"
        } else {
            "Ahora: 🏫 solo ${actividad.aulasAsignadas.joinToString(", ")}"
        }

        val opciones = mutableListOf("🌐 Global: todas las aulas")
        if (AulaActual.hayFiltro()) {
            opciones.add("🏫 Solo el aula ${AulaActual.nombre} (${AulaActual.codigo})")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("$actual")
            .setItems(opciones.toTypedArray()) { _, which ->

                val nuevas = if (which == 0) emptyList() else listOf(AulaActual.codigo)

                db.collection("actividades")
                    .document(actividad.id)
                    .update("aulasAsignadas", nuevas)
                    .addOnSuccessListener {
                        if (!isAdded) return@addOnSuccessListener
                        toast(if (nuevas.isEmpty()) "Ahora es global" else "Ahora es solo para ${AulaActual.nombre}")
                        cargarActividades()
                    }
                    .addOnFailureListener {
                        if (isAdded) toast("No se pudo cambiar la visibilidad")
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()

        if (!AulaActual.hayFiltro()) {
            toast("Para asignarla a un aula, primero selecciona el aula en «Aulas»")
        }
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
                        "ejercicios. Si ya existen, no se duplican: solo se completan las tarjetas «¿Sabías que?» y «Consejo»."
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
        private const val MAX_MODULOS_SIN_USO = 0
    }
}
