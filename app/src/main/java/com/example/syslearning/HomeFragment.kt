package com.example.syslearning

import android.os.Bundle
import com.example.syslearning.juego.Gamificacion
import com.example.syslearning.juego.RobotVisual
import com.example.syslearning.juego.SysRobotFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.syslearning.fragments.PalabraDelDiaFragment
import com.example.syslearning.game.PalabraDelDiaManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.random.Random

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Cursos recomendados de la semana
    private lateinit var cursosSemana: List<Curso>

    // Categorías
    private lateinit var cardOfimatica: CardView
    private lateinit var cardRedes: CardView
    private lateinit var cardSeguridad: CardView
    private lateinit var cardProgramacion: CardView

    // Guías rápidas
    private lateinit var cardHardware: CardView
    private lateinit var cardLinux: CardView
    private lateinit var cardRedes1: CardView

    // Componentes de las guías
    private lateinit var imgGuia1: ImageView
    private lateinit var imgGuia2: ImageView
    private lateinit var imgGuia3: ImageView

    private lateinit var tvTituloGuia1: TextView
    private lateinit var tvTituloGuia2: TextView
    private lateinit var tvTituloGuia3: TextView

    private lateinit var tvNivelGuia1: TextView
    private lateinit var tvNivelGuia2: TextView
    private lateinit var tvNivelGuia3: TextView

    // Bienvenida
    private lateinit var tvSaludo: TextView
    private lateinit var tvCursoActual: TextView
    private lateinit var tvPorcentaje: TextView
    private lateinit var progresoCurso: ProgressBar
    private lateinit var btnContinuar: Button

    // La Palabra del Día
    private lateinit var cardPalabraDia: CardView
    private lateinit var tvEstadoPalabraDia: TextView

    /**
     * Abre una categoría
     */
    private fun abrirCategoria(nombreCategoria: String) {

        val fragment = CategoriaFragment()

        val bundle = Bundle()

        bundle.putString(
            "categoria",
            nombreCategoria
        )

        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .addToBackStack(null)
            .commit()

    }

    /**
     * Abre la pantalla del juego "La Palabra del Día".
     */
    private fun abrirPalabraDelDia() {

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, PalabraDelDiaFragment())
            .addToBackStack(null)
            .commit()

    }

    /**
     * Selecciona automáticamente
     * las 3 guías recomendadas de la semana.
     */
    private fun obtenerCursosSemana(): List<Curso> {

        val semana = Calendar.getInstance()
            .get(Calendar.WEEK_OF_YEAR)

        return Cursos.todosLosCursos
            .shuffled(Random(semana))
            .take(3)

    }

    /**
     * Abre cualquier curso.
     */
    private fun abrirCurso(curso: Curso) {

        val fragment = ContenidoFragment()

        val bundle = Bundle()

        bundle.putSerializable(
            "curso",
            curso
        )

        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .addToBackStack(null)
            .commit()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Categorías
        cardOfimatica = view.findViewById(R.id.cardOfimatica)
        cardRedes = view.findViewById(R.id.cardRedes)
        cardSeguridad = view.findViewById(R.id.cardSeguridad)
        cardProgramacion = view.findViewById(R.id.cardProgramacion)

        // Guías rápidas
        cardHardware = view.findViewById(R.id.cardHardware)
        cardLinux = view.findViewById(R.id.cardLinux)
        cardRedes1 = view.findViewById(R.id.cardRedes1)

        // Imágenes
        imgGuia1 = view.findViewById(R.id.imgGuia1)
        imgGuia2 = view.findViewById(R.id.imgGuia2)
        imgGuia3 = view.findViewById(R.id.imgGuia3)

        // Títulos
        tvTituloGuia1 = view.findViewById(R.id.tvTituloGuia1)
        tvTituloGuia2 = view.findViewById(R.id.tvTituloGuia2)
        tvTituloGuia3 = view.findViewById(R.id.tvTituloGuia3)

        // Subtítulos
        tvNivelGuia1 = view.findViewById(R.id.tvNivelGuia1)
        tvNivelGuia2 = view.findViewById(R.id.tvNivelGuia2)
        tvNivelGuia3 = view.findViewById(R.id.tvNivelGuia3)

        // Bienvenida
        tvSaludo = view.findViewById(R.id.tvSaludo)
        tvCursoActual = view.findViewById(R.id.tvCursoActual)
        tvPorcentaje = view.findViewById(R.id.tvPorcentaje)
        progresoCurso = view.findViewById(R.id.progresoCurso)
        btnContinuar = view.findViewById(R.id.btnContinuar)

        // La Palabra del Día
        cardPalabraDia = view.findViewById(R.id.cardPalabraDia)
        tvEstadoPalabraDia = view.findViewById(R.id.tvEstadoPalabraDia)

        cargarUsuario()
        cargarProgresoEnHome()
        actualizarEstadoPalabraDia()

        // Obtener las recomendaciones de esta semana
        cursosSemana = obtenerCursosSemana()

        // Guía 1
        imgGuia1.setImageResource(cursosSemana[0].imagen)
        tvTituloGuia1.text = cursosSemana[0].titulo


// Guía 2
        imgGuia2.setImageResource(cursosSemana[1].imagen)
        tvTituloGuia2.text = cursosSemana[1].titulo


// Guía 3
        imgGuia3.setImageResource(cursosSemana[2].imagen)
        tvTituloGuia3.text = cursosSemana[2].titulo



// Abrir las categorías


        cardOfimatica.setOnClickListener {

            abrirCategoria("Ofimática")

        }

        cardRedes.setOnClickListener {

            abrirCategoria("Redes")

        }

        cardSeguridad.setOnClickListener {

            abrirCategoria("Seguridad")

        }

        cardProgramacion.setOnClickListener {

            abrirCategoria("Programación")

        }


// Abrir las guías recomendadas


        cardHardware.setOnClickListener {

            abrirCurso(cursosSemana[0])

        }

        cardLinux.setOnClickListener {

            abrirCurso(cursosSemana[1])

        }

        cardRedes1.setOnClickListener {

            abrirCurso(cursosSemana[2])

        }

        btnContinuar.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "Próximamente continuará el curso.",
                Toast.LENGTH_SHORT
            ).show()

        }

        // Minijuego SYS ROBOT: muestra el saldo de monedas en la tarjeta.
        view.findViewById<CardView>(R.id.cardSysRobot).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, SysRobotFragment())
                .addToBackStack(null)
                .commit()
        }

        Gamificacion.leer(
            requireContext(),
            onEstado = { estado ->
                if (!isAdded) return@leer
                view.findViewById<ImageView>(R.id.imgSysRobotHome)
                    .setImageDrawable(RobotVisual.imagen(requireContext(), estado.niveles))
                view.findViewById<TextView>(R.id.tvEstadoSysRobot).text =
                    "🪙 ${estado.monedas} monedas · vence virus con lo que aprendes"
            },
            onError = { }
        )

        cardPalabraDia.setOnClickListener {

            abrirPalabraDelDia()

        }

        return view

    }

    override fun onResume() {
        super.onResume()

        // Se refresca cada vez que el usuario vuelve a Home
        // (por ejemplo, al regresar de haber jugado).
        if (::tvEstadoPalabraDia.isInitialized) {
            actualizarEstadoPalabraDia()
        }
    }

    /**
     * Muestra en la tarjeta si el usuario ya jugó hoy, o su racha actual.
     */
    private fun actualizarEstadoPalabraDia() {

        val yaJugoHoy = PalabraDelDiaManager.yaJugoHoy(requireContext())
        val stats = PalabraDelDiaManager.obtenerEstadisticas(requireContext())

        tvEstadoPalabraDia.text = when {
            yaJugoHoy && stats.rachaActual > 0 ->
                "¡Ya completaste el reto de hoy! 🔥 Racha: ${stats.rachaActual} días. Vuelve mañana."

            yaJugoHoy ->
                "¡Ya jugaste la Palabra del Día! Vuelve mañana para un nuevo reto."

            stats.rachaActual > 0 ->
                "🔥 Racha de ${stats.rachaActual} días "

            else ->
                "Toca para jugar"
        }
    }

    //barra de progreso home
    private fun cargarProgresoEnHome() {

        val uid = auth.currentUser?.uid ?: run {
            progresoCurso.progress = 0
            tvPorcentaje.text = "0 % completado"
            return
        }

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (!isAdded) return@addOnSuccessListener

                val progreso = document.get("progreso") as? Map<*, *>

                val cursosCompletados = progreso?.count { entrada ->
                    val datos = entrada.value as? Map<*, *>
                    datos?.get("completado") == true
                } ?: 0

                val totalCursos = Cursos.todosLosCursos.size

                val porcentaje = if (totalCursos > 0) {
                    (cursosCompletados * 100) / totalCursos
                } else {
                    0
                }

                progresoCurso.max = 100
                progresoCurso.progress = porcentaje

                tvPorcentaje.text = "$porcentaje % completado"
                tvCursoActual.text = "$cursosCompletados de $totalCursos cursos terminados"
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener

                progresoCurso.progress = 0
                tvPorcentaje.text = "0 % completado"
            }
    }

// Obtiene la categoría de un curso


    private fun obtenerCategoria(curso: Curso): String {

        return when {

            Cursos.listaOfimatica.contains(curso) -> "📄 Ofimática"

            Cursos.listaRedes.contains(curso) -> "🌐 Redes"

            Cursos.listaSeguridad.contains(curso) -> "🛡 Seguridad"

            Cursos.listaProgramacion.contains(curso) -> "💻 Programación"

            else -> "📚 Guía de aprendizaje"

        }

    }



//====================================================
// Cargar usuario
//====================================================

    private fun cargarUsuario() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()

            .addOnSuccessListener { document ->

                if (!isAdded) return@addOnSuccessListener

                val nombre =
                    document.getString("nombre") ?: "Estudiante"

                tvSaludo.text = "Hola, $nombre 👋"

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

}