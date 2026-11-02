package com.example.syslearning
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.LinearLayout
import com.example.syslearning.fragments.UsuariosFragment
import androidx.fragment.app.Fragment
import com.example.syslearning.AulasFragment
import com.example.syslearning.fragments.DashboardFragment
import com.example.syslearning.fragments.CursosFragment
import com.example.syslearning.fragments.ActividadesAdminFragment

class PanelAdministradorActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar

    private lateinit var btnCerrarSesion: LinearLayout

    private lateinit var itemDashboard: LinearLayout
    private lateinit var itemUsuarios: LinearLayout
    private lateinit var itemCursos: LinearLayout
    private lateinit var itemAulas: LinearLayout
    private lateinit var itemActividades: LinearLayout
    private lateinit var itemEstadisticas: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_panel_administrador)

        // Toolbar
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.title = "Panel Administrador"

        actualizarTituloAula()

        // Drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Menú
        itemDashboard = findViewById(R.id.itemDashboard)
        itemUsuarios = findViewById(R.id.itemUsuarios)
        itemCursos = findViewById(R.id.itemCursos)
        itemAulas = findViewById(R.id.itemAulas)
        itemActividades = findViewById(R.id.itemActividades)
        itemEstadisticas = findViewById(R.id.itemEstadisticas)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)



        // Fragmento inicial
        abrirFragment(DashboardFragment())

        fun abrirFragmentDesdeDashboard(fragment: Fragment) {

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameAdmin, fragment)
                .addToBackStack(null)
                .commit()

        }

        // Dashboard
        itemDashboard.setOnClickListener {

            abrirFragment(DashboardFragment())

            drawerLayout.closeDrawers()

        }

        //cursos
        itemCursos.setOnClickListener {

            abrirFragment(CursosFragment())

            drawerLayout.closeDrawers()

        }

        // Usuarios
        itemUsuarios.setOnClickListener {

            abrirFragment(UsuariosFragment())

            drawerLayout.closeDrawers()

        }


        // Módulos
        itemAulas.setOnClickListener {

            abrirFragment(AulasFragment())

            drawerLayout.closeDrawers()

        }

        // Actividades
        itemActividades.setOnClickListener {

            abrirFragment(ActividadesAdminFragment())

            drawerLayout.closeDrawers()

        }

        // Estadísticas
        itemEstadisticas.setOnClickListener {

            abrirFragment(EstadisticasAdminFragment())

            drawerLayout.closeDrawers()

        }


        // Cerrar sesión
        btnCerrarSesion.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, Pantalla2::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()

        }

    }


    /**
     * Cambia el fragment mostrado en el panel administrador
     */
    private fun abrirFragment(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameAdmin, fragment)
            .commit()

    }

    fun abrirFragmentDesdeDashboard(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameAdmin, fragment)
            .addToBackStack(null)
            .commit()

    }

    fun actualizarTituloAula() {

        if (AulaActual.hayFiltro()) {

            supportActionBar?.subtitle = "🏫 ${AulaActual.nombre}"

        } else {

            supportActionBar?.subtitle = "Vista general"

        }

    }


}