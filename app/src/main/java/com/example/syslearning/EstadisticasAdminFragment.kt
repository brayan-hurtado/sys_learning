package com.example.syslearning

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class EstadisticasAdminFragment : Fragment(R.layout.fragment_estadisticas_admin) {

    private lateinit var db: FirebaseFirestore

    private lateinit var rvRanking: RecyclerView
    private lateinit var tvSinRanking: TextView
    private lateinit var rankingAdapter: RankingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvRanking = view.findViewById(R.id.rvRanking)
        tvSinRanking = view.findViewById(R.id.tvSinRanking)

        rankingAdapter = RankingAdapter()

        rvRanking.layoutManager = LinearLayoutManager(requireContext())
        rvRanking.adapter = rankingAdapter
        rvRanking.isNestedScrollingEnabled = false

        cargarRanking()
    }

    private fun cargarRanking() {

        val codigoAula = if (AulaActual.hayFiltro()) AulaActual.codigo else null

        RankingRepository.obtenerRanking(

            db = db,

            codigoAula = codigoAula,

            onSuccess = { ranking ->

                if (!isAdded) return@obtenerRanking

                rankingAdapter.actualizarLista(ranking)

                tvSinRanking.text = if (codigoAula != null) {
                    "Ningún estudiante de esta aula tiene datos todavía."
                } else {
                    "No hay datos para mostrar todavía."
                }

                tvSinRanking.visibility =
                    if (ranking.isEmpty())
                        View.VISIBLE
                    else
                        View.GONE

            },

            onError = {

                if (!isAdded) return@obtenerRanking

                tvSinRanking.text = "No fue posible cargar la tabla."

                tvSinRanking.visibility = View.VISIBLE

            }

        )

    }

}