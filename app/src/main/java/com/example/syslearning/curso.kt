package com.example.syslearning

import java.io.Serializable

data class Curso(

    val titulo: String,

    val descripcion: String,

    val datoImportante: String,

    val contenido: String,

    val imagen: Int,

    val imagenUrl: String = "",

    val materialUrl: String = "",

    val sabiasQue: String,
    val consejo: String,
    val pdf: String,
    val video: String,


) : Serializable