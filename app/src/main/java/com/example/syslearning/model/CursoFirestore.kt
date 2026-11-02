package com.example.syslearning.model

data class CursoFirestore(

    var id: String = "",

    var titulo: String = "",

    var descripcion: String = "",

    var contenido: String = "",

    var datoImportante: String = "",

    var sabiasQue: String = "",

    var consejo: String = "",

    var categoria: String = "",

    var nivel: String = "",

    var imagen: String = "",

    var pdf: String = "",

    var video: String = "",

    var creadoPor: String = "",

    var fechaCreacion: Long = System.currentTimeMillis(),

    var estado: Boolean = true,

    /**
     * Códigos de las aulas para las que se creó este curso (ej. "SYS-SMQM9").
     * Lista vacía = curso GLOBAL, visible para todos los estudiantes
     * sin importar su aula. Esto mantiene compatibilidad con los cursos
     * que ya existen en Firestore (no tienen este campo -> se leen como
     * lista vacía -> siguen siendo globales).
     */
    var aulasAsignadas: List<String> = emptyList()

)