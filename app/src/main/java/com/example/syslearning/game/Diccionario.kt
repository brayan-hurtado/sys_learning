package com.example.syslearning.game

/**
 * Diccionario interno de "La Palabra del Día".
 *
 * Esta misma lista se usa para:
 *  1) Elegir la palabra del día (según la fecha).
 *  2) Validar si la palabra escrita por el usuario "existe".
 */
object Diccionario {

    val palabras: List<String> = listOf(

        // ----- 5 letras -----
        "AULAS","LIBRO","LAPIZ","HOJAS","NOTAS",
        "TAREA","TEXTO","DATOS","REDES","LOGRO",
        "ERROR","NIVEL","RATON","TECLA","CABLE",
        "DISCO","VIDEO","AUDIO","NUBES","LOGIN",
        "FOROS","AYUDA","PANEL","CURSO","CLASE",
        "EXCEL","LINUX","MOUSE","FIBRA","GUIAS",
        "EXITO","BOTON","CLICK","COPIA","PEGAR",
        "VIRUS","TOKEN","EMAIL","NODOS","CACHE",
        "PIXEL","TARDE","RAMAS","BUCLE","ARRAY",
        "ADOBE","SMART","CLAVE","LOGIN","USADO",
        "ENERO","BORDE","ICONO","TEMAS","FONDO",
        "VISTA","PAGOS","VOLVER","LUCES","TABLA",
        "COLOR","TINTA","LENTE","AUDIO","VIDEO",

        // ----- 6 letras -----
        "CODIGO","PYTHON","KOTLIN","OBJETO","METODO",
        "PRUEBA","ROUTER","SWITCH","BACKUP","SCRIPT",
        "EDITOR","BUFFER","MATRIZ","VECTOR","LOGICA",
        "TABLAS","MANUAL","SESION","CURSOS","NUMERO",
        "ALUMNO","MODULO","LECTOR","TECLAS","MATRIZ",
        "LECTOR","TEORIA","PRUEBA","FILTRO","CARPETA",
        "ENLACE","NOMBRE","CUENTA","DISEÑO","COPIAR",
        "PEGADO","CELULA","MENTOR","PUENTE","OBJETO",
        "CLASES","FICHAS","ESCALA","FORMAS","BLOQUE",
        "CODIGO","SALIDA","ENTRAR","LISTAS","RUTINA",
        "EDITOR","FILTRO","GRUPOS","TEORIA","PREMIO",

        // ----- 7 letras -----
        "PALABRA","ESTUDIO","ARCHIVO","SISTEMA",
        "MOCHILA","PIZARRA","FUNCION","MEMORIA",
        "USUARIO","DOCENTE","TECLADO","PROCESO",
        "CONSOLA","MODULOS","APRENDE","MODELOS",
        "VARIABLE","COMPILA","DEPURAR","MONITOR",
        "ROUTERS","SERVICE","VIRTUAL","PUNTAJE",
        "PROCESO","COMPUTO","SERVIDOR","INTERNO",
        "PROGRAM","FIREFOX","ENTRADA","WINDOWS",
        "FIREWAL","ETHERNET","IMPRESO","LECTURA",
        "ESCRIBO","TEMARIO","PRACTIC","MATERIAL",
        "TUTORIA","RECURSO","CARPETA","SOFTWARE",
        "RETORNO","CONECTA","NAVEGAR","FORMATO",
        "ANDROID","TECNICO","ENRUTADO","SINTAXIS",
        "SOPORTE","FICHERO","COMANDO","TERMINA"

    ).map { it.uppercase() }
}