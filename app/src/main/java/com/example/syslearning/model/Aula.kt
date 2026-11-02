package com.example.syslearning.model

data class Aula(

    var id: String = "",

    var nombre: String = "",

    var codigo: String = "",

    var adminUid: String = "",

    var fechaCreacion: Long = System.currentTimeMillis(),

    var estado: Boolean = true

)