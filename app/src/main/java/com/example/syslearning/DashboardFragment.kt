package com.example.syslearning.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.syslearning.AulaActual
import com.example.syslearning.Cursos
import com.example.syslearning.EstadisticasAdminFragment
import com.example.syslearning.PanelAdministradorActivity
import com.example.syslearning.R
import com.example.syslearning.RankingRepository
import com.example.syslearning.model.CursoFirestore
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    private lateinit var txtUsuarios: TextView
    private lateinit var txtCursos: TextView
    private lateinit var txtActividades: TextView
    private lateinit var txtTopNombre: TextView
    private lateinit var txtTopPuntos: TextView
    private lateinit var txtTopInfo: TextView
    private lateinit var cardTopRanking: MaterialCardView
    private lateinit var cardUsuarios: MaterialCardView
    private lateinit var cardCursos: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_dashboard,
            container,
            false
        )

        db = FirebaseFirestore.getInstance()

        txtUsuarios = view.findViewById(R.id.txtUsuarios)
        txtCursos = view.findViewById(R.id.txtCursos)
        txtActividades = view.findViewById(R.id.txtActividades)
        txtTopNombre = view.findViewById(R.id.txtTopNombre)
        txtTopPuntos = view.findViewById(R.id.txtTopPuntos)
        txtTopInfo = view.findViewById(R.id.txtTopInfo)
        cardTopRanking = view.findViewById(R.id.cardTopRanking)
        cardUsuarios = view.findViewById(R.id.cardUsuarios)
        cardCursos = view.findViewById(R.id.cardCursos)

        cardTopRanking.setOnClickListener {

            (activity as? PanelAdministradorActivity)
                ?.abrirFragmentDesdeDashboard(EstadisticasAdminFragment())

        }

        cardUsuarios.setOnClickListener {

            (activity as? PanelAdministradorActivity)
                ?.abrirFragmentDesdeDashboard(UsuariosFragment())

        }

        cardCursos.setOnClickListener {

            (activity as? PanelAdministradorActivity)
                ?.abrirFragmentDesdeDashboard(CursosFragment())

        }

        cargarDashboard()

        return view
    }

    private fun cargarDashboard() {

        cargarUsuarios()

        cargarCursos()

        cargarActividades()

        cargarTopRanking()

    }

    /**
     * Usuarios registrados. Si hay un aula seleccionada en AulasFragment,
     * solo cuenta los estudiantes inscritos en esa aula.
     */
    private fun cargarUsuarios() {

        var consulta: Query = db.collection("usuarios")

        if (AulaActual.hayFiltro()) {
            consulta = consulta.whereArrayContains("aulas", AulaActual.codigo)
        }

        consulta
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                txtUsuarios.text = documentos.size().toString()

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                txtUsuarios.text = "0"

            }

    }

    /**
     * Cursos visibles: los del sistema (siempre) + los de Firestore que
     * son globales o que pertenecen al aula seleccionada.
     */
    private fun cargarCursos() {

        val cursosLocales = Cursos.todosLosCursos.size
        val codigoAula = if (AulaActual.hayFiltro()) AulaActual.codigo else null

        db.collection("cursos")
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                val cursosFirestoreVisibles = documentos.count { doc ->

                    val curso = doc.toObject(CursoFirestore::class.java)

                    val esGlobal = curso.aulasAsignadas.isEmpty()
                    val perteneceAlAula =
                        codigoAula != null && curso.aulasAsignadas.contains(codigoAula)

                    codigoAula == null || esGlobal || perteneceAlAula
                }

                txtCursos.text = (cursosLocales + cursosFirestoreVisibles).toString()

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                txtCursos.text = cursosLocales.toString()

            }

    }

    /**
     * Actividades creadas.
     *
     * NOTA: la colección "actividades" no tiene, por ahora, ningún campo
     * que la relacione con un aula (como sí lo tienen "usuarios" con
     * "aulas" y "cursos" con "aulasAsignadas"), así que este número
     * siempre es el total general. Si más adelante agregas ese campo,
     * aplico aquí el mismo filtro.
     */
    private fun cargarActividades() {

        db.collection("actividades")
            .get()
            .addOnSuccessListener { documentos ->

                if (!isAdded) return@addOnSuccessListener

                txtActividades.text = documentos.size().toString()

            }

            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                txtActividades.text = "0"

            }

    }

    //cargar usuario numero 1 (respeta el aula seleccionada)//

    private fun cargarTopRanking() {

        val codigoAula = if (AulaActual.hayFiltro()) AulaActual.codigo else null

        RankingRepository.obtenerRanking(

            db = db,

            codigoAula = codigoAula,

            onSuccess = { ranking ->

                if (!isAdded) return@obtenerRanking

                if (ranking.isNotEmpty()) {

                    val top = ranking.first()

                    txtTopNombre.text = top.nombre
                    txtTopPuntos.text = "🏆 ${top.puntos} puntos"
                    txtTopInfo.text =
                        "🔥 ${top.racha} días • 📚 ${top.cursosCompletados} cursos"

                } else {

                    txtTopNombre.text = "Sin datos"
                    txtTopPuntos.text = ""
                    txtTopInfo.text = ""

                }

            },

            onError = {

                if (!isAdded) return@obtenerRanking

                txtTopNombre.text = "Error al cargar"
                txtTopPuntos.text = ""
                txtTopInfo.text = ""

            }

        )

    }



}