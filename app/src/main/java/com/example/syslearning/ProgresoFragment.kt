package com.example.syslearning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.example.syslearning.game.PalabraDelDiaManager
import com.example.syslearning.actividades.SesionActividadesManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ProgresoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progresoGeneral: ProgressBar
    private lateinit var tvPorcentaje: TextView
    private lateinit var tvCursosCompletados: TextView
    private lateinit var tvUltimoCurso: TextView
    private lateinit var tvCategoriaFavorita: TextView
    private lateinit var tvLogros: TextView
    private lateinit var tvNumeroCursos: TextView

    // Estadísticas de "La Palabra del Día"
    private lateinit var tvPalabraDiaJugadas: TextView
    private lateinit var tvPalabraDiaGanadas: TextView
    private lateinit var tvPalabraDiaRacha: TextView
    private lateinit var tvPalabraDiaRachaMax: TextView
    private lateinit var rvRanking: RecyclerView
    private lateinit var tvSinRanking: TextView
    private lateinit var rankingAdapter: RankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_progreso,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        progresoGeneral = view.findViewById(R.id.progresoGeneral)
        tvPorcentaje = view.findViewById(R.id.tvPorcentaje)
        tvCursosCompletados = view.findViewById(R.id.tvCursosCompletados)
        tvUltimoCurso = view.findViewById(R.id.tvUltimoCurso)
        tvCategoriaFavorita = view.findViewById(R.id.tvCategoriaFavorita)
        tvLogros = view.findViewById(R.id.tvLogros)
        tvNumeroCursos = view.findViewById(R.id.tvNumeroCursos)

        tvPalabraDiaJugadas = view.findViewById(R.id.tvPalabraDiaJugadas)
        tvPalabraDiaGanadas = view.findViewById(R.id.tvPalabraDiaGanadas)
        tvPalabraDiaRacha = view.findViewById(R.id.tvPalabraDiaRacha)
        tvPalabraDiaRachaMax = view.findViewById(R.id.tvPalabraDiaRachaMax)
        rvRanking = view.findViewById(R.id.rvRanking)
        tvSinRanking = view.findViewById(R.id.tvSinRanking)

        rankingAdapter = RankingAdapter()

        rvRanking.layoutManager = LinearLayoutManager(requireContext())
        rvRanking.adapter = rankingAdapter
        rvRanking.isNestedScrollingEnabled = false

        cargarProgreso()
        cargarRanking()
        cargarEstadisticasPalabraDia()

        return view
    }

    override fun onResume() {
        super.onResume()

        // Por si el usuario viene de jugar "La Palabra del Día":
        // refresca tanto los números (racha/jugadas) como la lista de
        // logros (que ahora también depende de la racha máxima).
        if (::tvPalabraDiaJugadas.isInitialized) {
            cargarEstadisticasPalabraDia()
        }

        if (::tvLogros.isInitialized) {
            cargarProgreso()
        }
    }

    /**
     * Estadísticas locales del juego (no dependen de Firestore).
     */
    private fun cargarEstadisticasPalabraDia() {

        val stats = PalabraDelDiaManager.obtenerEstadisticas(requireContext())

        tvPalabraDiaJugadas.text = stats.totalJugados.toString()
        tvPalabraDiaGanadas.text = stats.totalGanados.toString()
        tvPalabraDiaRacha.text = stats.rachaActual.toString()
        tvPalabraDiaRachaMax.text = "Mejor racha: ${stats.rachaMaxima} días"
    }

    private fun cargarProgreso() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (!isAdded) return@addOnSuccessListener

                val progreso = document.get("progreso") as? Map<*, *>

                val cursosCompletados = progreso?.count {

                    val datos = it.value as? Map<*, *>

                    datos?.get("completado") == true

                } ?: 0

                val logros = mutableListOf<String>()

                if (cursosCompletados >= 1) {
                    logros.add("🥉 Primer curso completado")
                }

                if (cursosCompletados >= 5) {
                    logros.add("🥈 Aprendiz constante")
                }

                if (cursosCompletados >= 10) {
                    logros.add("🥇 Estudiante avanzado")
                }

                if (cursosCompletados == Cursos.todosLosCursos.size) {
                    logros.add("🏆 Experto SysLearning")
                }

                // ------------------------------------------------------
                // Logros de racha en "La Palabra del Día".
                //
                // Se basan en la racha MÁXIMA (no la actual), así que
                // una vez desbloqueados quedan para siempre, aunque
                // después el usuario pierda la racha por no jugar un día.
                // ------------------------------------------------------

                val statsPalabraDelDia = PalabraDelDiaManager.obtenerEstadisticas(requireContext())

                if (statsPalabraDelDia.rachaMaxima >= 3) {
                    logros.add("🔥 Racha de 3 días en La Palabra del Día")
                }

                if (statsPalabraDelDia.rachaMaxima >= 10) {
                    logros.add("🔥🔥 Racha de 10 días en La Palabra del Día")
                }

                if (statsPalabraDelDia.rachaMaxima >= 30) {
                    logros.add("🔥🔥🔥 Racha de 30 días en La Palabra del Día")
                }

                // ------------------------------------------------------
                // Insignias por actividades (sesiones de preguntas)
                // completadas. Se basan en el total acumulado, así que
                // también quedan para siempre una vez desbloqueadas.
                // ------------------------------------------------------

                val statsActividades =com.example.syslearning.actividades.SesionActividadesManager
                        .obtenerEstadisticas(requireContext())

                if (statsActividades.totalCompletadas >= 1) {
                    logros.add("📘 Primera actividad completada")
                }

                if (statsActividades.totalCompletadas >= 5) {
                    logros.add("📗 5 actividades completadas")
                }

                if (statsActividades.totalCompletadas >= 10) {
                    logros.add("📕 10 actividades completadas")
                }

                val totalCursos = Cursos.todosLosCursos.size

                val porcentaje =
                    if (totalCursos > 0)
                        (cursosCompletados * 100) / totalCursos
                    else
                        0

                progresoGeneral.max = 100
                progresoGeneral.progress = porcentaje

                tvPorcentaje.text = "$porcentaje% completado"

                tvCursosCompletados.text =
                    "$cursosCompletados de $totalCursos cursos completados"

                tvNumeroCursos.text = cursosCompletados.toString()

                var ultimoCurso = "Ninguno"
                var ultimaFecha: Timestamp? = null

                var ofimatica = 0
                var redes = 0
                var seguridad = 0
                var programacion = 0

                progreso?.forEach { entrada ->

                    val datos = entrada.value as? Map<*, *> ?: return@forEach

                    val fecha = datos["fecha"] as? Timestamp

                    if (fecha != null) {

                        if (ultimaFecha == null || fecha.seconds > ultimaFecha!!.seconds) {
                            ultimaFecha = fecha
                            ultimoCurso = entrada.key.toString()
                        }
                    }

                    if (datos["completado"] == true) {

                        val nombreCurso = entrada.key.toString()

                        when {

                            Cursos.listaOfimatica.any { it.titulo == nombreCurso } ->
                                ofimatica++

                            Cursos.listaRedes.any { it.titulo == nombreCurso } ->
                                redes++

                            Cursos.listaSeguridad.any { it.titulo == nombreCurso } ->
                                seguridad++

                            Cursos.listaProgramacion.any { it.titulo == nombreCurso } ->
                                programacion++
                        }
                    }
                }

                val categoriaFavorita = if (
                    ofimatica == 0 &&
                    redes == 0 &&
                    seguridad == 0 &&
                    programacion == 0
                ) {
                    "Sin información"
                } else {

                    when (maxOf(ofimatica, redes, seguridad, programacion)) {
                        ofimatica -> "📄 Ofimática"
                        redes -> "🌐 Redes"
                        seguridad -> "🛡 Seguridad"
                        else -> "💻 Programación"
                    }
                }

                tvUltimoCurso.text = "📚 : $ultimoCurso"
                tvCategoriaFavorita.text = "Categoría favorita: $categoriaFavorita"
                tvLogros.text = if (logros.isEmpty()) "Ninguno" else logros.joinToString("\n")
            }


    }

    /**
     * Antes de pedir el ranking, se lee el propio documento del usuario
     * para saber si pertenece a alguna aula:
     *  - Si tiene aula(s): el ranking se filtra a su primera aula
     *    (mismos compañeros de clase).
     *  - Si NO tiene aula: el ranking se filtra a otros usuarios que
     *    tampoco tienen aula asignada (no se mezcla con estudiantes
     *    inscritos en alguna).
     */
    private fun cargarRanking() {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            cargarRankingConFiltro(codigoAula = null, soloSinAula = true)
            return
        }

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (!isAdded) return@addOnSuccessListener

                val misAulas = (documento.get("aulas") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()

                if (misAulas.isNotEmpty()) {
                    cargarRankingConFiltro(codigoAula = misAulas.first(), soloSinAula = false)
                } else {
                    cargarRankingConFiltro(codigoAula = null, soloSinAula = true)
                }
            }
            .addOnFailureListener {

                if (!isAdded) return@addOnFailureListener

                // Si no se pudo leer el propio documento, se muestra
                // al menos el grupo de "sin aula" en vez de fallar del todo.
                cargarRankingConFiltro(codigoAula = null, soloSinAula = true)
            }
    }

    private fun cargarRankingConFiltro(codigoAula: String?, soloSinAula: Boolean) {

        RankingRepository.obtenerRanking(

            db = db,

            codigoAula = codigoAula,

            soloSinAula = soloSinAula,

            onSuccess = { ranking ->

                if (!isAdded) return@obtenerRanking

                rankingAdapter.actualizarLista(ranking)

                tvSinRanking.text = if (codigoAula != null) {
                    "Ningún compañero de tu aula tiene datos todavía."
                } else {
                    "No hay otros usuarios sin aula todavía."
                }

                tvSinRanking.visibility =
                    if (ranking.isEmpty())
                        View.VISIBLE
                    else
                        View.GONE

            },

            onError = {

                if (!isAdded) return@obtenerRanking

                tvSinRanking.text = "No fue posible cargar la tabla"

                tvSinRanking.visibility = View.VISIBLE

            }

        )

    }
}