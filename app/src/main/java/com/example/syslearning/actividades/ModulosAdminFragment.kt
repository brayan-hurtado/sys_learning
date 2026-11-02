package com.example.syslearning.actividades

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.R
import com.example.syslearning.model.Modulo
import com.example.syslearning.model.NivelActividad
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Panel admin de módulos (colección "modulos").
 *
 * - Crear (número, título, descripción, visible).
 * - Editar título, descripción y visibilidad. El número no cambia porque
 *   las actividades se asignan por número.
 * - Eliminar solo si ninguna actividad usa ese número.
 * - «Sugeridos» crea los 5 módulos del curso que falten.
 */
class ModulosAdminFragment : Fragment(R.layout.fragment_modulos_admin) {

    private val db = FirebaseFirestore.getInstance()

    private val lista = mutableListOf<Modulo>()

    /** número de módulo -> (actividades, tiene reto final). */
    private var conteo: Map<Int, Pair<Int, Boolean>> = emptyMap()

    private lateinit var adapter: Adaptador
    private lateinit var tvVacio: TextView

    private val colores = listOf("#3E7BFA", "#8B5CF6", "#FF7A45", "#00B8D9", "#E91E63")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvVacio = view.findViewById(R.id.tvVacioModulosAdmin)

        val recycler = view.findViewById<RecyclerView>(R.id.rvModulosAdmin)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = Adaptador()
        recycler.adapter = adapter

        view.findViewById<TextView>(R.id.btnVolverModulosAdmin).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<FloatingActionButton>(R.id.fabNuevoModulo).setOnClickListener {
            mostrarDialogo(null)
        }

        view.findViewById<MaterialButton>(R.id.btnCrearSugeridos).setOnClickListener {
            ModulosRepository.crearSugeridos(
                lista,
                onTerminar = { creados ->
                    if (!isAdded) return@crearSugeridos
                    val mensaje = if (creados == 0) "Ya existen todos los módulos sugeridos"
                    else "Se crearon $creados módulo(s)"
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                    cargar()
                },
                onError = { mostrarError(it) }
            )
        }

        cargar()
    }

    private fun cargar() {

        ModulosRepository.obtenerModulosAdmin(
            onLista = { modulos ->
                if (!isAdded) return@obtenerModulosAdmin
                lista.clear()
                lista.addAll(modulos)
                tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                adapter.notifyDataSetChanged()
            },
            onError = { mostrarError(it) }
        )

        // Cuántas actividades tiene cada módulo y si ya tiene reto final.
        db.collection("actividades").get()
            .addOnSuccessListener { documentos ->
                if (!isAdded) return@addOnSuccessListener
                val porModulo = documentos.groupBy { (it.getLong("modulo") ?: 1L).toInt() }
                conteo = porModulo.mapValues { (_, docs) ->
                    docs.size to docs.any {
                        NivelActividad.desde(it.getString("nivel")) == NivelActividad.INTEGRACION
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun mostrarDialogo(existente: Modulo?) {

        val vista = layoutInflater.inflate(R.layout.dialog_modulo, null)
        val etNumero = vista.findViewById<TextInputEditText>(R.id.etNumeroModulo)
        val etTitulo = vista.findViewById<TextInputEditText>(R.id.etTituloModulo)
        val etDescripcion = vista.findViewById<TextInputEditText>(R.id.etDescripcionModulo)
        val swActivo = vista.findViewById<SwitchMaterial>(R.id.swModuloActivo)
        val tvAviso = vista.findViewById<TextView>(R.id.tvAvisoNumeroModulo)

        if (existente != null) {
            etNumero.setText(existente.numero.toString())
            etNumero.isEnabled = false
            tvAviso.visibility = View.VISIBLE
            etTitulo.setText(existente.titulo)
            etDescripcion.setText(existente.descripcion)
            swActivo.isChecked = existente.activo
        } else {
            val siguiente = (lista.maxOfOrNull { it.numero } ?: 0) + 1
            etNumero.setText(siguiente.toString())
        }

        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(if (existente == null) "Nuevo módulo" else "Editar módulo")
            .setView(vista)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val numero = etNumero.text?.toString()?.trim()?.toIntOrNull()
                val titulo = etTitulo.text?.toString()?.trim().orEmpty()
                val descripcion = etDescripcion.text?.toString()?.trim().orEmpty()

                when {
                    numero == null || numero !in 1..99 -> {
                        etNumero.error = "Escribe un número entre 1 y 99"
                        return@setOnClickListener
                    }
                    existente == null && lista.any { it.numero == numero } -> {
                        etNumero.error = "Ya existe el módulo $numero"
                        return@setOnClickListener
                    }
                    titulo.isEmpty() -> {
                        etTitulo.error = "Escribe el título"
                        return@setOnClickListener
                    }
                }

                val modulo = Modulo(
                    id = existente?.id ?: "",
                    numero = numero!!,
                    titulo = titulo,
                    descripcion = descripcion,
                    activo = swActivo.isChecked
                )

                ModulosRepository.guardarModulo(
                    modulo,
                    onSuccess = {
                        if (!isAdded) return@guardarModulo
                        Toast.makeText(requireContext(), "Módulo guardado", Toast.LENGTH_SHORT).show()
                        dialogo.dismiss()
                        cargar()
                    },
                    onError = { mostrarError(it) }
                )
            }
        }

        dialogo.show()
    }

    private fun confirmarEliminar(modulo: Modulo) {

        val actividades = conteo[modulo.numero]?.first ?: 0

        if (actividades > 0) {
            AlertDialog.Builder(requireContext())
                .setTitle("No se puede eliminar")
                .setMessage(
                    "El módulo ${modulo.numero} tiene $actividades actividad(es). " +
                            "Muévelas a otro módulo o desactiva el módulo para ocultarlo."
                )
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        val esSugerido = Modulo.SUGERIDOS.any { it.numero == modulo.numero }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar módulo")
            .setMessage(
                "¿Eliminar «${modulo.nombreCompleto()}»?" +
                        if (esSugerido) "\n\nEs uno de los módulos sugeridos: si lo eliminas vuelve a su " +
                                "nombre original. Para ocultarlo, mejor desactívalo." else ""
            )
            .setPositiveButton("Eliminar") { _, _ ->
                ModulosRepository.eliminarModulo(
                    modulo.id,
                    onSuccess = {
                        if (!isAdded) return@eliminarModulo
                        Toast.makeText(requireContext(), "Módulo eliminado", Toast.LENGTH_SHORT).show()
                        cargar()
                    },
                    onError = { mostrarError(it) }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarError(e: Exception) {
        if (!isAdded) return
        Toast.makeText(
            requireContext(),
            "Error: ${e.localizedMessage}. ¿Publicaste la regla de «modulos» en Firestore?",
            Toast.LENGTH_LONG
        ).show()
    }

    // ------------------------------------------------------------------

    private inner class Adaptador : RecyclerView.Adapter<Adaptador.VH>() {

        inner class VH(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
            val tvNumero: TextView = card.findViewById(R.id.tvNumeroModuloAdmin)
            val tvTitulo: TextView = card.findViewById(R.id.tvTituloModuloAdmin)
            val tvDetalle: TextView = card.findViewById(R.id.tvDetalleModuloAdmin)
            val tvEstado: TextView = card.findViewById(R.id.tvEstadoModuloAdmin)
            val btnEliminar: ImageView = card.findViewById(R.id.btnEliminarModulo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_modulo_admin, parent, false) as MaterialCardView
            )

        override fun getItemCount() = lista.size

        override fun onBindViewHolder(holder: VH, position: Int) {

            val modulo = lista[position]
            val color = colores[(modulo.numero - 1).coerceAtLeast(0) % colores.size]

            holder.tvNumero.text = modulo.numero.toString()
            holder.tvNumero.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(if (modulo.activo) color else "#B0B7C3"))
            }

            holder.tvTitulo.text = modulo.titulo

            val (cantidad, tieneReto) = conteo[modulo.numero] ?: (0 to false)
            holder.tvDetalle.text = "$cantidad actividad(es) · " +
                    if (tieneReto) "🏆 con reto final" else "⚠ sin reto final"

            holder.tvEstado.text = if (modulo.activo) "Visible" else "Oculto"
            holder.tvEstado.setTextColor(Color.parseColor(if (modulo.activo) "#2E7D32" else "#8A93A6"))

            holder.card.setOnClickListener { mostrarDialogo(modulo) }
            holder.btnEliminar.setOnClickListener { confirmarEliminar(modulo) }
        }
    }
}
