document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token"); // Obtener el token desde localStorage

    if (!token) {
        window.location.href = 'indexFct.html';
        return;
    }

    // Conectar al WebSocket para notificaciones de perfil
    const socket = conectarWebSocket(token);

    socket.onmessage = (event) => {
        try {
            const mensaje = JSON.parse(event.data);
            switch (mensaje.tipo) {
                case "ACTUALIZACION_NOMBRE":
                    //console.log("Notificación de nombre:", mensaje.mensaje);
                  //  alert(mensaje.mensaje);
                    break;

                case "ACTUALIZACION_CONTRASENA":
                  //  console.log("Notificación de contraseña:", mensaje.mensaje);
                   // alert(mensaje.mensaje);
                    break;

                default:
                    console.warn("Tipo de mensaje no reconocido:", mensaje.tipo);
            }
        } catch (error) {
            console.error("Error al procesar el mensaje del WebSocket:", event.data, error);
        }
    };

    socket.onerror = (error) => {
        console.error("Error en el WebSocket:", error);
    };

    socket.onclose = () => {
        console.warn("WebSocket cerrado.");
    };
});

// Función para conectar al WebSocket
function conectarWebSocket(token) {
    const socket = new WebSocket("ws://localhost:8080/ws/datos-alumno"); // Ruta del WebSocket para datos de alumno

    socket.onopen = () => {
      //  console.log("WebSocket de datos de alumno conectado.");
    };

    return socket;
}

// Abrir el modal
function abrirModalEditarPerfil() {
    document.getElementById("modalEditarPerfil").style.display = "block";
}

// Cerrar el modal
function cerrarModalEditarPerfil() {
    document.getElementById("modalEditarPerfil").style.display = "none";
}

// Cerrar modal al hacer clic fuera de él
window.addEventListener("click", function (event) {
    const modal = document.getElementById("modalEditarPerfil");
    if (event.target === modal) {
        cerrarModalEditarPerfil();
    }
});

// Validar y enviar el formulario
document.getElementById("formEditarPerfil").addEventListener("submit", function (e) {
    e.preventDefault();

    const nombreCompleto = document.getElementById("nombreCompleto").value.trim();
    const contrasenaActual = document.getElementById("contrasenaActual").value.trim();
    const nuevaContrasena = document.getElementById("nuevaContrasena").value.trim();
    const repetirContrasena = document.getElementById("repetirContrasena").value.trim();

    if (!nombreCompleto && (!contrasenaActual || !nuevaContrasena || !repetirContrasena)) {
        alert("Por favor, realiza al menos un cambio.");
        return;
    }

    if (nuevaContrasena && nuevaContrasena !== repetirContrasena) {
        alert("La nueva contraseña no coincide.");
        return;
    }

    // Crear el objeto de datos dinámicamente
    const datosPerfil = {};

    if (nombreCompleto) {
        datosPerfil.nombreCompleto = nombreCompleto;
    }

    if (contrasenaActual && nuevaContrasena) {
        datosPerfil.contrasenaActual = contrasenaActual;
        datosPerfil.nuevaContrasena = nuevaContrasena;
    }


    // Realizar la solicitud al servidor
    fetch("http://localhost:8080/api/perfil/editar", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}` // Agregar el token
        },
        body: JSON.stringify(datosPerfil)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || "Error desconocido al editar el perfil");
                });
            }
            return response.json();
        })
        .then(data => {
            alert("Perfil actualizado exitosamente.");
            console.log("Datos actualizados:", data);

            // Opcional: Cerrar el modal y limpiar los campos
            cerrarModalEditarPerfil();
            document.getElementById("formEditarPerfil").reset();
        })
        .catch(error => {
            console.error("Error:", error.message);
            alert("Hubo un problema al actualizar el perfil: " + error.message);
        });
});
