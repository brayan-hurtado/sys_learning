package com.example.syslearning.model

/**
 * Pregunta de una sesión de actividad, guardada en Firestore
 * (colección "preguntas"). Cada una pertenece a una actividad
 * (actividadId = el título de la actividad, ej. "Introducción").
 */
data class Pregunta(

    var id: String = "",

    var actividadId: String = "",

    var enunciado: String = "",

    var opcionA: String = "",

    var opcionB: String = "",

    /** "A" o "B" */
    var respuestaCorrecta: String = "A",

    var explicacion: String = "",

    var estado: Boolean = true

)