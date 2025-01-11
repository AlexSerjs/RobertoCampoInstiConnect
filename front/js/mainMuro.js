const token = localStorage.getItem('token');

document.addEventListener('DOMContentLoaded', function () {
    if (!token) {
        window.location.href = 'indexFct.html'; // Redirige al login si no hay token
        return;
    }

    const fotoUsuario = document.getElementById("fotoUsuario");
    const menuPerfil = document.getElementById("menuPerfil");
    const enlacePerfil = document.getElementById("enlacePerfil");
    const cambiarFoto = document.getElementById("cambiarFoto");

    inicializarEventos();
    inicializarCambiarFoto();
    sincronizarFoto();
    actualizarInfoUsuario();

    // Sincronizar cada 60 60000 segundos
 // setInterval(sincronizarFoto, 6000);
});

// ---------------------------------------------------------------------------------------------------------
// Muro izquierda: Inicializar eventos


/**
 * Sincroniza la foto del usuario con el servidor.
 */
function sincronizarFoto() {
    fetch('http://localhost:8080/api/muro/obtenerFoto', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error al sincronizar la foto: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        const nuevaRutaFoto = data.rutaFoto;
        if (nuevaRutaFoto) {
            actualizarVistaFoto(`http://localhost:8080/${nuevaRutaFoto}`);
        }
    })
    .catch(error => {
        console.error("Error al sincronizar la foto:", error);
    });
}

/**
 * Actualiza la vista de la foto en el DOM.
 * @param {string} nuevaRutaFoto - Ruta de la nueva foto.
 */
function actualizarVistaFoto(nuevaRutaFoto) {
    const fotoUsuario = document.getElementById('fotoUsuario');
    if (fotoUsuario && nuevaRutaFoto) {
        fotoUsuario.src = `${nuevaRutaFoto}?t=${new Date().getTime()}`; // Marca de tiempo para evitar caché
    }
}



function inicializarEventos() {
    fotoUsuario.addEventListener("click", mostrarMenuPerfil);
    enlacePerfil.addEventListener("click", verPerfil);
    cambiarFoto.addEventListener("click", cambiarFotoUsuario);
    document.addEventListener("click", ocultarMenuSiEsNecesario);
}

function mostrarMenuPerfil() {
    menuPerfil.style.display = "flex";
}

function verPerfil(event) {
    event.preventDefault();
    menuPerfil.style.display = "none";

    if (!token) {
        alert("No se encontró el token. Por favor, inicia sesión.");
        window.location.href = 'indexFct.html';
        return;
    }

    fetch('http://localhost:8080/api/muro/tipoPerfil', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}` // Incluye el token en el encabezado
        }
    })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    alert("Sesión expirada. Por favor, inicia sesión nuevamente.");
                    window.location.href = 'indexFct.html';
                }
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Respuesta del servidor:", data); // Depuración
            const tipoUsuario = data.tipoUsuario;
            if (tipoUsuario === 'alumno') {
                window.location.href = 'PerfilAlumno.html';
            } else if (tipoUsuario === 'delegado') {
                window.location.href = 'PerfilDelegado.html';
            } else {
                alert("Tipo de usuario no reconocido");
            }
        })
        .catch(error => {
            console.error("Error en la solicitud:", error);
            alert("Hubo un error al intentar redirigir al perfil. Por favor, intenta nuevamente.");
        });
}

function cambiarFotoUsuario(event) {
    event.preventDefault();
    menuPerfil.style.display = "none";
}

function ocultarMenuSiEsNecesario(event) {
    if (!fotoUsuario.contains(event.target) && !menuPerfil.contains(event.target)) {
        menuPerfil.style.display = "none";
    }
}

// ---------------------------------------------------------------------------------------------------------
// Cambiar Foto: Inicializar eventos

function inicializarCambiarFoto() {
    const cambiarFotoBtn = document.getElementById('cambiarFoto');
    const modalCambiarFoto = document.getElementById('modalCambiarFoto');
    const cerrarModalBtn = document.getElementById('cerrarModalFoto');
    const subirFotoBtn = document.getElementById('subirFotoBtn');
    const nuevaFotoInput = document.getElementById('nuevaFoto');
    const previewImage = document.getElementById('previewImage');

    if (cambiarFotoBtn) {
        cambiarFotoBtn.addEventListener('click', function (event) {
            event.preventDefault();
            abrirModal(modalCambiarFoto);
        });
    }

    if (cerrarModalBtn) {
        cerrarModalBtn.addEventListener('click', function () {
            cerrarModal(modalCambiarFoto);
        });
    }

    if (nuevaFotoInput) {
        nuevaFotoInput.addEventListener('change', function () {
            mostrarPrevisualizacion(this, previewImage);
        });
    }

    if (subirFotoBtn) {
        subirFotoBtn.addEventListener('click', function () {
            manejarSubidaFoto(nuevaFotoInput, modalCambiarFoto);
        });
    }
}

// ---------------------------------------------------------------------------------------------------------
// Funciones generales


function manejarSubidaFoto(nuevaFotoInput, modal) {
    const archivo = nuevaFotoInput.files[0];

    if (!archivo) {
        alert('Por favor, selecciona una foto antes de subir.');
        return;
    }

    const tiposValidos = ['image/jpeg', 'image/png', 'image/jpg'];
    if (!tiposValidos.includes(archivo.type)) {
        alert('Solo se permiten imágenes en formato JPG o PNG.');
        return;
    }

    const formData = new FormData();
    formData.append('foto', archivo);

    fetch('http://localhost:8080/api/muro/cambiarFoto', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error al subir la foto');
        }
        return response.json();
    })
    .then(data => {
        console.log("Foto actualizada:", data.rutaFoto);
        actualizarVistaFoto(`http://localhost:8080/${data.rutaFoto}`);
        cerrarModal(modal);
    })
    .catch(error => {
        console.error("Error al cambiar la foto:", error);
        alert("Hubo un error al subir la foto. Inténtalo de nuevo.");
    });
}

function actualizarVistaFoto(nuevaRutaFoto) {
    const fotoUsuario = document.getElementById('fotoUsuario');
    if (nuevaRutaFoto) {
        // Actualiza el src de la imagen con una marca de tiempo para evitar caché
        fotoUsuario.src = `${nuevaRutaFoto}?t=${new Date().getTime()}`;
        
    } else {
        console.warn('No se recibió una ruta válida para la foto.');
    }
}

function abrirModal(modal) {
    if (modal) {
        modal.style.display = 'block';
        modal.style.opacity = '0';
        setTimeout(() => {
            modal.style.opacity = '1';
        }, 50);
    }
}

function cerrarModal(modal) {
    if (modal) {
        modal.style.opacity = '0';
        setTimeout(() => {
            modal.style.display = 'none';
        }, 300);
    }
}

function mostrarPrevisualizacion(input, previewImage) {
    const archivo = input.files[0];

    if (archivo) {
        const reader = new FileReader();
        reader.onload = function (e) {
            previewImage.src = e.target.result; 
        };
        reader.readAsDataURL(archivo);
    }
}

function actualizarInfoUsuario() {
    fetch('http://localhost:8080/api/muro/infoUsuario', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error al obtener la información del usuario: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        // Validar que los datos recibidos existen
        if (data && data.nombreCompleto && data.email) {
            // Seleccionar los elementos por clase
            const nombreCompletoElement = document.querySelector('.user-name');
            const emailElement = document.querySelector('.user-email');

            // Actualizar el contenido
            nombreCompletoElement.textContent = data.nombreCompleto; // Asigna solo el nombre
            emailElement.textContent = data.email; // Asigna solo el email
        } else {
            console.warn('Datos incompletos o inválidos recibidos del servidor:', data);
        }
    })
    .catch(error => {
        console.error('Error al obtener la información del usuario:', error);
    });
}

// Llamar a la función al cargar el DOM
document.addEventListener('DOMContentLoaded', function () {
    actualizarInfoUsuario();
});

// Función para cerrar sesión
function cerrarSesion() {
    // Confirmación opcional
    const confirmacion = confirm("¿Estás seguro de que deseas cerrar sesión?");
    if (!confirmacion) {
        return; // Si el usuario cancela, no hace nada
    }

    // Eliminamos el token del localStorage
    localStorage.removeItem("token");

    // Redirigimos al usuario a la página de inicio de sesión
    window.location.href = "indexFct.html"; // Cambia esto por la ruta de tu página de inicio de sesión
}



