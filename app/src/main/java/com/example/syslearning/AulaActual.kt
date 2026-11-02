package com.example.syslearning

object AulaActual {

    var id: String = ""

    var codigo: String = ""

    var nombre: String = ""

    fun limpiar() {
        id = ""
        codigo = ""
        nombre = ""
    }

    fun hayFiltro(): Boolean {
        return codigo.isNotEmpty()
    }

}