package com.example.syslearning

import android.content.Context
import com.example.syslearning.util.LecturaRapida
import com.example.syslearning.util.Puntuacion
import com.google.firebase.firestore.Source
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
        onError: (Exception) -> Unit,
        /** Con context, sin internet se usa la copia del dispositivo al instante. */
        context: Context? = null
    ) {

        val usuarios = db.collection("usuarios")

        val tarea = if (context != null && !LecturaRapida.hayConexion(context)) {
            usuarios.get(Source.CACHE)
        } else {
            usuarios.get()
        }

        tarea
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

                        // Micro actividades: dominadas, intentos y XP
                        // (los escribe SesionActividadesManager).
                        val actividades = Puntuacion.resumirActividades(
                            documento.get("actividadesProgreso") as? Map<*, *>
                        )

                        val xp = (documento.get("xpTotal") as? Number)?.toInt() ?: 0

                        //=========================
                        // PUNTOS (reglas en Puntuacion.kt)
                        //=========================

                        val puntos = Puntuacion.puntos(
                            cursosCompletados = cursosCompletados,
                            actividades = actividades,
                            xpActividades = xp,
                            rachaPalabra = racha,
                            palabrasGanadas = totalGanados,
                            palabrasJugadas = totalJugados,
                            intentosPalabra = totalIntentos
                        )

                        RankingUsuario(
                            nombre = nombre,
                            racha = racha,
                            cursosCompletados = cursosCompletados,
                            puntos = puntos,
                            uid = documento.id,
                            actividadesCompletadas = actividades.completadas,
                            intentosActividades = actividades.intentos,
                            precision = actividades.precision,
                            xp = xp
                        )

                    }
                    .sortedWith(
                        compareByDescending<RankingUsuario> { it.puntos }
                            .thenByDescending { it.actividadesCompletadas }
                            .thenBy { it.nombre.lowercase() }
                    )

                onSuccess(ranking)

            }
            .addOnFailureListener {

                onError(it)

            }

    }

}