package com.example.syslearning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.jvm.java
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay una sesión iniciada, ir directamente al menú
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { document ->

                    val rol = document.getString("rol")

                    if (rol.equals("admin", ignoreCase = true)) {

                        startActivity(
                            Intent(this, PanelAdministradorActivity::class.java)
                        )

                    } else {

                        startActivity(
                            Intent(this, Menu::class.java)
                        )

                    }

                    finish()

                }
                .addOnFailureListener {

                    // Si ocurre un error leyendo Firestore,
                    // cerrar la sesión por seguridad.
                    FirebaseAuth.getInstance().signOut()

                    setContentView(R.layout.activity_main)

                    inicializarBoton()

                }

            return
        }

        // Si no hay sesión, mostrar pantalla principal
        setContentView(R.layout.activity_main)

        inicializarBoton()
    }

    private fun inicializarBoton() {

        val btn = findViewById<Button>(R.id.bienvenido)

        btn.setOnClickListener {

            btn.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(80)
                .withEndAction {

                    btn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)

                    startActivity(Intent(this, Pantalla2::class.java))

                    overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                }
        }
    }
}
