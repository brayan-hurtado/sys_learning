package com.example.syslearning

object Cursos {

    val redesInternetNivel2 = Curso(
        titulo = "Redes 2: Cómo Viajan los Datos (Paquetes y Protocolos)",
        descripcion = "Descubre el fascinante viaje que hace un mensaje de WhatsApp desde que presionas 'Enviar' hasta que llega al celular de tu amigo.",
        contenido = """
        Cuando envías una foto o un video por internet, la información no viaja como un solo bloque gigante a través de los cables. Si fuera así, un video pesado atascaría toda la red y nadie más podría usar el internet al mismo tiempo. 
        
        📦 1. ¿Qué es un Paquete de Datos?
        
        Para evitar atascos, las computadoras hacen algo brillante: dividen cualquier archivo (una canción, un texto o una imagen) en miles de pedacitos diminutos llamados 'Paquetes de Datos'.
        
        • La Etiqueta de Envío: Cada pequeño paquete lleva una etiqueta digital invisible con tres datos cruciales: la dirección IP de quién lo envía, la dirección IP de quién lo recibe, y el número de pedacito que es (ej. 'Paquete 4 de 500').
        • El Viaje de los Paquetes: Los paquetes se lanzan al internet y pueden tomar caminos completamente diferentes. Algunos viajarán por un cable submarino bajo el océano y otros por satélite. Los equipos de red llamados 'Routers' deciden en milisegundos cuál es la ruta más rápida y libre para cada pedacito.
        
        🧩 2. El Protocolo TCP/IP (El Rompecabezas)
        
        Cuando todos los paquetes llegan por fin al celular de destino, entran las reglas universales del internet, conocidas como el protocolo TCP/IP.
        
        • Reensamblaje: El celular receptor lee las etiquetas de los paquetes, los ordena en su secuencia correcta (del 1 al 500) y vuelve a armar el archivo original para que puedas ver la foto en pantalla.
        
        • Control de Errores: Si durante el viaje el paquete número 14 se perdió por una mala señal, el protocolo TCP le avisa de inmediato al emisor: 'Oye, no me llegó el 14, reenvíamelo'. La app no te muestra la foto hasta que el rompecabezas esté 100% completo.
    """.trimIndent(),
        datoImportante = "Debido a que los paquetes se transmiten a la velocidad de la luz a través de cables de fibra óptica, este proceso de dividir, enviar por rutas distintas, revisar errores y rearmar un archivo tarda apenas unas pocas milésimas de segundo.",
        sabiasQue = "Casi todo el internet mundial se sostiene gracias a cables submarinos de fibra óptica gigantescos que cruzan los océanos del planeta de un extremo a otro. Son tan gruesos como un brazo humano y están reforzados con capas de acero para soportar la presión marina y las mordeduras de tiburones.",
        consejo = "Cuando sientas que tu internet está intermitente o que los videos se detienen a la mitad, lo que está experimentando tu red se llama 'Pérdida de Paquetes'. Reiniciar tu router limpia la memoria de estos caminos y ayuda a que los paquetes vuelvan a fluir sin perderse.",
        imagen = R.drawable.img_redes,
        pdf = "https://tu-servidor.com/redes_nivel3.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val seguridadInternetNivel4 = Curso(
        titulo = "Seguridad 4: El Arte del Engaño (Phishing)",
        descripcion = "Aprende a detectar correos, mensajes y páginas web falsas que intentan robar tus contraseñas y datos personales.",
        contenido = """
        En los niveles anteriores aprendiste a crear contraseñas fuertes y a cuidar tus redes. Sin embargo, los criminales digitales han descubierto que es más fácil engañar a una persona que hackear un sistema seguro. A esto se le llama Ingeniería Social.
        
        🎣 1. ¿Qué es el Phishing?
        
        El Phishing (que suena como 'fishing' o pescar en inglés) es una técnica donde los atacantes lanzan un anzuelo digital esperando que caigas. 
        
        • ¿Cómo funciona? Te envían un correo electrónico, un mensaje de WhatsApp o un SMS haciéndose pasar por una entidad de confianza (tu banco, Netflix, la policía o una empresa de envíos).
        
        • El mensaje suele ser alarmante para que actúes rápido sin pensar (ej. 'Tu cuenta ha sido bloqueada', 'Alguien intentó ingresar a tu banca móvil' o 'Tienes un paquete retenido, paga aquí').
        
        🕵️‍♂️ 2. Las 3 Reglas de Oro para Detectar el Engaño
        
        Para no caer en la trampa, solo debes entrenar tu ojo con estos tres detalles:
        
        • Revisa el remitente real: El mensaje puede decir 'Banco Original', pero si miras el correo del remitente, suele ser algo extraño como 'seguridad-banco@XyZ-alerta.com' en lugar del dominio oficial.
        
        • El Sentido de Urgencia: Si te presionan diciendo que debes dar clic en las próximas 2 horas o perderás tu cuenta, desconfía de inmediato. Las empresas serias no actúan así.
        
        • Links Sospechosos: Nunca des clic directo. Si dejas el cursor sobre el enlace (o lo miras con atención en el celular), verás que la dirección web no es la real (ej. 'www.netflix-actualizacion.com' en lugar de 'www.netflix.com').
    """.trimIndent(),
        datoImportante = "Ningún banco, plataforma de streaming o red social te pedirá jamás tus contraseñas, pines o códigos de verificación por llamada, correo o mensaje de texto. Esos datos son privados y solo tú debes conocerlos.",
        sabiasQue = "El término 'Phishing' nació a mediados de los años 90. Los primeros ataques masivos de este tipo se hicieron en la plataforma AOL (America Online), donde los atacantes se hacían pasar por miembros del equipo de soporte para robarle las cuentas a los usuarios.",
        consejo = "Cuando recibas una alerta sospechosa de tu banco o de un servicio, cierra el mensaje, abre el navegador de tu celular y escribe tú mismo la dirección oficial de la página, o llama a las líneas de atención oficiales. Nunca uses los números ni los enlaces que vienen dentro del mensaje sospechoso.",
        imagen = R.drawable.img_seguridad_phishing,
        pdf = "https://tu-servidor.com/seguridad_nivel4.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val conceptosProgramacionNivel6 = Curso(
        titulo = "Fundamentos 6: Colecciones de Datos (Listas y Arreglos)",
        descripcion = "Aprende cómo almacenar y organizar cientos de datos juntos en un solo lugar utilizando listas inteligentes.",
        contenido = """
        Hasta ahora, cada variable que creábamos era como una cajita individual que solo guardaba un dato a la vez (un nombre, un número o un color). Pero, ¿qué pasa si tu aplicación tiene que manejar los nombres de 50 cursos o una lista de 100 alumnos? Crear 100 variables individuales sería un dolor de cabeza. Para eso existen los Arreglos (Arrays) y las Listas.
        
        🗂️ 1. ¿Qué es un Arreglo o Lista?
        
        Imagina una lista como un organizador de zapatos colgante o un casillero de correo con muchas divisiones numeradas. Sigue siendo una sola variable grande, pero por dentro está dividida en compartimentos donde puedes guardar muchos datos del mismo tipo en orden.
        
        
        • El Índice (La posición): Para poder sacar o cambiar un objeto de la lista, la computadora le asigna un número de posición a cada compartimento llamado 'Índice'. 
        
        • Regla de Oro: En programación, las computadoras SIEMPRE empiezan a contar desde el número 0. El primer elemento de tu lista estará en la posición 0, el segundo en la posición 1, el tercero en la 2, y así sucesivamente.
        
        🔄 2. El Superpoder: Combinar Listas con Bucles
        
        Las listas se vuelven verdaderamente poderosas cuando las unes con un bucle (como el ciclo 'Para' o 'For'). 
        
        • Le puedes ordenar a la computadora: 'Recorre esta lista desde la posición 0 hasta el final y muéstrame todos los elementos en la pantalla'. La app lo hará en milisegundos, sin importar si tienes 5 o 50,000 elementos guardados.
    """.trimIndent(),
        datoImportante = "En lenguajes modernos como Kotlin, existen dos tipos de listas: las Fijas (que no cambian de tamaño una vez creadas) y las Mutables (listas dinámicas a las que les puedes añadir, quitar o modificar elementos en tiempo real mientras el usuario usa la app).",
        sabiasQue = "El error más famoso al usar listas se llama 'Index Out of Bounds' (Índice fuera de rango). Ocurre cuando intentas buscar, por ejemplo, el elemento en la posición 5 de una lista que solo tiene 3 cosas guardadas. La app no encuentra nada en ese casillero y se cierra.",
        consejo = "Cuando nombres una variable que guarde una lista, escríbela siempre en plural (ej. 'cursos', 'alumnos', 'logros'). Así, cuando leas tu propio código meses después, sabrás al instante que esa variable guarda un conjunto de datos y no uno solo.",
        imagen = R.drawable.img_fundamentos_variables,
        pdf = "https://tu-servidor.com/fundamentos_programacion6.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val conceptosProgramacionNivel5 = Curso(
        titulo = "Fundamentos 5: El Poder de las Funciones",
        descripcion = "Aprende a organizar tu código en bloques inteligentes y reutilizables para escribir programas más limpios y ordenados.",
        contenido = """
        ¡Felicidades! Ya conoces las piezas sueltas del rompecabezas: variables, condiciones y bucles. El último paso de los fundamentos es aprender a agrupar esas piezas en bloques organizados llamados Funciones.
        
        ⚙️ 1. ¿Qué es una Función?
       
        Imagina una función como un botón de una máquina o un electrodoméstico en tu casa (como la licuadora). Tú no necesitas saber cómo giran los engranajes por dentro cada vez que la usas; simplemente presionas el botón 'Licuar' y la máquina hace su trabajo.
       
        • En programación, una función es un bloque de código con un nombre específico que realiza una tarea concreta. 
        
        • En lugar de escribir las mismas 10 líneas de código cada vez que quieres hacer algo, las metes dentro de una función y la llamas por su nombre cuando la necesites.
        
        📥 2. Entradas (Parámetros) y Salidas (Retornos)
       
        Las funciones más útiles trabajan procesando información como una pequeña fábrica:
        
        • Datos de entrada (Parámetros): Es lo que la función necesita para trabajar. Siguiendo el ejemplo de la licuadora, las entradas serían las frutas y la leche. En tu app, si haces una función para saludar, la entrada sería el 'nombreUsuario'.
        
        • El Proceso: El código interno que realiza la acción (licuar los ingredientes o armar el texto del saludo).
        
        • Datos de salida (Retorno): Es el resultado final que la función te devuelve cuando termina su trabajo (el jugo preparado, o el mensaje de saludo listo para mostrarse en la pantalla).
    """.trimIndent(),
        datoImportante = "La regla de oro de todo buen programador es el principio DRY (Don't Repeat Yourself, o 'No te repitas'). Si ves que estás copiando y pegando el mismo bloque de código en varios lados, es una señal clara de que debes convertirlo en una función.",
        sabiasQue = "Incluso el programa más sencillo del mundo tiene al menos una función. En la mayoría de lenguajes de programación, existe una función principal obligatoria llamada 'main' (principal), que es la que le avisa a la computadora por dónde debe empezar a leer el código.",
        consejo = "Al igual que con las variables, ponle nombres a tus funciones que empiecen con un verbo de acción (ej. calcularTotal(), enviarMensaje() o cambiarModoOscuro()). Así sabrás exactamente qué hace ese botón con solo leerlo.",
        imagen = R.drawable.img_fundamentos_lenguajes,
        pdf = "https://tu-servidor.com/fundamentos_programacion5.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val conceptosProgramacionNivel4 = Curso(
        titulo = "Fundamentos 4: El Mapa de los Lenguajes",
        descripcion = "Descubre qué es un lenguaje de programación, por qué existen tantos y cómo elegir el ideal para tu proyecto.",
        contenido = """
        Ya sabes cómo armar la lógica en tu mente. Pero, ¿cómo se la transmites a la computadora? Ahí es donde entran los Lenguajes de Programación: el puente entre el idioma humano y el mundo digital.
        
        🗣️ 1. ¿Qué es un Lenguaje de Programación?
        
        Una computadora solo entiende electricidad: encendido (1) y apagado (0). Escribir un programa usando solo unos y ceros sería imposible para un humano. 
        
        • Un lenguaje de programación es un conjunto de palabras y reglas que nos permiten escribir instrucciones de una forma que nosotros podamos entender (ej. 'if', 'print', 'val').
        
        • El Traductor (Compilador): Cuando terminas de escribir, un programa interno traduce tu código amigable al lenguaje nativo de la máquina (unos y ceros) para que el procesador lo ejecute.
        
        🏗️ 2. Tipos de Lenguajes según su Uso
        
        No existe un lenguaje "mejor" que otro; cada uno fue creado para resolver un problema diferente, igual que las herramientas en una caja de construcción:
        
        • Para Aplicaciones Móviles: Kotlin es el rey absoluto para crear apps de Android, mientras que Swift se usa para el sistema iOS de Apple.
        
        • Para Páginas Web: JavaScript es el motor que le da vida, movimiento e interactividad a los sitios web en internet.
        
        • Para Inteligencia Artificial y Datos: Python es el más popular debido a su sintaxis limpia, lo que facilita procesar miles de datos o entrenar robots.
        
        • Para Videojuegos: Lenguajes como C++ o C# (C-Sharp) ofrecen la máxima velocidad y potencia visual que los juegos modernos necesitan.
    """.trimIndent(),
        datoImportante = "A pesar de que cambian las palabras clave entre un lenguaje y otro, la lógica detrás de todos es exactamente la misma: todos usan variables, condiciones y bucles.",
        sabiasQue = "Existen más de 700 lenguajes de programación en todo el mundo. Sin embargo, en el día a día del mercado profesional, la gran mayoría de la tecnología se mueve usando únicamente los 10 o 15 lenguajes más populares.",
        consejo = "No intentes aprender tres lenguajes al mismo tiempo cuando estás empezando. Domina bien la lógica en uno solo (como Kotlin para tus apps) y verás que saltar a cualquier otro en el futuro te tomará un par de días.",
        imagen = R.drawable.img_fundamentos_lenguajes,
        pdf = "https://tu-servidor.com/fundamentos_programacion4.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val conceptosProgramacionNivel3 = Curso(
        titulo = "Fundamentos 3: Decisiones y Bucles en el Código",
        descripcion = "Aprende cómo hacer que tu programa tome decisiones de forma automática y descubra el poder de automatizar tareas repetitivas.",
        contenido = """
        En los niveles anteriores aprendiste a dar órdenes en orden y a guardar datos en variables. Ahora verás cómo darle 'vida' e inteligencia a tu código para que reaccione de forma diferente según la situación.
        
        🛣️ 1. Estructuras de Decisión (Condicionales)
        
        Las condicionales permiten que el programa tome un camino u otro dependiendo de si una condición es verdadera o falsa. En el mundo de la programación, esto se conoce universalmente como la estructura SI / SINO (If / Else).
        
        • Imagina este escenario: Estás creando una pantalla de inicio de sesión. El código evalúa lo siguiente:
        
          SI (la contraseña escrita es igual a la guardada) -> Permite el ingreso a la app.
          SINO -> Muestra un mensaje rojo que dice 'Contraseña incorrecta'.
          
        • Gracias a esto, tu aplicación puede adaptarse a lo que haga el usuario en tiempo real.
        
        🔄 2. Estructuras de Repetición (Bucles o Ciclos)
        
        ¿Qué pasa si tienes una lista de 500 alumnos y necesitas enviarles un correo a cada uno? Escribir la misma instrucción 500 veces sería una locura. Para eso existen los Bucles (como el ciclo 'Para' o 'Mientras').
        
        • Un bucle le ordena a la computadora que repita un bloque de código de forma automática tantas veces como sea necesario.
        
        • Funciona bajo una regla simple: 'Mientras esta condición sea verdadera, sigue repitiendo la tarea'. En el momento en que la condición se vuelve falsa, el bucle se detiene (ej. Mientras queden alumnos en la lista -> Envía el correo. Cuando la lista llegue a 0, el bucle se apaga solo).
    """.trimIndent(),
        datoImportante = "Un error muy común al empezar es crear un 'Bucle Infinito'. Esto ocurre cuando olvidas programar la condición de salida, haciendo que la computadora repita la instrucción para siempre hasta que el celular o el programa se cuelguen por falta de memoria.",
        sabiasQue = "Los videojuegos utilizan un bucle gigante llamado 'Game Loop' (Bucle de Juego). Este ciclo se repite unas 60 veces por segundo para redibujar los gráficos en la pantalla y revisar constantemente si presionaste algún botón del control.",
        consejo = "Cuando diseñes una condición compleja con muchas opciones, intenta usar diagramas de flujo sencillos en papel. Dibujar las flechas de los caminos (Sí / No) te aclarará la mente antes de pasarlo a líneas de código.",
        imagen = R.drawable.img_fundamentos_variables,
        pdf = "https://tu-servidor.com/fundamentos_programacion3.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val conceptosProgramacionNivel2 = Curso(
        titulo = "Fundamentos 2: El Cerebro del Código (Variables)",
        descripcion = "Descubre cómo las computadoras guardan información en su memoria y cuáles son los tipos de datos universales en programación.",
        contenido = """
        En el módulo anterior vimos que programar es dar instrucciones. Para que esas instrucciones funcionen, los programas necesitan recordar datos (como tu nombre de usuario, el puntaje de un juego o si tienes el internet activado). 
        
        📦 1. ¿Qué es una Variable?
        
        Imagina la memoria de la computadora o de tu celular como un gran almacén lleno de cajas vacías. Una variable es simplemente una de esas cajas a la que le pones una etiqueta con un nombre para saber qué hay dentro.
        
        • Guardar información: Cuando pones un dato dentro de la caja, se dice que estás 'asignando un valor'.
        
        • Cambiar información: Se llaman 'variables' porque el contenido de la caja puede cambiar a lo largo del juego o de la aplicación (ej. la caja 'monedas' empieza en 0, pero cada vez que ganas un nivel, el número cambia a 10, luego a 20, etc.).
        
        🎨 2. Los 4 Tipos de Datos Universales
        
        Las computadoras necesitan saber qué tipo de objeto vas a meter en la caja para saber cómo tratarlo. En cualquier lenguaje de programación existen estos cuatro tipos básicos:
        
        • Texto (Strings): Cadenas de letras, palabras o frases completas. En el código siempre se escriben entre comillas para que la computadora no las confunda con una orden (ej. "Carlos", "Hola Mundo").
        
        • Números Enteros (Integers): Números completos sin decimales. Se usan para contar cosas exactas (ej. 5 vidas, año 2026, 12 meses).
        
        • Números Decimales (Floats / Doubles): Números que llevan punto decimal. Son los que usas para cálculos más precisos (ej. una calificación de 4.5, el precio de un artículo de ${'$'}19.99).
        
        • Valores Lógicos (Booleans): Es un tipo de dato especial que solo tiene dos respuestas posibles: Verdadero (True) o Falso (False). Es el interruptor del código (ej. ¿la sesión está iniciada? = True; ¿el juego terminó? = False).
    """.trimIndent(),
        datoImportante = "Las computadoras no pueden sumar un Texto con un Número. Si intentas sumar \"5\" (como texto) + 5 (como número), el programa arrojará un error. Por eso es vital definir bien el tipo de dato.",
        sabiasQue = "El tipo de dato lógico 'Boolean' se llama así en honor a George Boole, un matemático que inventó un sistema de álgebra basado únicamente en el 1 y el 0 (Verdadero y Falso), el cual es la base de toda la tecnología actual.",
        consejo = "Ponle siempre nombres claros y descriptivos a tus variables. Es mucho mejor nombrar a tu caja como 'edadUsuario' que llamarla simplemente 'x'. Tu código será más fácil de leer y entender.",
        imagen = R.drawable.img_fundamentos_variables,
        pdf = "https://tu-servidor.com/fundamentos_programacion2.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val conceptosProgramacionBasica = Curso(
        titulo = "Fundamentos: Aprende a Pensar como Programador",
        descripcion = "Descubre qué es la programación, qué es un algoritmo y cómo usar la lógica para resolver cualquier problema técnico.",
        contenido = """
        Programar no es escribir códigos extraños en una pantalla negra; programar es el arte de resolver problemas. Consiste en darle una serie de instrucciones ordenadas a una computadora para que realice una tarea específica.
        
        🧠 1. ¿Qué es un Algoritmo?
        
        Un algoritmo es simplemente una receta de cocina, pero para computadoras. Es una lista de pasos ordenados, lógicos y finitos (que tienen un fin) para lograr un objetivo.
       
        • Las computadoras son extremadamente rápidas, pero también son muy tontas: no asumen nada. Si no les dices cada paso con precisión, no sabrán qué hacer.
        
        • Ejemplo cotidiano: El algoritmo para cepillarse los dientes requiere pasos exactos: 1. Tomar el cepillo, 2. Ponerle pasta dental, 3. Llevarlo a la boca, 4. Cepillar, 5. Enjuagar. Si alteras el orden (como enjuagar antes de cepillar), el resultado falla. Lo mismo pasa en el código.
        
        💻 2. Los 3 Pilares de la Lógica de Programación
        Casi cualquier programa del mundo, desde WhatsApp hasta un videojuego en 3D, se construye combinando estas tres estructuras básicas:
        
        • Secuencias: Es el orden en el que se ejecutan las órdenes, siempre de arriba hacia abajo, una línea después de la otra.
        
        • Condicionales (Decisiones): Permiten al programa tomar caminos diferentes según la situación. Es el famoso "Si pasa esto, haz aquello; si no, haz esto otro" (ej. SI el usuario pone la contraseña correcta, entra a la app; SI NO, muestra un mensaje de error).
        
        • Bucles o Ciclos (Repeticiones): Evitan que tengas que escribir el mismo código mil veces. Le dicen a la computadora que repita una instrucción de forma automática mientras se cumpla una condición (ej. enviar una notificación a todos los alumnos de la lista, uno por uno, hasta llegar al final).
    """.trimIndent(),
        datoImportante = "El lenguaje de programación es solo una herramienta. El 80% del trabajo de un programador consiste en pensar, diseñar el algoritmo y resolver el problema en papel; el otro 20% es traducirlo a código.",
        sabiasQue = "La primera programadora de la historia fue una mujer llamada Ada Lovelace en el año 1843. Ella escribió el primer algoritmo pensado para ser procesado por una máquina, mucho antes de que existieran las computadoras electrónicas.",
        consejo = "Antes de abrir Android Studio a escribir código de golpe, escribe los pasos de lo que quieres lograr en un cuaderno usando tus propias palabras (a esto se le llama Pseudocódigo). Te ahorrará horas de frustración.",
        imagen = R.drawable.img_programacion,
        pdf = "https://tu-servidor.com/fundamentos_programacion.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val seguridadInternetNivel3 = Curso(
        titulo = "Seguridad: Privacidad e Identidad Digital",
        descripcion = "Aprende a controlar qué información ven los demás sobre ti en internet y cómo proteger tu privacidad en redes sociales.",
        contenido = """
        Cada vez que navegas, das un 'Me gusta' o subes una foto, dejas un rastro en internet. En este último nivel aprenderás a cuidar tu privacidad para que seas tú quien decida qué se sabe de ti en el mundo digital.
        
        👣 1. Tu Huella Digital (Lo que internet sabe de ti)
        
        La huella digital es el registro o historial de todo lo que haces en la web. 
        
        • Las páginas que visitas y las aplicaciones usan pequeños archivos llamados 'Cookies' para recordar tus gustos y mostrarte publicidad personalizada.
        
        • Truco sencillo: Borrar el historial de tu navegador y las cookies de vez en cuando (o usar el 'Modo Incógnito') ayuda a que las empresas no rastreen cada uno de tus pasos en la red.
        
        📸 2. El riesgo de compartir demasiado (Oversharing)
        
        Compartir fotos o estados en redes sociales es divertido, pero subir demasiada información en tiempo real puede ser peligroso.
        
        • Evita publicar fotos donde se vea la dirección de tu casa, el uniforme de tu lugar de estudio o trabajo, o fotos de pasaportes y documentos de identidad. Un desconocido podría usar esos datos para suplantar tu identidad o planear estafas.
        
        ⚙️ 3. Ponle candado a tus Redes Sociales
        
        No dejes tus cuentas abiertas para todo el mundo. Dedica cinco minutos a revisar los ajustes de privacidad de tus aplicaciones:
        
        • Cuentas Privadas: Configura tus redes (como Instagram o Facebook) para que solo tus amigos reales puedan ver tus publicaciones.
        
        • Permisos de Apps: Revisa qué aplicaciones tienen acceso a tu ubicación, a tu cámara o a tus contactos desde los ajustes de tu celular y desactiva las que no sean necesarias.
    """.trimIndent(),
        datoImportante = "Una vez que subes una foto o un video a internet, pierdes casi por completo el control sobre ese archivo, ya que cualquiera podría descargarlo, tomarle captura de pantalla o compartirlo sin tu permiso.",
        sabiasQue = "El 'Modo Incógnito' de los navegadores evita que se guarde el historial y las contraseñas en tu propio dispositivo, pero no te hace invisible en internet; las páginas que visitas y tu proveedor de internet siguen sabiendo que entraste.",
        consejo = "Haz un experimento sencillo: escribe tu nombre completo entre comillas en el buscador de Google. Así podrás ver qué información tuya es pública en internet y decidir si hay algo que deberías borrar o proteger mejor.",
        imagen = R.drawable.img_privacidad,
        pdf = "https://tu-servidor.com/seguridad_nivel3.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val seguridadInternetNivel2 = Curso(
        titulo = "Seguridad: Dispositivos y Redes Seguras",
        descripcion = "Aprende a proteger tu celular y computadora de virus, navegar seguro en redes Wi-Fi públicas y cuidar tus archivos.",
        contenido = """
        Ahora que sabes proteger tus cuentas, el siguiente paso es cuidar los dispositivos que usas a diario (tu celular y tu computadora) para evitar que caigan en manos equivocadas o pierdas tu información.
        
        📱 1. El peligro de las redes Wi-Fi públicas
        
        El Wi-Fi gratis de los centros comerciales, parques o cafeterías es muy útil, pero es poco seguro. 
        
        • Al ser una red abierta, cualquier persona conectada a ella con malas intenciones podría "escuchar" o interceptar lo que haces en internet.
        
        • Truco de oro: Evita entrar a la aplicación de tu banco o iniciar sesión en cuentas importantes cuando estés conectado a un Wi-Fi público. Si es urgente, usa mejor tus datos móviles.
        
        👾 2. ¿Cómo entran los virus (Malware) a tu equipo?
        
        Los virus no entran solos; casi siempre los dejamos pasar sin darnos cuenta mediante dos trampas comunes:
        
        • Descargas piratas: Bajar películas, música o programas gratis desde páginas sospechosas suele incluir virus ocultos que dañan tu equipo o espían tus teclas.
        
        • Anuncios falsos: Ventanas flotantes que dicen "¡Tu celular está infectado, da clic aquí para limpiar!". Es mentira; al dar clic, ahí es cuando realmente descargas el virus.
        
        💾 3. Tu salvavidas: Las Copias de Seguridad (Backup)
        
        Los accidentes pasan (un robo, un daño o un virus que bloquea tu equipo). La única forma de no perder tus fotos, documentos o proyectos es teniendo un respaldo.
        
        • Acostúmbrate a guardar tus archivos importantes en la nube (como Google Drive o OneDrive) o en una memoria externa una vez al mes.
    """.trimIndent(),
        datoImportante = "Mantener actualizados tu sistema operativo (Android, Windows, etc.) y tus aplicaciones es la mejor defensa, ya que las actualizaciones cierran agujeros de seguridad que los virus usan para entrar.",
        sabiasQue = "Una VPN (Red Privada Virtual) es una herramienta que funciona como un túnel secreto y blindado para tu internet; si la activas, puedes usar redes Wi-Fi públicas de forma 100% segura.",
        consejo = "Descarga tus aplicaciones únicamente desde las tiendas oficiales como Google Play Store o App Store. Estas plataformas revisan cada app detalladamente antes de dejarla salir al público para asegurar que esté limpia.",
        imagen = R.drawable.img_seguridad,
        pdf = "https://tu-servidor.com/seguridad_nivel2.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val seguridadInternetBasico = Curso(
        titulo = "Seguridad en Internet: Guía Básica",
        descripcion = "Aprende trucos sencillos para proteger tus cuentas, reconocer trampas en la web y navegar sin riesgos.",
        contenido = """
        Internet es una herramienta increíble, pero al igual que al salir a la calle, debemos conocer ciertas normas básicas para proteger nuestra información personal y evitar caer en engaños.
        
        🛡️ 1. El Candado de Seguridad (HTTPS)
        
        Cuando entres a una página web, lo primero que debes mirar es la barra de direcciones (donde escribes el nombre del sitio).
        
        • Busca el icono de un candado cerrado y que la dirección empiece con 'https://' (la 'S' significa Seguro). 
        
        • Esto garantiza que los datos que escribas ahí (como contraseñas o nombres) viajan encriptados, es decir, como un mensaje secreto que nadie más puede leer en el camino.
        
        🎣 2. Cuidado con el 'Phishing' (La trampa del correo)
        
        Es el engaño más común en internet. Ocurre cuando te llega un mensaje de texto, un correo o un chat de WhatsApp que se hace pasar por tu banco, una red social o una empresa conocida.
        
        • Te dirán que hay un 'problema con tu cuenta' o que 'ganaste un premio' y te darán un enlace para que inicies sesión. ¡No lo abras! Al escribir tus datos ahí, se los estás regalando a un desconocido.
        
        🔑 3. Tres Reglas de Oro para tus Contraseñas
       
        Para que tus cuentas sean como una caja fuerte, sigue estos consejos rápidos:
       
        • No uses datos obvios: Evita tu fecha de nacimiento, tu nombre o secuencias como '123456'.
       
        • Mezcla ingredientes: Combina letras mayúsculas, minúsculas, números y un símbolo (como #, $ o @).
        
        • No repitas: Si usan la misma contraseña para todo, y alguien la descubre, tendrá acceso a toda tu vida digital.
    """.trimIndent(),
        datoImportante = "Ningún banco, entidad pública o plataforma seria te pedirá jamás tus contraseñas o datos de tarjetas de crédito por medio de un correo electrónico o un mensaje de texto.",
        sabiasQue = "El término 'Phishing' viene de la palabra en inglés 'fishing' (pescar), porque los atacantes lanzan un anzuelo en internet a miles de personas esperando a ver quién muerde el anzuelo.",
        consejo = "Activa siempre la 'Verificación en dos pasos' en tu WhatsApp y redes sociales. Esto hace que, además de tu contraseña, el sistema te pida un código a tu celular, haciendo casi imposible que te roben la cuenta.",
        imagen = R.drawable.img_seguridad,
        pdf = "https://tu-servidor.com/seguridad_basica.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val microsoftWord2 = Curso(
        titulo = "Word: Estructura e Informes Profesionales",
        descripcion = "Aprende a organizar documentos extensos, crear índices automáticos y usar herramientas avanzadas de diseño.",
        contenido = """
        Cuando trabajas con documentos de muchas páginas (como proyectos o informes), hacer todo a mano quita tiempo y desordena el diseño.
        
        🎨 1. El secreto de los Estilos de Texto
        
        Los 'Estilos' (Título 1, Título 2, Subtítulo) no sirven solo para cambiar el color de la letra. Son etiquetas que le avisan a Word cómo está organizado tu documento. 
        • Al usar los estilos correctamente, puedes cambiar el diseño de todos tus títulos al mismo tiempo con un solo clic, manteniendo la armonía visual.
        
        🗂️ 2. Herramientas Esenciales para Documentos Largos
        
        Para evitar que tu información se mueva de puesto o se desconfigure, estas funciones te salvarán el día:
        
        • Salto de Página: Olvídate de presionar la tecla 'Enter' muchas veces para pasar a la siguiente hoja. El salto de página corta el texto limpiamente y asegura que tus títulos siempre empiecen al inicio de una hoja nueva.
        
        • Tabla de Contenido (Índice Automático): Si usaste los Estilos de Texto, Word puede leerlos y armar el índice de tu documento en dos segundos. Si agregas más páginas, solo das clic en 'Actualizar tabla' y los números se corrigen solos.
        
        • Encabezados y Pies de Página Inteligentes: Te permiten colocar el título de tu proyecto o el número de página en la parte superior o inferior de todo el documento de forma automática.
        
        • Formato de Párrafo Avanzado: Controla el interlineado (el espacio entre renglones) y la sangría para cumplir con normas de presentación formales.
    """.trimIndent(),
        datoImportante = "Hacer un índice a mano usando puntos virtuales es el error más común. Las Tablas de Contenido automáticas te ahorran horas de trabajo y evitan descuadres.",
        sabiasQue = "Word permite configurar 'Secciones' diferentes en un mismo archivo. Gracias a esto, puedes hacer que las primeras páginas (como la portada o introducción) no lleven número, y que la numeración empiece exactamente desde el capítulo uno.",
        consejo = "Usa siempre el atajo 'Control + Enter' cuando quieras pasar a una nueva página en blanco. Es la forma rápida de crear un Salto de Página limpio sin desordenar el texto anterior.",
        imagen = R.drawable.img_word,
        pdf = "https://tu-servidor.com/word_nivel2.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val microsoftExcel3 = Curso(
        titulo = "Trucos con Fórmulas y Limpieza de Datos",
        descripcion = "Aprende a unir textos, ocultar errores visuales y limpiar tus listas de datos de forma rápida y sencilla.",
        contenido = """
        
        🧩 1. Conectar Fórmulas (Anidación)
        Anidar es simplemente meter una fórmula dentro de otra para que trabajen en equipo. 
        • Piensa en esto: Puedes programar a Excel para que primero BUSQUE un dato, y SI no lo encuentra, en vez de romperse, muestre un mensaje amigable como 'No existe'.
        
        🧹 2. Tres Fórmulas Sencillas que te salvarán el Día cuando los datos vienen desordenados, estas funciones limpian tu hoja de cálculo en segundos:
        
        • =SI.ERROR(fórmula; "texto"): Sirve para ocultar esos códigos feos de error que salen a veces (como #N/D o #¡DIV/0!). En su lugar, puedes hacer que la celda quede en blanco o muestre un texto limpio como "Dato no encontrado".
        
        • =CONCAT(texto1; texto2): Une la información de varias celdas en una sola. Es perfecta si tienes la columna 'Nombre' y la columna 'Apellido' por separado y quieres juntarlas en una sola celda.
        
        • =UNICOS(rango): Es una fórmula mágica. Revisa una lista o columna entera y te extrae automáticamente una copia limpia, eliminando al instante todos los nombres o números que estén repetidos.
    """.trimIndent(),
        datoImportante = "La fórmula SI.ERROR es como un escudo protector: no evita que ocurra un error, pero hace que tu pantalla se siga viendo limpia y ordenada.",
        sabiasQue = "Antes, para unir textos tenías que escribir la palabra completa CONCATENAR, pero ahora Excel lo resumió a solo CONCAT para que sea más rápido de usar.",
        consejo = "Usa la fórmula UNICOS en una columna nueva para saber exactamente cuántos elementos reales y sin repetir tienes en tus listas de tareas o registros.",
        imagen = R.drawable.img_excel,
        pdf = "https://tu-servidor.com/excel_nivel3_facil.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val microsoftExcel2 = Curso(
        titulo = "Fórmulas en Excel: Estructura y Uso",
        descripcion = "Aprende la lógica detrás de cualquier fórmula en Excel y descubre cuáles son las funciones esenciales que debes dominar.",
        contenido = """
        Para dominar Excel, el secreto no es memorizar cientos de comandos, sino entender la estructura de sus fórmulas y saber qué función aplicar según el problema.
        
        🧠 1. La Estructura de una Fórmula
        
        Toda fórmula o función en Excel sigue estrictamente un orden lógico (Sintaxis). Si olvidas un elemento, el sistema arrojará un error:
        
        • El signo igual (=): Es obligatorio. Le avisa a Excel que vas a realizar un cálculo y no a escribir texto común.
        
        • Nombre de la función: Es la palabra clave que define la acción (ej. SUMA, SI, BUSCARV). Siempre va en mayúsculas.
        
        • Paréntesis de apertura ( ): Todo lo que la función necesita para calcular debe ir encerrado entre paréntesis.
        
        • Argumentos: Son los datos, celdas o rangos que la función va a procesar, separados por puntos y comas (;) o comas (,) según la configuración de tu sistema.
        
        • Paréntesis de cierre ): Cierra la instrucción.
        
        📊 2. Las Fórmulas Más Importantes (¿Para qué sirven?)
        Para resolver tareas administrativas, auditorías de datos o análisis lógicos, estas son las funciones que debes conocer:
        
        • =SUMA(rango): Suma de forma masiva todos los valores numéricos de las celdas seleccionadas.
       
        • =CONTAR(rango): Cuenta cuántas celdas del rango seleccionado contienen números (ignora el texto).
       
        • =CONTAR.SI(rango; criterio): Cuenta celdas solo si cumplen una condición específica (ej. contar cuántas veces se repite un número de reclamo o un código).
       
        • =SI(condición; valor_si_verdadero; valor_si_falso): Es una función lógica. Evalúa una situación y toma una decisión automática (ej. si el saldo es menor a 0, muestra 'Alerta', si no, muestra 'Ok').
        
        • =BUSCARV(valor_buscado; matriz; indicador_columnas; ordenado): Busca un dato en una tabla extensa y te trae la información relacionada de otra columna (ej. buscas la cédula de alguien y te devuelve su nombre automáticamente).
       
        • =ESPACIOS(texto): Elimina todos los espacios adicionales ocultos que a veces se copian mal al importar datos, dejando solo los espacios simples entre palabras.
    """.trimIndent(),
        datoImportante = "El signo igual (=) es el interruptor que activa el motor de cálculo de Excel. Sin él, la plataforma interpretará tu fórmula como un texto común y corriente.",
        sabiasQue = "Si anidas la función SI dentro de otra función SI, puedes crear árboles de decisión complejos para automatizar aprobaciones, estados de cuentas o categorías de datos.",
        consejo = "Para evitar escribir las fórmulas desde cero, puedes usar el atajo 'Shift + F3' dentro de una celda para abrir el Asistente de Funciones de Excel, el cual te guiará paso a paso con los argumentos.",
        imagen = R.drawable.img_excel,
        pdf = "https://tu-servidor.com/excel_formulas.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val microsoftExcel = Curso(
        titulo = "Microsoft Excel",
        descripcion = "Aprende a organizar datos, realizar cálculos y analizar información de manera eficiente utilizando hojas de cálculo.",
        contenido = """
        Microsoft Excel es la hoja de cálculo más utilizada en el mundo y forma parte de la suite de Microsoft Office. Su función principal es organizar datos en filas y columnas para realizar cálculos matemáticos, estadísticas y análisis lógicos.
        
        Entre las herramientas más importantes de Excel encontramos:
        
        • Crear y organizar hojas de cálculo.
        • Introducir y editar datos numéricos y de texto.
        • Utilizar fórmulas básicas (Suma, Promedio, Contar).
        • Aplicar funciones lógicas y de búsqueda (SI, BUSCARV).
        • Diseñar gráficos estadísticos (Barras, Líneas, Pasteles).
        • Formatear celdas con colores, bordes y estilos de moneda.
        • Utilizar el Formato Condicional para resaltar datos clave.
        • Filtrar y ordenar grandes listas de información.
        • Crear Tablas Dinámicas para resumir grandes volúmenes de datos.
        • Proteger hojas y libros con contraseñas.
        
        Excel permite guardar y exportar el trabajo en diferentes formatos, siendo los más comunes:
        
        • XLSX (Formato nativo de Excel)
        • CSV (Valores separados por comas, ideal para bases de datos)
        
        Dominar Excel es una de las habilidades más valoradas y solicitadas por las empresas a nivel global, ya que facilita la toma de decisiones, la administración de finanzas y el control de inventarios.
    """.trimIndent(),
        datoImportante = "Microsoft Excel es la herramienta estándar en el mundo empresarial para la gestión de datos, auditorías y contabilidad.",
        sabiasQue = "La primera versión de Excel fue lanzada originalmente para equipos Apple Macintosh en 1985, antes de llegar al sistema operativo Windows en 1987.",
        consejo = "Usa siempre las referencias absolutas (anteponiendo el signo de dinero '$' como en ${'$'}A${'$'}1) cuando fijes celdas dentro de tus fórmulas para evitar errores al arrastrarlas.",
        imagen = R.drawable.img_excel,
        pdf = "https://tu-servidor.com/excel.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val microsoftWord = Curso(

        titulo = "Microsoft Word",

        descripcion = "Aprende a crear, editar y dar formato a documentos profesionales utilizando Microsoft Word.",

        contenido = """
Microsoft Word es uno de los procesadores de texto más utilizados en el mundo y forma parte de la suite Microsoft Office.

Su principal función es permitir la creación de documentos como cartas, informes, trabajos académicos, currículums y manuales.

Entre las herramientas más importantes de Word encontramos:

• Crear documentos nuevos.

• Editar texto.

• Cambiar el tipo y tamaño de fuente.

• Aplicar negrita, cursiva y subrayado.

• Insertar imágenes.

• Crear tablas.

• Agregar encabezados y pies de página.

• Numerar páginas.

• Insertar formas y gráficos.

• Revisar ortografía y gramática.

Word también permite guardar documentos en diferentes formatos, siendo los más utilizados:

• DOCX

• PDF

Aprender Word facilita la elaboración de documentos profesionales tanto en el ámbito académico como laboral.

Es una de las herramientas más solicitadas por las empresas y una habilidad básica para cualquier estudiante o profesional.

    """.trimIndent(),

        datoImportante =
            "Microsoft Word es utilizado por millones de personas en todo el mundo para crear documentos profesionales.",

        sabiasQue =
            "La primera versión de Microsoft Word fue lanzada en 1983 y desde entonces se ha convertido en el procesador de texto más popular.",

        consejo =
            "Utiliza siempre los estilos y títulos de Word para organizar tus documentos. Esto facilita la creación automática de tablas de contenido.",

        imagen = R.drawable.img_word,

        pdf = "https://tu-servidor.com/word.pdf",

        video = "https://youtu.be/XXXXXXXX"

    )

    val fundamentosRedes = Curso(

        titulo = "Fundamentos de Redes",

        descripcion = "Aprende cómo se comunican los dispositivos entre sí, conoce los componentes de una red y comprende cómo funciona Internet.",

        contenido = """

Las redes informáticas permiten que dos o más dispositivos puedan comunicarse entre sí para compartir información, archivos, impresoras, servicios y acceso a Internet.

Actualmente prácticamente todo lo que hacemos depende de una red: enviar un mensaje por WhatsApp, navegar por Internet, ver un video en YouTube o realizar una videollamada.

Una red está formada por diferentes dispositivos que trabajan de manera conjunta para transportar información de un punto a otro.

Entre los principales componentes de una red encontramos:

• Computadores y dispositivos móviles
Son los equipos que envían y reciben la información.

• Router
Es el dispositivo encargado de conectar una red local con Internet. Además, dirige los paquetes de datos hacia su destino correcto.

• Switch
Permite conectar varios dispositivos dentro de una misma red local y facilita la comunicación entre ellos.

• Cableado de red
Transporta físicamente la información mediante cables Ethernet o fibra óptica.

• Redes inalámbricas (Wi-Fi)
Permiten conectar dispositivos sin necesidad de utilizar cables mediante ondas de radio.

------------------------------------------------------------

¿Cómo viaja la información?

Cuando una persona envía un mensaje desde su teléfono móvil, ese mensaje no viaja completo como un único archivo.

La información se divide en pequeños fragmentos llamados paquetes de datos.

Cada paquete contiene información sobre:

• Dirección del origen.
• Dirección del destino.
• Contenido.
• Orden en el que debe reconstruirse.

Estos paquetes recorren diferentes dispositivos de red hasta llegar a su destino, donde vuelven a ensamblarse para formar el mensaje original.

------------------------------------------------------------

¿Qué es una dirección IP?

Cada dispositivo conectado a una red posee una dirección IP, la cual funciona como si fuera la dirección de una casa.

Gracias a ella los routers saben exactamente dónde entregar la información.

Existen dos versiones principales:

• IPv4
Ejemplo:
192.168.1.25

• IPv6
Ejemplo:
2001:0db8:85a3::8a2e:0370:7334

------------------------------------------------------------

Tipos de redes

Dependiendo del tamaño y alcance existen diferentes tipos de redes.

• LAN (Local Area Network)
Conecta dispositivos dentro de una casa, oficina o colegio.

• MAN (Metropolitan Area Network)
Interconecta varias redes dentro de una ciudad.

• WAN (Wide Area Network)
Conecta redes ubicadas en diferentes ciudades o países.

Internet es el ejemplo más grande de una red WAN.

------------------------------------------------------------

Protocolos de comunicación

Para que todos los dispositivos puedan entenderse utilizan protocolos de comunicación.

Un protocolo es un conjunto de reglas que define cómo debe enviarse y recibirse la información.

Algunos de los más utilizados son:

• TCP
Garantiza que la información llegue completa y en el orden correcto.

• IP
Permite identificar cada dispositivo y dirigir los paquetes de datos.

• HTTP y HTTPS
Permiten acceder a páginas web.

• FTP
Se utiliza para transferir archivos entre computadores.

------------------------------------------------------------

¿Por qué son importantes las redes?

Las redes permiten compartir recursos, facilitar el trabajo colaborativo y acceder a información desde cualquier lugar del mundo.

Gracias a ellas es posible utilizar servicios como el correo electrónico, videoconferencias, plataformas educativas, almacenamiento en la nube y aplicaciones empresariales.

Actualmente prácticamente todas las empresas dependen de una infraestructura de red para realizar sus actividades diarias, por lo que conocer sus fundamentos es una habilidad muy valiosa para cualquier profesional del área de tecnología.

    """.trimIndent(),

        datoImportante =
            "Internet es la red de computadoras más grande del mundo y conecta miles de millones de dispositivos las 24 horas del día.",

        sabiasQue =
            "El primer mensaje enviado por ARPANET, la red precursora de Internet, fue la palabra 'LOGIN'. Sin embargo, el sistema falló después de enviar solamente las dos primeras letras: 'LO'.",

        consejo =
            "Antes de intentar solucionar un problema de conexión, verifica siempre los aspectos más básicos: alimentación eléctrica, cables correctamente conectados y estado del router. Muchas fallas de red tienen soluciones muy simples.",

        imagen = R.drawable.img_redes,

        pdf = "https://tuservidor.com/redes.pdf",

        video = "https://youtu.be/QKfk7YFILws"

    )

    val introduccionLinux = Curso(

        titulo = "Introducción a Linux",

        descripcion = "Conoce qué es Linux, sus principales características y por qué es uno de los sistemas operativos más utilizados en el mundo.",

        contenido = """
Linux es un sistema operativo de código abierto basado en Unix. Es utilizado en computadores personales, servidores, dispositivos móviles y equipos embebidos.

Entre sus principales características se encuentran:

• Es gratuito y de código abierto.

• Es altamente seguro y estable.

• Permite la personalización del sistema.

• Es utilizado por empresas como Google, Amazon, Meta y la mayoría de servidores de Internet.

Existen muchas distribuciones de Linux, como Ubuntu, Debian, Fedora y Kali Linux, cada una diseñada para diferentes necesidades.

Linux es ampliamente utilizado por administradores de sistemas, desarrolladores de software y especialistas en ciberseguridad debido a su rendimiento y flexibilidad.

Aprender Linux proporciona una base sólida para comprender el funcionamiento interno de los sistemas operativos y administrar servidores de manera profesional.

A diferencia de Windows (donde tienes el disco C: o D:), en Linux todo nace de un único punto de origen llamado Raíz, representado por una barra diagonal: /.

Imagina la raíz como el tronco de un árbol. De ahí nacen las ramas principales:

/home: La carpeta donde viven los archivos personales de los usuarios (tus documentos, descargas, etc.).

/etc: El centro de control donde se guardan las configuraciones del sistema.

/bin y /sbin: Las carpetas que contienen los comandos y programas esenciales del sistema (como los que usaremos a continuación).

1. Los Primeros 4 Comandos Esenciales

- pwd: te dice exactamente en qué parte del mapa (carpeta) estás parado en este momento para que no te pierdas.

- ls:  Muestra todo lo que hay dentro de la carpeta en la que estás (archivos y otras carpetas), Si usas ls -l, te muestra la lista detallada (con tamaños y permisos). Si usas ls -a, te muestra los archivos ocultos.

- cd: Es el equivalente a hacer doble clic sobre una carpeta para entrar en ella, o para salir.

- mkdir: Crea una carpeta nueva y vacía en donde estés ubicado.
    """.trimIndent(),

        datoImportante =
            "Más del 90% de los servidores que existen en Internet utilizan alguna distribución de Linux.",

        sabiasQue =
            "Android está basado en el kernel de Linux, por lo que millones de dispositivos móviles utilizan esta tecnología diariamente.",

        consejo =
            "Comienza practicando con una máquina virtual antes de instalar Linux en tu computador principal.",

        imagen = R.drawable.img_linux,

        pdf = "https://tuservidor.com/linux.pdf",

        video = "https://youtu.be/ROjZy1WbCIA"

    )

    val introduccionLinuxNivel2 = Curso(
        titulo = "Linux 2: Manejo de Archivos y Permisos",
        descripcion = "Aprende a crear, copiar, mover y borrar archivos desde la terminal, y descubre cómo funciona la seguridad con los permisos.",
        contenido = """
        Ahora que sabes cómo moverte entre carpetas con los comandos básicos, el siguiente paso es aprender a manipular tus archivos y entender quién tiene permiso de verlos o modificarlos.
        
        📂 1. Cuatro Comandos para Controlar tus Archivos
        En la terminal no usamos el mouse para arrastrar archivos, usamos palabras clave muy cortas:
        
        • touch [nombre_archivo]: Crea un archivo de texto completamente vacío en segundos (ej. touch notas.txt).
        
        • cp [origen] [destino]: Sirve para sacar una copia exacta de un archivo y ponerla en otra carpeta sin borrar el original.
        
        • mv [origen] [destino]: Funciona de dos formas: mueve un archivo de una carpeta a otra (como cortar y pegar), o cambia el nombre de un archivo si lo dejas en el mismo lugar.
        
        • rm [archivo]: Borra un archivo de forma definitiva. ¡Ten cuidado! En la terminal de Linux no existe una papelera de reciclaje; lo que borras, se va para siempre.
        
        🔒 2. El Escudo de Linux: Permisos Básicos
        Linux es súper seguro porque cada archivo tiene un dueño y unas reglas estrictas. Si usas el comando 'ls -l', verás unas letras extrañas al inicio de cada archivo (como rwx):
        
        • r (Read / Lectura): Permite abrir y ver el contenido del archivo.
       
        • w (Write / Escritura): Permite modificar, editar o borrar el archivo.
       
        • x (Execute / Ejecución): Permite arrancar el archivo si es un programa o una aplicación.
        
        Si no tienes activada la letra correcta, Linux bloqueará tu acceso con el mensaje 'Permiso denegado'.
    """.trimIndent(),
        datoImportante = "El comando rm es una de las herramientas más potentes de Linux. Borrar archivos del sistema por error con este comando puede dañar el funcionamiento de la computadora por completo.",
        sabiasQue = "En Linux, las extensiones de archivos (como .txt o .docx) son opcionales y sirven más como ayuda visual para el usuario, ya que el sistema operativo identifica qué tipo de archivo es leyendo su contenido interno, no su nombre.",
        consejo = "Si quieres borrar una carpeta que tiene archivos por dentro, el comando rm común no funcionará. Debes usar 'rm -r' (la 'r' significa recursivo) para decirle a Linux que borre la carpeta con todo lo que tiene adentro.",
        imagen = R.drawable.img_linux,
        pdf = "https://tuservidor.com/linux_nivel2.pdf",
        video = "https://youtu.be/XXXXXXXX"
    )

    val hardwareBasico = Curso(


        titulo = "Hardware Básico",

        descripcion = "Aprende los conceptos fundamentales del hardware y conoce los componentes físicos que conforman un computador.",

        datoImportante = "El hardware corresponde a todas las partes físicas de un computador. Sin hardware, el software no puede ejecutarse.",

        sabiasQue = "¿Sabías que el primer disco duro comercial, creado por IBM en 1956, pesaba más de una tonelada y solo podía almacenar 5 MB de información?",

        consejo = "Antes de manipular componentes internos del computador, desconéctalo de la corriente y descarga la electricidad estática tocando una superficie metálica.",
        contenido = """

El hardware es el conjunto de componentes físicos que conforman un computador. Estos elementos pueden verse y tocarse, y trabajan en conjunto para que el equipo funcione correctamente.

Entre los componentes principales encontramos:

• Procesador (CPU)
Es el cerebro del computador y se encarga de ejecutar todas las instrucciones.

• Memoria RAM
Almacena temporalmente la información mientras los programas están en ejecución.

• Disco Duro o SSD
Guarda permanentemente el sistema operativo, programas y archivos del usuario.

• Tarjeta Madre
Permite la comunicación entre todos los componentes del computador.

• Fuente de Poder
Suministra la energía eléctrica necesaria para el funcionamiento del equipo.

• Dispositivos de Entrada
Permiten ingresar información al computador, como el teclado, el mouse o el escáner.

• Dispositivos de Salida
Permiten visualizar los resultados del procesamiento, como el monitor, los parlantes o la impresora.

Todos estos componentes trabajan de manera conjunta para que el computador pueda ejecutar aplicaciones y responder a las acciones del usuario.

    """.trimIndent(),

        imagen = R.drawable.img_hardware,

        pdf = "https://www.webcolegios.com/file/abc728.pdf",

        video = "https://www.youtube.com/watch?v=O0E_Rv-r1e8",

        materialUrl = "https://es.wikipedia.org/wiki/Hardware",



        )

    val listaRedes = listOf(

        introduccionLinux,
        introduccionLinuxNivel2,
        fundamentosRedes,
        redesInternetNivel2,
    )

    val listaOfimatica = listOf(

        hardwareBasico,
        microsoftWord,
        microsoftWord2,
        microsoftExcel,
        microsoftExcel2,
        microsoftExcel3,

    )

    val listaSeguridad = listOf(

        seguridadInternetBasico,
        seguridadInternetNivel2,
        seguridadInternetNivel3,
        seguridadInternetNivel4,
    )

    val listaProgramacion = listOf(

        conceptosProgramacionBasica,
        conceptosProgramacionNivel2,
        conceptosProgramacionNivel3,
        conceptosProgramacionNivel4,
        conceptosProgramacionNivel5,
        conceptosProgramacionNivel6,
    )

    val todosLosCursos =
        listaOfimatica +
                listaRedes +
                listaSeguridad +
                listaProgramacion
}

