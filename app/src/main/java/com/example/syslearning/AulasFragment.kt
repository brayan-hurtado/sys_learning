package com.example.syslearning

import android.app.AlertDialog
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.AulaActual
import com.example.syslearning.PanelAdministradorActivity
import com.example.syslearning.R
import com.example.syslearning.adapters.AulaAdapter
import com.example.syslearning.fragments.DetalleAulaFragment
import com.example.syslearning.model.Aula
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AulasFragment : Fragment(R.layout.fragment_aulas) {

    private lateinit var recycler: RecyclerView
    private lateinit var fabNuevaAula: FloatingActionButton
    private lateinit var adapter: AulaAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val listaAulas = mutableListOf<Aula>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvAulas)
        fabNuevaAula = view.findViewById(R.id.fabNuevaAula)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recycler.layoutManager = LinearLayoutManager(requireContext())

        AulaActual.limpiar()

        (activity as? PanelAdministradorActivity)
            ?.actualizarTituloAula()

        Toast.makeText(
            requireContext(),
            "Mostrando información general",
            Toast.LENGTH_SHORT
        ).show()

        adapter = AulaAdapter(

            listaAulas,

            onClick = { aula ->

                abrirDetalleAula(aula)

            },

            onLongClick = { aula ->

                mostrarMenuAula(aula)

            }

        )



        recycler.adapter = adapter

        cargarAulas()

        fabNuevaAula.setOnClickListener {

            mostrarDialogoNuevaAula()

        }
    }

    private fun abrirDetalleAula(aula: Aula) {

        // Guardar el aula seleccionada
        AulaActual.id = aula.id
        AulaActual.codigo = aula.codigo
        AulaActual.nombre = aula.nombre

        (activity as? PanelAdministradorActivity)
            ?.actualizarTituloAula()

        val fragment = DetalleAulaFragment()

        val bundle = Bundle()
        bundle.putString("idAula", aula.id)

        fragment.arguments = bundle

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.frameAdmin, fragment)
            .addToBackStack(null)
            .commit()

    }

    private fun mostrarDialogoNuevaAula() {

        val vista = layoutInflater.inflate(
            R.layout.dialog_nueva_aula,
            null
        )

        val edtNombre =
            vista.findViewById<EditText>(R.id.edtNombreAula)

        val btnCancelar =
            vista.findViewById<Button>(R.id.btnCancelar)

        val btnCrear =
            vista.findViewById<Button>(R.id.btnCrear)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(vista)
            .create()

        btnCancelar.setOnClickListener {

            dialog.dismiss()

        }

        btnCrear.setOnClickListener {

            val nombre = edtNombre.text.toString().trim()

            if (nombre.isEmpty()) {

                edtNombre.error = "Ingrese un nombre"

                return@setOnClickListener

            }

            crearAula(nombre)

            dialog.dismiss()

        }

        dialog.show()

    }

    private fun crearAula(nombre: String) {

        val uid = auth.currentUser?.uid ?: return

        val aula = Aula(

            nombre = nombre,

            codigo = generarCodigo(),

            adminUid = uid

        )

        db.collection("aulas")
            .add(aula)
            .addOnSuccessListener {

                Toast.makeText(
                    requireContext(),
                    "Aula creada correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                cargarAulas()

            }
            .addOnFailureListener {

                Toast.makeText(
                    requireContext(),
                    "No fue posible crear el aula",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun cargarAulas() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("aulas")
            .whereEqualTo("adminUid", uid)
            .get()
            .addOnSuccessListener { documentos ->

                listaAulas.clear()

                documentos.forEach { doc ->

                    val aula = doc.toObject(Aula::class.java)

                    aula.id = doc.id

                    listaAulas.add(aula)

                }

                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { e ->

                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_LONG
                ).show()

                Log.e("Aulas", "Error al crear aula", e)

            }

    }

    private fun generarCodigo(): String {

        val caracteres =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        val codigo = StringBuilder("SYS-")

        repeat(5) {

            codigo.append(
                caracteres.random()
            )

        }

        return codigo.toString()

    }

    private fun mostrarMenuAula(aula: Aula) {

        val opciones = arrayOf(

            "📋 Copiar código",

            "✏️ Editar nombre",

            "🗑️ Eliminar aula"

        )

        AlertDialog.Builder(requireContext())
            .setTitle(aula.nombre)
            .setItems(opciones) { _, which ->

                when (which) {

                    0 -> copiarCodigo(aula)

                    1 -> editarAula(aula)

                    2 -> confirmarEliminarAula(aula)

                }

            }
            .show()

    }

    private fun copiarCodigo(aula: Aula) {

        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                    as ClipboardManager

        val clip = ClipData.newPlainText(
            "Código Aula",
            aula.codigo
        )

        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            requireContext(),
            "Código copiado",
            Toast.LENGTH_SHORT
        ).show()

    }

    private fun editarAula(aula: Aula) {

        val vista = layoutInflater.inflate(
            R.layout.dialog_nueva_aula,
            null
        )

        val edtNombre =
            vista.findViewById<EditText>(R.id.edtNombreAula)

        val btnCancelar =
            vista.findViewById<Button>(R.id.btnCancelar)

        val btnCrear =
            vista.findViewById<Button>(R.id.btnCrear)

        edtNombre.setText(aula.nombre)

        btnCrear.text = "Guardar"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(vista)
            .create()

        btnCancelar.setOnClickListener {

            dialog.dismiss()

        }

        btnCrear.setOnClickListener {

            val nuevoNombre = edtNombre.text.toString().trim()

            if (nuevoNombre.isEmpty()) {

                edtNombre.error = "Ingrese un nombre"

                return@setOnClickListener

            }

            db.collection("aulas")
                .document(aula.id)
                .update("nombre", nuevoNombre)
                .addOnSuccessListener {

                    Toast.makeText(
                        requireContext(),
                        "Aula actualizada",
                        Toast.LENGTH_SHORT
                    ).show()

                    cargarAulas()

                    dialog.dismiss()

                }
                .addOnFailureListener {

                    Toast.makeText(
                        requireContext(),
                        "No fue posible actualizar",
                        Toast.LENGTH_SHORT
                    ).show()

                }

        }

        dialog.show()

    }

    private fun confirmarEliminarAula(aula: Aula) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar aula")
            .setMessage(
                "¿Está seguro de eliminar el aula \"${aula.nombre}\"?\n\nEsta acción no se puede deshacer."
            )
            .setPositiveButton("Eliminar") { _, _ ->

                eliminarAula(aula)

            }
            .setNegativeButton("Cancelar", null)
            .show()

    }

    private fun eliminarAula(aula: Aula) {

        db.collection("aulas")
            .document(aula.id)
            .delete()
            .addOnSuccessListener {

                Toast.makeText(
                    requireContext(),
                    "Aula eliminada",
                    Toast.LENGTH_SHORT
                ).show()

                cargarAulas()

            }
            .addOnFailureListener {

                Toast.makeText(
                    requireContext(),
                    "No fue posible eliminar el aula",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

}