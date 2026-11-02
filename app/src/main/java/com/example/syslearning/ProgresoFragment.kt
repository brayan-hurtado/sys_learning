package com.example.syslearning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.example.syslearning.game.PalabraDelDiaManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.example.syslearning.model.Actividad
import com.example.syslearning.model.CursoFirestore
import com.example.syslearning.util.FiltroAula
import com.example.syslearning.util.LecturaRapida
import com.example.syslearning.util.Puntuacion
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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

    // Gamificación: actividades, nivel y XP
    private lateinit var tvActividadesGeneral: TextView
    private lateinit var tvNumeroActividades: TextView
    private lateinit var tvDetalleActividades: TextView
    private lateinit var tvNivel: TextView
    private lateinit var tvXpTotal: TextView
    private lateinit var progresoNivel: ProgressBar
    private lateinit var tvXpSiguienteNivel: TextView
    private lateinit var tvMiPuesto: TextView
    private lateinit var tvAlcanceRanking: TextView
    private lateinit var vistaRaiz: View

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

        tvActividadesGeneral = view.findViewById(R.id.tvActividadesCompletadasGeneral)
        tvNumeroActividades = view.findViewById(R.id.tvNumeroActividades)
        tvDetalleActividades = view.findViewById(R.id.tvDetalleActividades)
        tvNivel = view.findViewById(R.id.tvNivel)
        tvXpTotal = view.findViewById(R.id.tvXpTotal)
        progresoNivel = view.findViewById(R.id.progresoNivel)
        tvXpSiguienteNivel = view.findViewById(R.id.tvXpSiguienteNivel)
        tvMiPuesto = view.findViewById(R.id.tvMiPuesto)
        tvAlcanceRanking = view.findViewById(R.id.tvAlcanceRanking)
        vistaRaiz = view

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

        cargarEstadisticasPalabraDia()

        // cargarProgreso() y cargarRanking() se llaman en onResume, que
        // también corre al volver de una actividad.
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
            cargarRanking()
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

    /**
     * Progreso general = cursos leídos + actividades dominadas, sobre el
     * total de cursos y actividades que el estudiante puede ver (globales y
     * de sus aulas). Se piden en paralelo el usuario, las actividades y los
     * cursos; sin internet se usan las copias del dispositivo.
     */
    private fun cargarProgreso() {

        val uid = auth.currentUser?.uid ?: return
        val contexto = context ?: return

        var documentoUsuario: DocumentSnapshot? = null
        var actividades: List<Actividad>? = null
        var cursosFirestore: List<CursoFirestore>? = null

        fun intentarMostrar() {
            val doc = documentoUsuario ?: return
            val listaActividades = actividades ?: return
            val listaCursos = cursosFirestore ?: return
            if (isAdded) mostrarProgreso(doc, listaActividades, listaCursos)
        }

        LecturaRapida.documento(
            context = contexto,
            referencia = db.collection("usuarios").document(uid),
            onDatos = { doc, _ ->
                documentoUsuario = doc
                intentarMostrar()
            },
            onError = { }
        )

        LecturaRapida.consulta(
            context = contexto,
            query = db.collection("actividades").whereEqualTo("activo", true),
            refrescar = false,
            onDatos = { documentos, _ ->
                actividades = documentos.mapNotNull { doc ->
                    runCatching {
                        doc.toObject(Actividad::class.java).apply { id = doc.id }
                    }.getOrNull()
                }
                intentarMostrar()
            },
            onError = {
                actividades = emptyList()
                intentarMostrar()
            }
        )

        LecturaRapida.consulta(
            context = contexto,
            query = db.collection("cursos"),
            refrescar = false,
            onDatos = { documentos, _ ->
                cursosFirestore = documentos.mapNotNull { doc ->
                    runCatching { doc.toObject(CursoFirestore::class.java) }.getOrNull()
                }
                intentarMostrar()
            },
            onError = {
                cursosFirestore = emptyList()
                intentarMostrar()
            }
        )
    }

    private fun mostrarProgreso(
        document: DocumentSnapshot,
        todasLasActividades: List<Actividad>,
        todosLosCursosFirestore: List<CursoFirestore>
    ) {

        val misAulas = FiltroAula.aulasDe(document)

        // ---- Cursos: los del sistema + los del administrador visibles para su aula ----
        val titulosCursos = mutableSetOf<String>()
        Cursos.todosLosCursos.forEach { titulosCursos.add(it.titulo) }
        todosLosCursosFirestore
            .filter { FiltroAula.esVisible(it.aulasAsignadas, misAulas) }
            .forEach { titulosCursos.add(it.titulo) }

        val progreso = document.get("progreso") as? Map<*, *>

        val cursosCompletados = progreso?.count {

            val datos = it.value as? Map<*, *>

            datos?.get("completado") == true && titulosCursos.contains(it.key.toString())

        } ?: 0

        val totalCursos = titulosCursos.size

        // ---- Actividades visibles para su aula ----
        val actividadesVisibles = todasLasActividades
            .filter { FiltroAula.esVisible(it.aulasAsignadas, misAulas) }
        val idsVisibles = actividadesVisibles.map { it.id }.toSet()

        val resumen = Puntuacion.resumirActividades(
            document.get("actividadesProgreso") as? Map<*, *>
        )

        val actividadesCompletadas = resumen.idsCompletadas.count { it in idsVisibles }
        val totalActividades = actividadesVisibles.size

        // ---- Barra de progreso general ----
        val porcentaje = Puntuacion.porcentajeGeneral(
            completados = cursosCompletados + actividadesCompletadas,
            total = totalCursos + totalActividades
        )

        progresoGeneral.max = 100
        progresoGeneral.progress = porcentaje

        tvPorcentaje.text = "$porcentaje% completado"

        tvCursosCompletados.text =
            "📚 $cursosCompletados de $totalCursos cursos completados"

        tvActividadesGeneral.text =
            "🧩 $actividadesCompletadas de $totalActividades actividades completadas"

        tvNumeroCursos.text = cursosCompletados.toString()

        // ---- Tarjeta de actividades ----
        tvNumeroActividades.text = actividadesCompletadas.toString()
        tvDetalleActividades.text = if (resumen.intentos > 0) {
            "de $totalActividades · ${resumen.intentos} intentos · 🎯 ${resumen.precision} %"
        } else {
            "de $totalActividades · aún sin intentos"
        }

        // ---- Nivel y XP ----
        val xp = (document.get("xpTotal") as? Number)?.toInt() ?: 0
        val nivel = Puntuacion.nivel(xp)

        tvNivel.text = "Nivel ${nivel.numero}"
        tvXpTotal.text = "$xp XP"
        progresoNivel.max = Puntuacion.XP_POR_NIVEL
        progresoNivel.progress = nivel.xpEnNivel
        tvXpSiguienteNivel.text = "${nivel.xpParaSubir} XP para el nivel ${nivel.numero + 1}"

        // ---- Logros ----
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

        if (totalCursos > 0 && cursosCompletados == totalCursos) {
            logros.add("🏆 Experto SysLearning")
        }

        // Logros de racha en "La Palabra del Día" (racha MÁXIMA:
        // una vez desbloqueados quedan para siempre).
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

        // Insignias de micro actividades: se leen de Firestore (no del
        // celular), así se conservan aunque cambie de dispositivo.
        if (resumen.completadas >= 1) {
            logros.add("📘 Primera actividad completada")
        }

        if (resumen.completadas >= 5) {
            logros.add("📗 5 actividades completadas")
        }

        if (resumen.completadas >= 10) {
            logros.add("📕 10 actividades completadas")
        }

        if (resumen.intentos >= 5 && resumen.precision >= 80) {
            logros.add("🎯 Precisión del 80 % o más")
        }

        if (totalActividades > 0 && actividadesCompletadas == totalActividades) {
            logros.add("🧠 Todas las actividades dominadas")
        }

        if (nivel.numero >= 2) {
            logros.add("⭐ Nivel ${nivel.numero} alcanzado")
        }

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

        val contexto = context ?: return

        LecturaRapida.documento(
            context = contexto,
            referencia = db.collection("usuarios").document(uid),
            onDatos = { documento, _ ->

                if (!isAdded) return@documento

                val misAulas = (documento.get("aulas") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()

                if (misAulas.isNotEmpty()) {
                    cargarRankingConFiltro(codigoAula = misAulas.first(), soloSinAula = false)
                } else {
                    cargarRankingConFiltro(codigoAula = null, soloSinAula = true)
                }
            },
            onError = {

                if (!isAdded) return@documento

                // Si no se pudo leer el propio documento, se muestra
                // al menos el grupo de "sin aula" en vez de fallar del todo.
                cargarRankingConFiltro(codigoAula = null, soloSinAula = true)
            }
        )
    }

    private fun cargarRankingConFiltro(codigoAula: String?, soloSinAula: Boolean) {

        RankingRepository.obtenerRanking(

            db = db,

            codigoAula = codigoAula,

            soloSinAula = soloSinAula,

            context = context,

            onSuccess = { ranking ->

                if (!isAdded) return@obtenerRanking

                val uidActual = auth.currentUser?.uid

                // Los 3 primeros van en el podio; el resto, en la lista.
                val enPodio = PodioRanking.mostrar(vistaRaiz, ranking, uidActual)
                rankingAdapter.actualizarLista(
                    ranking.drop(enPodio),
                    posicionInicial = enPodio + 1,
                    uidActual = uidActual
                )

                // Puesto del usuario actual, en una etiqueta del encabezado.
                val miPuesto = ranking.indexOfFirst { it.uid == uidActual } + 1
                if (miPuesto > 0) {
                    tvMiPuesto.text = "Tu puesto: #$miPuesto"
                    tvMiPuesto.background = GradientDrawable().apply {
                        cornerRadius = 40f
                        setColor(Color.parseColor("#FFD54F"))
                    }
                    tvMiPuesto.visibility = View.VISIBLE
                } else {
                    tvMiPuesto.visibility = View.GONE
                }

                tvAlcanceRanking.text = if (codigoAula != null) {
                    "Compañeros de tu aula"
                } else {
                    "Estudiantes sin aula asignada"
                }

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