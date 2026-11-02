package com.example.syslearning

import android.content.Context
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java


class Pantalla2 : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla2)

        auth = FirebaseAuth.getInstance()

        val etCorreo = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegistro = findViewById<TextView>(R.id.tvRegistro)


        // Ir al registro
        tvRegistro.setOnClickListener {
            startActivity(Intent(this, registro::class.java))
        }

        // Iniciar sesión
        btnLogin.setOnClickListener {

            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Complete todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener {

                    val uid = FirebaseAuth.getInstance().currentUser!!.uid

                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { document ->

                            val rol = document.getString("rol")

                            Toast.makeText(
                                this,
                                "¡Bienvenido!",
                                Toast.LENGTH_SHORT
                            ).show()

                            if (rol == "admin") {

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

                }
                .addOnFailureListener { e ->

                    Toast.makeText(
                        this,
                        e.localizedMessage ?: "Correo o contraseña incorrectos",
                        Toast.LENGTH_LONG
                    ).show()

                }


            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->

                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )

                insets
            }


        }


            }
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()


        }

                return super.dispatchTouchEvent(ev)
    }

}