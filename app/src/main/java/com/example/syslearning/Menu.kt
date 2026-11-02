package com.example.syslearning

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.syslearning.ActividadesFragment
import com.google.android.material.card.MaterialCardView


class Menu : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Contenedores clicables (nueva barra)
    private lateinit var itemInicio: FrameLayout
    private lateinit var itemActividades: FrameLayout
    private lateinit var itemProgreso: FrameLayout
    private lateinit var itemPerfil: FrameLayout

    // Íconos
    private lateinit var imgInicio: ImageView
    private lateinit var imgActividades: ImageView
    private lateinit var imgProgreso: ImageView
    private lateinit var imgPerfil: ImageView

    // Indicador que se desliza hacia el ítem activo
    private lateinit var indicador: View

    private val items = mutableListOf<Pair<FrameLayout, ImageView>>()

    private var indiceActual = 0

    private val colorActivo = Color.parseColor("#FFFFFF")
    private val colorInactivo = Color.parseColor("#8A93A6")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // El margen inferior dinámico ahora se aplica a la tarjeta
        // (es la vista que tiene el layout_marginBottom en el XML nuevo).
        val cardBarraInferior = findViewById<MaterialCardView>(R.id.cardBarraInferior)

        ViewCompat.setOnApplyWindowInsetsListener(cardBarraInferior) { view, insets ->

            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = bottomInset + 20
            }

            insets
        }

        auth = FirebaseAuth.getInstance()

        // Contenedores (nuevos ids del XML mejorado)
        itemInicio = findViewById(R.id.itemInicio)
        itemActividades = findViewById(R.id.itemActividades)
        itemProgreso = findViewById(R.id.itemProgreso)
        itemPerfil = findViewById(R.id.itemPerfil)

        // Íconos
        imgInicio = findViewById(R.id.imgInicio)
        imgActividades = findViewById(R.id.imgActividades)
        imgProgreso = findViewById(R.id.imgProgreso)
        imgPerfil = findViewById(R.id.imgPerfil)

        indicador = findViewById(R.id.indicadorActivo)

        items.clear()
        items.add(itemInicio to imgInicio)
        items.add(itemActividades to imgActividades)
        items.add(itemProgreso to imgProgreso)
        items.add(itemPerfil to imgPerfil)

        itemInicio.setOnClickListener {
            seleccionarIndice(0)
            replaceFragment(HomeFragment())
        }

        itemActividades.setOnClickListener {
            seleccionarIndice(1)
            replaceFragment(ActividadesFragment())
        }

        itemProgreso.setOnClickListener {
            seleccionarIndice(2)
            replaceFragment(ProgresoFragment())
        }

        itemPerfil.setOnClickListener {
            seleccionarIndice(3)
            replaceFragment(PerfilFragment())
        }

        // El indicador necesita que la barra ya esté medida (ancho > 0)
        // para poder posicionarse correctamente la primera vez.
        indicador.post {

            indiceActual = savedInstanceState?.getInt("indiceActual") ?: 0

            posicionarIndicadorSinAnimar(indiceActual)
            actualizarColoresIconos(indiceActual)

            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("indiceActual", indiceActual)
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()

    }

    /**
     * Selecciona un ítem con animación: mueve el indicador deslizándose
     * hacia el ícono, y anima el cambio de color/escala de los íconos.
     */
    private fun seleccionarIndice(indice: Int) {

        indiceActual = indice

        actualizarColoresIconos(indice)

        val (contenedorActivo, _) = items[indice]

        indicador.animate()
            .x(contenedorActivo.x + contenedorActivo.width / 2f - indicador.width / 2f)
            .setDuration(220)
            .start()
    }

    /** Coloca el indicador de una vez, sin animación (usado en el arranque). */
    private fun posicionarIndicadorSinAnimar(indice: Int) {

        val (contenedorActivo, _) = items[indice]

        indicador.x = contenedorActivo.x + contenedorActivo.width / 2f - indicador.width / 2f
    }

    private fun actualizarColoresIconos(indiceSeleccionado: Int) {

        items.forEachIndexed { index, (_, icono) ->

            val activo = index == indiceSeleccionado

            icono.animate()
                .scaleX(if (activo) 1.15f else 1f)
                .scaleY(if (activo) 1.15f else 1f)
                .setDuration(150)
                .start()

            icono.setColorFilter(if (activo) colorActivo else colorInactivo)
        }
    }

    fun cerrarSesion() {

        auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}