package com.example.syslearning.model

data class Usuario(

    var uid: String = "",

    var nombre: String = "",

    var correo: String = "",

    var rol: String = "",

    var foto: String = "",

    var aulas: List<String> = emptyList()

)