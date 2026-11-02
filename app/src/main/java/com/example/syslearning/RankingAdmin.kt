package com.example.syslearning

import com.google.firebase.firestore.FirebaseFirestore

object RankingRepository {

    /**
     * @param codigoAula si no es null, solo incluye usuarios cuyo campo
     * "aulas" (lista de códigos) contiene ese código.
     * @param soloSinAula si es true (y codigoAula es null), solo incluye
     * usuarios cuyo campo "aulas" está vacío. Se ignora si codigoAula
     * no es null.
     *
     * Si ambos quedan en sus valores por defecto (null / false), no se
     * aplica ningún filtro de aula (comportamiento original: todos).
     */
    fun obtenerRanking(
        db: FirebaseFirestore,
        codigoAula: String? = null,
        soloSinAula: Boolean = false,
        onSuccess: (List<RankingUsuario>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection("usuarios")
            .get()
            .addOnSuccessListener { resultado ->

                val ranking = resultado.documents
                    .filter { documento ->
                        !documento.getString("rol").equals("admin", ignoreCase = true)
                    }
                    .filter { documento ->

                        val aulas = (documento.get("aulas") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?: emptyList()

                        when {
                            codigoAula != null -> aulas.contains(codigoAula)
                            soloSinAula -> aulas.isEmpty()
                            else -> true
                        }
                    }
                    .map { documento ->

                        val nombre = documento.getString("nombre")
                            ?: documento.getString("correo")
                                ?.substringBefore("@")
                            ?: "Usuario"

                        val progreso =
                            documento.get("progreso") as? Map<*, *>

                        val cursosCompletados = progreso?.count { entrada ->
                            val datosCurso =
                                entrada.value as? Map<*, *>

                            datosCurso?.get("completado") == true
                        } ?: 0

                        val palabraDelDia =
                            documento.get("palabraDelDia") as? Map<*, *>

                        val racha =
                            (palabraDelDia?.get("racha") as? Number)
                                ?.toInt() ?: 0

                        val totalJugados =
                            (palabraDelDia?.get("totalJugados") as? Number)
                                ?.toInt() ?: 0

                        val totalIntentos =
                            (palabraDelDia?.get("totalIntentos") as? Number)
                                ?.toInt() ?: 0

                        val totalGanados =
                            (palabraDelDia?.get("totalGanados") as? Number)
                                ?.toInt() ?: 0

                        //=========================
                        // PUNTOS
                        //=========================

                        val puntosRacha = racha * 15

                        val puntosCursos = cursosCompletados * 100

                        val puntosPalabras = totalGanados * 50

                        val bonusIntentos =
                            if (totalJugados > 0) {

                                ((7.0 - (totalIntentos.toDouble() / totalJugados)) * 20)
                                    .coerceAtLeast(0.0)
                                    .toInt()

                            } else {

                                0

                            }

                        // XP de las micro actividades (lo suma SesionActividadesManager)
                        val puntosActividades =
                            (documento.get("xpTotal") as? Number)?.toInt() ?: 0

                        val puntos =
                            puntosRacha +
                                    puntosCursos +
                                    puntosPalabras +
                                    bonusIntentos +
                                    puntosActividades

                        RankingUsuario(
                            nombre = nombre,
                            racha = racha,
                            cursosCompletados = cursosCompletados,
                            puntos = puntos
                        )

                    }
                    .sortedByDescending { it.puntos }

                onSuccess(ranking)

            }
            .addOnFailureListener {

                onError(it)

            }

    }

}