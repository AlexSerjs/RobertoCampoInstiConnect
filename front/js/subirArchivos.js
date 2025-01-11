document.addEventListener("DOMContentLoaded", function() {
    const token = localStorage.getItem('token'); // Obtener token una sola vez

    const modalImagen = document.getElementById("modalImagen");
    const modalArchivo = document.getElementById("modalArchivo");

    const btnSubirImagen = document.getElementById("subirImagenBtn");
    const btnSubirArchivo = document.getElementById("subirArchivoBtn");

    const closeImagenModal = document.getElementById("closeImagenModal");
    const closeArchivoModal = document.getElementById("closeArchivoModal");

    const archivoPreview = document.getElementById("archivoPreview");

    // Mostrar el modal de imágenes
    btnSubirImagen.addEventListener("click", function() {
        mostrarModal(modalImagen);
    });

    // Mostrar el modal de archivos
    btnSubirArchivo.addEventListener("click", function() {
        mostrarModal(modalArchivo);
    });

    // Cerrar el modal de imágenes
    closeImagenModal.addEventListener("click", function() {
        cerrarModal(modalImagen);
    });

    // Cerrar el modal de archivos
    closeArchivoModal.addEventListener("click", function() {
        cerrarModal(modalArchivo);
    });

    // Cerrar el modal si se hace clic fuera del contenido
    window.addEventListener("click", function(event) {
        if (event.target === modalImagen) {
            cerrarModal(modalImagen);
        }
        if (event.target === modalArchivo) {
            cerrarModal(modalArchivo);
        }
    });

    // Subir imagen
    document.getElementById("submitImagenBtn").addEventListener("click", function(e) {
        e.preventDefault();
        const imagenInput = document.getElementById("imagenSubida");
        const imagen = imagenInput.files[0];
        if (imagen) {
            subirImagen(imagen); // Llamamos a la función para subir imagen
        } else {
            alert("Por favor, selecciona una imagen.");
        }
    });

    // Subir archivo
    document.getElementById("submitArchivoBtn").addEventListener("click", function(e) {
        e.preventDefault();
        const archivoInput = document.getElementById("archivoSubido");
        const archivo = archivoInput.files[0];
        if (archivo) {
            subirFichero(archivo); // Llamamos a la función para subir archivo
        } else {
            alert("Por favor, selecciona un archivo.");
        }
    });

    // Previsualizar la imagen seleccionada
    document.getElementById("imagenSubida").addEventListener("change", function(e) {
        const imagen = e.target.files[0];
        if (imagen && imagen.type.startsWith("image/")) {
            const reader = new FileReader();
            reader.onload = function(event) {
                archivoPreview.src = event.target.result;
                archivoPreview.style.display = "block";
            };
            reader.readAsDataURL(imagen);
        }
    });

    // Función para mostrar un modal
    function mostrarModal(modal) {
        modal.style.display = "block";
    }

    // Función para cerrar el modal
    function cerrarModal(modal) {
        modal.style.display = "none";
    }

   // Subir imagen
function subirImagen(imagen) {
    const tituloInput = document.getElementById("tituloImagen");
    let titulo = tituloInput.value.trim();

    if (!titulo) {
        alert("Por favor, escribe un título.");
        return;
    }

    titulo = titulo.toUpperCase();

    const formData = new FormData();
    formData.append("imagen", imagen);
    formData.append("titulo", titulo);

    fetch("http://localhost:8080/api/imagenes/subir", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + token
        },
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.url) {
            alert("Imagen subida exitosamente: " + data.url);
            cerrarModal(modalImagen); // Cerrar el modal de imagen
        } else {
            alert("Error: " + (data.error || "No se pudo subir la imagen"));
        }
    })
    .catch(error => {
        console.error("Error al subir la imagen", error);
        alert("Ocurrió un error al subir la imagen.");
    });
}

// Subir archivo
function subirFichero(archivo) {
    const tituloInput = document.getElementById("tituloArchivo");
    let titulo = tituloInput.value.trim();

    if (!titulo) {
        alert("Por favor, escribe un título.");
        return;
    }

    
    titulo = titulo.toUpperCase();

    const formData = new FormData();
    formData.append("fichero", archivo);
    formData.append("titulo", titulo);

    fetch("http://localhost:8080/api/ficheros/subir", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + token
        },
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.url) {
            alert("Fichero subido exitosamente: " + data.url);
            cerrarModal(modalArchivo); // Cerrar el modal de archivo
        } else {
            alert("Error: " + (data.error || "No se pudo subir el fichero"));
        }
    })
    .catch(error => {
        console.error("Error al subir el fichero", error);
        alert("Ocurrió un error al subir el fichero.");
    });
}

});
