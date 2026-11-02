package com.example.syslearning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import de.hdodenhof.circleimageview.CircleImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FieldValue




class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var imgPerfil: CircleImageView

    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvTipoDocumento: TextView
    private lateinit var tvDocumento: TextView
    private lateinit var tvFechaNacimiento: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var layoutMisAulas: LinearLayout
    private lateinit var btnEditar: Button
    private lateinit var btnCambiarPassword: Button
    private lateinit var btnCerrarSesion: Button

    private var imagenSeleccionada: Uri? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {

                imagenSeleccionada = uri

                imgPerfil.setImageURI(uri)

                subirImagen(uri)

            }

        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        val btnUnirseAula = view.findViewById<Button>(R.id.btnUnirseAula)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        imgPerfil = view.findViewById(R.id.imgPerfil)
        layoutMisAulas = view.findViewById(R.id.layoutMisAulas)
        tvNombre = view.findViewById(R.id.tvNombre)
        tvCorreo = view.findViewById(R.id.tvCorreo)
        tvTipoDocumento = view.findViewById(R.id.tvTipoDocumento)
        tvDocumento = view.findViewById(R.id.tvDocumento)
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento)
        tvTelefono = view.findViewById(R.id.tvTelefono)

        btnEditar = view.findViewById(R.id.btnEditar)
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)

        cargarDatosUsuario()
        cargarMisAulas()

        imgPerfil.setOnClickListener {

            seleccionarImagen.launch("image/*")

        }

        btnEditar.setOnClickListener {
            mostrarDialogEditar()


        }

        btnCambiarPassword.setOnClickListener {

            mostrarDialogCambiarPassword()

        }

        btnCerrarSesion.setOnClickListener {

            auth.signOut()

            startActivity(
                Intent(requireContext(), MainActivity::class.java)
            )

            requireActivity().finish()

        }

        btnUnirseAula.setOnClickListener {

            mostrarDialogoUnirseAula()

        }

        return view
    }

    private fun cargarDatosUsuario() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()

            .addOnSuccessListener { document ->

                if (!isAdded) return@addOnSuccessListener

                if (document.exists()) {

                    tvNombre.text = document.getString("nombre")
                    tvCorreo.text = document.getString("correo")
                    tvTipoDocumento.text = document.getString("tipoDocumento")
                    tvDocumento.text = document.getString("documento")
                    tvFechaNacimiento.text = document.getString("fechaNacimiento")
                    tvTelefono.text = document.getString("telefono")


                    val fotoPerfil = document.getString("fotoPerfil")

                    if (!fotoPerfil.isNullOrEmpty()) {

                        Glide.with(imgPerfil)
                            .load(fotoPerfil)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(imgPerfil)

                    }

                }

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar la información.",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun mostrarDialogEditar() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_editar_perfil,
            null
        )

        val etNombre =
            dialogView.findViewById<EditText>(R.id.etEditarNombre)

        val etFecha =
            dialogView.findViewById<EditText>(R.id.etEditarFecha)

        val etTelefono =
            dialogView.findViewById<EditText>(R.id.etEditarTelefono)

        val btnCancelar =
            dialogView.findViewById<Button>(R.id.btnCancelarEditar)

        val btnGuardar =
            dialogView.findViewById<Button>(R.id.btnGuardarEditar)

        etNombre.setText(tvNombre.text.toString())
        etFecha.setText(tvFechaNacimiento.text.toString())
        etTelefono.setText(tvTelefono.text.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        etFecha.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    etFecha.setText("$day/${month + 1}/$year")

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.datePicker.maxDate = System.currentTimeMillis()

            datePicker.show()

        }

        btnCancelar.setOnClickListener {

            dialog.dismiss()

        }

        btnGuardar.setOnClickListener {

            guardarCambiosPerfil(
                dialog,
                etNombre.text.toString().trim(),
                etFecha.text.toString().trim(),
                etTelefono.text.toString().trim()
            )

        }

        dialog.show()

    }

    private fun guardarCambiosPerfil(

        dialog: AlertDialog,
        nombre: String,
        fecha: String,
        telefono: String

    ) {

        val uid = auth.currentUser?.uid ?: return

        if (nombre.isEmpty() ||
            fecha.isEmpty() ||
            telefono.isEmpty()
        ) {

            if (!isAdded) return

            Toast.makeText(
                requireContext(),
                "Complete todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return

        }

        val datos = hashMapOf<String, Any>(

            "nombre" to nombre,
            "fechaNacimiento" to fecha,
            "telefono" to telefono

        )

        db.collection("usuarios")
            .document(uid)
            .update(datos)

            .addOnSuccessListener {

                if (!isAdded) return@addOnSuccessListener

                Toast.makeText(
                    requireContext(),
                    "Perfil actualizado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                cargarDatosUsuario()

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "No se pudo actualizar el perfil",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun mostrarDialogCambiarPassword() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_cambiar_password,
            null
        )

        val etActual =
            dialogView.findViewById<EditText>(R.id.etPasswordActual)

        val etNueva =
            dialogView.findViewById<EditText>(R.id.etNuevaPassword)

        val etConfirmar =
            dialogView.findViewById<EditText>(R.id.etConfirmarPassword)

        val btnCancelar =
            dialogView.findViewById<Button>(R.id.btnCancelarPassword)

        val btnGuardar =
            dialogView.findViewById<Button>(R.id.btnGuardarPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancelar.setOnClickListener {

            dialog.dismiss()

        }

        btnGuardar.setOnClickListener {

            cambiarPassword(
                dialog,
                etActual.text.toString().trim(),
                etNueva.text.toString().trim(),
                etConfirmar.text.toString().trim()
            )

        }

        dialog.show()

    }

    private fun cambiarPassword(

        dialog: AlertDialog,
        passwordActual: String,
        nuevaPassword: String,
        confirmarPassword: String

    ) {

        if (
            passwordActual.isEmpty() ||
            nuevaPassword.isEmpty() ||
            confirmarPassword.isEmpty()
        ) {

            if (!isAdded) return

            Toast.makeText(
                requireContext(),
                "Complete todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return

        }

        if (nuevaPassword.length < 6) {

            if (!isAdded) return

            Toast.makeText(
                requireContext(),
                "La nueva contraseña debe tener mínimo 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()

            return

        }

        if (nuevaPassword != confirmarPassword) {

            if (!isAdded) return

            Toast.makeText(
                requireContext(),
                "Las contraseñas no coinciden",
                Toast.LENGTH_SHORT
            ).show()

            return

        }

        val usuario = auth.currentUser ?: return

        val credential = EmailAuthProvider.getCredential(
            usuario.email!!,
            passwordActual
        )

        usuario.reauthenticate(credential)

            .addOnSuccessListener {

                usuario.updatePassword(nuevaPassword)

                    .addOnSuccessListener {

                        if (!isAdded) return@addOnSuccessListener

                        Toast.makeText(
                            requireContext(),
                            "Contraseña actualizada correctamente",
                            Toast.LENGTH_LONG
                        ).show()

                        dialog.dismiss()

                    }

                    .addOnFailureListener {

                        if (!isAdded) return@addOnFailureListener

                        Toast.makeText(
                            requireContext(),
                            "No se pudo cambiar la contraseña",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "La contraseña actual es incorrecta",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun subirImagen(uri: Uri) {

        val uid = auth.currentUser?.uid ?: return

        val referenciaImagen = storageReference
            .child("fotosPerfil")
            .child("$uid.jpg")

        referenciaImagen.putFile(uri)

            .addOnSuccessListener {

                referenciaImagen.downloadUrl

                    .addOnSuccessListener { downloadUri ->

                        db.collection("usuarios")
                            .document(uid)
                            .update("fotoPerfil", downloadUri.toString())

                            .addOnSuccessListener {

                                if (isAdded) {

                                    Glide.with(imgPerfil)
                                        .load(downloadUri)
                                        .placeholder(R.drawable.ic_person)
                                        .error(R.drawable.ic_person)
                                        .into(imgPerfil)

                                    Toast.makeText(
                                        requireContext(),
                                        "Foto actualizada correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }

                            }

                            .addOnFailureListener {

                                if (!isAdded) return@addOnFailureListener

                                Toast.makeText(
                                    requireContext(),
                                    "No se pudo guardar la foto en Firestore.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                    }

                    .addOnFailureListener {

                        if (!isAdded) return@addOnFailureListener

                        Toast.makeText(
                            requireContext(),
                            "No se pudo obtener la URL de la imagen.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Error al subir la imagen",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun mostrarDialogoUnirseAula() {

        val vista = layoutInflater.inflate(
            R.layout.dialog_unirse_aula,
            null
        )

        val etCodigo = vista.findViewById<TextInputEditText>(
            R.id.etCodigoAula
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Unirse a un aula")
            .setView(vista)
            .setPositiveButton("Unirse", null)
            .setNegativeButton("Cancelar", null)
            .create()
            .apply {

                setOnShowListener {

                    getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {

                            val codigo = etCodigo.text
                                .toString()
                                .trim()
                                .uppercase()

                            if (codigo.isEmpty()) {

                                etCodigo.error = "Ingrese el código"

                                return@setOnClickListener

                            }

                            buscarAula(codigo)

                            dismiss()

                        }

                }

            }
            .show()

    }

    private fun buscarAula(codigo: String) {

        db.collection("aulas")
            .whereEqualTo("codigo", codigo)
            .limit(1)
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                if (documentos.isEmpty) {

                    Toast.makeText(
                        requireContext(),
                        "El código no existe",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener

                }

                unirseAAula(codigo)

            }
            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Error al consultar el aula",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun unirseAAula(codigo: String) {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .set(
                mapOf(
                    "aulas" to FieldValue.arrayUnion(codigo)
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .addOnSuccessListener {

                if (!isAdded) return@addOnSuccessListener

                Toast.makeText(
                    requireContext(),
                    "Te has unido al aula correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                cargarMisAulas()

            }
            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "No fue posible unirse al aula",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun cargarMisAulas() {

        val uid = auth.currentUser?.uid ?: return

        layoutMisAulas.removeAllViews()

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documentoUsuario ->

                if (!isAdded) return@addOnSuccessListener

                val codigos =
                    documentoUsuario.get("aulas") as? List<String> ?: emptyList()

                if (codigos.isEmpty()) {

                    val tv = TextView(requireContext())
                    tv.text = "No perteneces a ninguna aula"
                    tv.textSize = 15f
                    tv.setTextColor(resources.getColor(android.R.color.darker_gray))

                    layoutMisAulas.addView(tv)

                    return@addOnSuccessListener

                }

                db.collection("aulas")
                    .whereIn("codigo", codigos)
                    .get()
                    .addOnSuccessListener { aulas ->

                        if (!isAdded) return@addOnSuccessListener

                        layoutMisAulas.removeAllViews()

                        for (documento in aulas) {

                            val nombre = documento.getString("nombre") ?: ""
                            val codigo = documento.getString("codigo") ?: ""

                            val tv = TextView(requireContext())

                            tv.text = "🏫 $nombre\nCódigo: $codigo"

                            tv.textSize = 16f

                            tv.setPadding(0, 12, 0, 12)

                            layoutMisAulas.addView(tv)

                        }

                    }
                    .addOnFailureListener {

                        if (!isAdded) return@addOnFailureListener

                        Toast.makeText(
                            requireContext(),
                            "No se pudieron cargar tus aulas",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

            }
            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "No se pudo consultar tu información",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

}