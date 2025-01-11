document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'indexFct.html';
        return;
    }
    cargarPublicaciones(token);
    const socket = conectarWebSocket(token);

    socket.onmessage = (event) => {
        try {
            const mensaje = JSON.parse(event.data);
            switch (mensaje.tipo) {
                case "ACTUALIZACION_PUBLICACIONES":
                    console.log("Actualización recibida: Recargando publicaciones...");
                    cargarPublicaciones(token);
                    break;
                case "NUEVO_COMUNICADO":
                    console.log("Nuevo comunicado:", mensaje.mensaje);
                    cargarPublicaciones(token);
                    break;
                case "NUEVA_PUBLICACION_ARCHIVO":
                    console.log("Nuevo archivo subido:", mensaje.mensaje);
                    cargarPublicaciones(token);
                    break;
                case "NUEVA_IMAGEN":
                console.log("Nueva imagen:", mensaje.mensaje);
                cargarPublicaciones(token); // Actualizar publicaciones
                break;
                default:
                    console.warn("Tipo de mensaje desconocido:", mensaje.tipo);
            }
        } catch (error) {
            console.error("Error al procesar el mensaje del WebSocket:", event.data, error);
        }
    };
});

// Función para conectar al WebSocket
function conectarWebSocket(token) {
    const socket = new WebSocket("ws://localhost:8080/ws/notificaciones");

    socket.onopen = () => {
        console.log("Conexión WebSocket establecida.");
        // Si es necesario, autentica al WebSocket enviando el token
        socket.send(JSON.stringify({ tipo: "AUTENTICACION", token }));
    };

    socket.onerror = (error) => {
        console.error("Error en el WebSocket:", error);
    };

    socket.onclose = () => {
        console.warn("WebSocket cerrado. Reintentando en 5 segundos...");
        setTimeout(() => conectarWebSocket(token), 5000); // Reintentar conexión
    };

    return socket;
}




function cargarPublicaciones(token) {
    fetch("http://localhost:8080/api/publicaciones/grupo", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al obtener las publicaciones");
            }
            return response.json();
        })
        .then(data => {
            if (data.publicaciones && data.publicaciones.length > 0) {
                mostrarPublicaciones(data.publicaciones);
            } else {
                alert("No hay publicaciones disponibles");
            }
        })
        .catch(error => {
         //   console.error("Error al obtener las publicaciones:", error);
          //  alert("Error al obtener las publicaciones.");
        });
}

function mostrarPublicaciones(publicaciones) {
    const muroPublicaciones = document.getElementById("muro-publicaciones");
    muroPublicaciones.innerHTML = ""; // Limpiar cualquier contenido previo

    publicaciones.forEach(publicacion => {
        const publicacionSection = document.createElement("section");
        publicacionSection.classList.add("publicacion");

        // Título de la publicación
        const titulo = document.createElement("h2");
        titulo.textContent = publicacion.titulo || "Sin título";
        publicacionSection.appendChild(titulo);

        // Contenido de la publicación
        const contenido = document.createElement("p");
        contenido.textContent = publicacion.contenido || "Contenido no disponible";
        publicacionSection.appendChild(contenido);

        // Fecha de creación
        const fecha = document.createElement("small");
        fecha.textContent = new Date(publicacion.fechaCreacion).toLocaleString();
        publicacionSection.appendChild(fecha);

        // Autor de la publicación
        const autor = document.createElement("strong");
        autor.textContent = `Autor: ${publicacion.autor || "Autor no disponible"}`;
        publicacionSection.appendChild(autor);

        // Crear elementos según el tipo de contenido
        const tipoContenido = publicacion.tipoContenido;

        // Mostrar imagen si corresponde
        if (tipoContenido === "imagen" && publicacion.imagenUrl) {
            const imagen = document.createElement("img");
            imagen.src = `http://localhost:8080/uploads/${publicacion.imagenUrl}`;
            imagen.alt = "Imagen de la publicación";
            imagen.style.maxWidth = "100%";
            imagen.style.marginTop = "10px";
            publicacionSection.appendChild(imagen);
        }

        // Botón para descargar archivo si corresponde
        if (tipoContenido === "archivo") {
           // console.log("Tipo de contenido:", tipoContenido, "Archivo URL:", publicacion.archivoUrl);
        
            if (publicacion.archivoUrl) {
                const botonDescargar = document.createElement("a");
                botonDescargar.href = `http://localhost:8080/uploads/ficheros/${publicacion.archivoUrl}`;
                botonDescargar.textContent = "Descargar Archivo";
                botonDescargar.target = "_blank";
                botonDescargar.style.display = "block";
                botonDescargar.style.marginTop = "10px";
                publicacionSection.appendChild(botonDescargar);
            } else {
               // console.warn("No se encontró archivoUrl para la publicación:", publicacion.id);
            }
        }
        

        // Mostrar opciones de encuesta si corresponde
// Mostrar opciones de encuesta o resultados si ya votó
if (tipoContenido === "encuesta" && publicacion.opcionesEncuesta) {
    const encuestaForm = document.createElement("div");
    encuestaForm.classList.add("encuesta");

    if (publicacion.yaVotado) {
        // Mostrar la opción votada y los resultados
        const resultado = document.createElement("p");
        resultado.textContent = `Ya has votado por: ${publicacion.opcionSeleccionada.texto}`;
        encuestaForm.appendChild(resultado);

        const resultadosLista = document.createElement("ul");
        publicacion.opcionesEncuesta.forEach(opcion => {
            const resultadoItem = document.createElement("li");
            resultadoItem.textContent = `${opcion.texto}: ${opcion.votos} votos`;
            resultadosLista.appendChild(resultadoItem);
        });

        encuestaForm.appendChild(resultadosLista);
    } else {
        // Mostrar opciones de votación si aún no ha votado
        publicacion.opcionesEncuesta.forEach(opcion => {
            const opcionLabel = document.createElement("label");
            opcionLabel.textContent = `${opcion.texto} (${opcion.votos} votos)`;

            const opcionRadio = document.createElement("input");
            opcionRadio.type = "radio";
            opcionRadio.name = `encuesta-${publicacion.id}`;
            opcionRadio.value = opcion.id;

            opcionLabel.prepend(opcionRadio);
            encuestaForm.appendChild(opcionLabel);
        });
    
    

        const botonVotar = document.createElement("button");
        botonVotar.textContent = "Votar";
        botonVotar.setAttribute("data-encuesta-id", publicacion.id);
        botonVotar.addEventListener("click", function (event) {
            event.preventDefault();
            const opcionSeleccionada = encuestaForm.querySelector(`input[name='encuesta-${publicacion.id}']:checked`);
            if (opcionSeleccionada && opcionSeleccionada.value) {
                votarOpcion(opcionSeleccionada.value, publicacion.id);
            } else {
                alert("Por favor, selecciona una opción.");
            }
        });

        encuestaForm.appendChild(botonVotar);
    }

    publicacionSection.appendChild(encuestaForm);
}

function votarOpcion(opcionId, encuestaId) {
    const token = localStorage.getItem("token");
    fetch("http://localhost:8080/api/encuestas/votar", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ opcionId: parseInt(opcionId), encuestaId: parseInt(encuestaId) })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al registrar el voto.");
        }
        return response.json();
    })
    .then(data => {
        alert(data.message || "Voto registrado exitosamente.");
        cargarPublicaciones(token); // Recargar publicaciones
    })
    .catch(error => {
       // console.error("Error al votar:", error);
        alert("Hubo un problema al registrar tu voto.");
    });
}


        // Mostrar comentarios
        const comentariosSection = document.createElement("div");
        comentariosSection.classList.add("comentarios");

        const tituloComentarios = document.createElement("h3");
        tituloComentarios.textContent = "Comentarios";
        tituloComentarios.style.marginBottom = "10px";
        tituloComentarios.style.color = "#333";
        comentariosSection.appendChild(tituloComentarios);

        if (publicacion.comentarios.length > 0) {
            publicacion.comentarios.forEach(comentario => {
                const comentarioDiv = document.createElement("div");
                comentarioDiv.textContent = `${comentario.autorNombre || "Anónimo"}: ${comentario.contenido || "Sin contenido"}`;
                comentariosSection.appendChild(comentarioDiv);
            });
        } else {
            const sinComentarios = document.createElement("p");
            sinComentarios.textContent = "Sin comentarios";
            comentariosSection.appendChild(sinComentarios);
        }

        // Formulario para añadir un comentario
        const formularioComentario = document.createElement("form");
        const inputComentario = document.createElement("input");
        inputComentario.type = "text";
        inputComentario.placeholder = "Añadir un comentario...";
        const botonComentario = document.createElement("button");
        botonComentario.textContent = "Comentar";

        formularioComentario.appendChild(inputComentario);
        formularioComentario.appendChild(botonComentario);

        formularioComentario.addEventListener("submit", function (event) {
            event.preventDefault();
            if (inputComentario.value.trim()) {
                añadirComentario(publicacion.id, inputComentario.value);
                inputComentario.value = ""; // Limpiar el campo
            } else {
                inputComentario.classList.add("input-error");
                setTimeout(() => inputComentario.classList.remove("input-error"), 2000); // Retroalimentación visual
                alert("El comentario no puede estar vacío.");
            }
        });
        

        comentariosSection.appendChild(formularioComentario);
        publicacionSection.appendChild(comentariosSection);

        // Agregar la publicación al muro
        muroPublicaciones.appendChild(publicacionSection);
    });
}

function votarOpcion(publicacionId, opcionTexto) {
    const token = localStorage.getItem("token");

    fetch(`http://localhost:8080/api/encuestas/votar`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ opcionId: opcionTexto })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al votar la opción.");
            }
            return response.json();
        })
        .then(data => {
            //alert("Voto registrado exitosamente.");
            cargarPublicaciones(token); // Recargar publicaciones para reflejar el cambio
        })
        .catch(error => {
           // console.error("Error al votar:", error);
            alert("Error al votar.");
        });
}

function añadirComentario(publicacionId, contenido) {
    const token = localStorage.getItem("token");

    fetch(`http://localhost:8080/api/comentarios`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ publicacionId, contenido })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al añadir el comentario.");
            }
            return response.json();
        })
        .then(data => {
          //  alert("Comentario añadido exitosamente.");
            cargarPublicaciones(token); // Recargar publicaciones para reflejar el cambio
        })
        .catch(error => {
           // console.error("Error al añadir comentario:", error);
           // alert("Error al añadir el comentario.");
           cargarPublicaciones(token);
        });
}
