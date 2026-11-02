package com.example.syslearning.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syslearning.AulaActual
import com.example.syslearning.R
import com.example.syslearning.adapter.UsuarioAdapter
import com.example.syslearning.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.syslearning.DetalleUsuarioFragment

class UsuariosFragment : Fragment(R.layout.fragment_usuarios) {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var edtBuscar: EditText

    private val listaUsuarios = mutableListOf<Usuario>()
    private val listaFiltrada = mutableListOf<Usuario>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvUsuarios)
        edtBuscar = view.findViewById(R.id.edtBuscar)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = UsuarioAdapter(listaFiltrada) { usuario ->

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameAdmin, DetalleUsuarioFragment.newInstance(usuario.uid))
                .addToBackStack(null)
                .commit()

        }

        recycler.adapter = adapter

        // Aviso simple de que hay un filtro de aula activo.
        // (Si me compartes fragment_usuarios.xml puedo poner esto como
        // un banner fijo en pantalla en vez de un Toast.)
        if (AulaActual.hayFiltro()) {
            Toast.makeText(
                requireContext(),
                "Mostrando estudiantes de: ${AulaActual.nombre}",
                Toast.LENGTH_SHORT
            ).show()
        }

        cargarUsuarios()

        // Buscar en tiempo real
        edtBuscar.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                filtrarUsuarios(s.toString())

            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }

    private fun cargarUsuarios() {

        var consulta: Query = FirebaseFirestore.getInstance().collection("usuarios")

        // Si hay un aula seleccionada en AulasFragment, solo se traen
        // los estudiantes cuyo campo "aulas" (lista de códigos) contiene
        // el código del aula seleccionada.
        if (AulaActual.hayFiltro()) {
            consulta = consulta.whereArrayContains("aulas", AulaActual.codigo)
        }

        consulta
            .get()
            .addOnSuccessListener { documents ->

                if (!isAdded) return@addOnSuccessListener

                listaUsuarios.clear()

                for (doc in documents) {

                    val usuario = doc.toObject(Usuario::class.java)

                    usuario.uid = doc.id

                    listaUsuarios.add(usuario)

                }

                listaFiltrada.clear()
                listaFiltrada.addAll(listaUsuarios)

                adapter.notifyDataSetChanged()

            }

    }

    private fun filtrarUsuarios(texto: String) {

        listaFiltrada.clear()

        if (texto.isEmpty()) {

            listaFiltrada.addAll(listaUsuarios)

        } else {

            val busqueda = texto.lowercase()

            for (usuario in listaUsuarios) {

                val nombre = usuario.nombre.lowercase()
                val correo = usuario.correo.lowercase()
                val rol = usuario.rol.lowercase()

                if (
                    nombre.contains(busqueda)
                    ||
                    correo.contains(busqueda)
                    ||
                    rol.contains(busqueda)
                ) {

                    listaFiltrada.add(usuario)

                }

            }

        }

        adapter.notifyDataSetChanged()

    }

}