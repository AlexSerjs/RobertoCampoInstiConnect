document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'indexFct.html';
        return;
    }

    cargarPublicaciones(token);
});

const formEncuesta = document.getElementById("form-encuesta");
const opcionesContainer = document.getElementById("opciones-container");
const addOpcionBtn = document.getElementById("add-opcion");

let opcionCount = 2; // Ya tenemos 2 opciones iniciales

// Evento para agregar nuevas opciones
addOpcionBtn.addEventListener("click", function () {
    if (opcionCount < 5) { // Permitir hasta 5 opciones en total
        opcionCount++;

        // Crear un nuevo campo de opción con un botón de eliminar
        const newOption = document.createElement("div");
        newOption.classList.add("form-group", "opcion");
        newOption.id = `opcion-container-${opcionCount}`;
        newOption.innerHTML = `
            <label for="opcion${opcionCount}">Opción ${opcionCount}:</label>
            <input type="text" id="opcion${opcionCount}" name="opcion[]" placeholder="Escribe la opción ${opcionCount}">
            <button type="button" class="eliminar-opcion" data-opcion="${opcionCount}">Eliminar</button>
        `;
        opcionesContainer.appendChild(newOption);

        // Evento para eliminar la opción
        newOption.querySelector(".eliminar-opcion").addEventListener("click", function () {
            opcionesContainer.removeChild(newOption); // Eliminar la opción del DOM
            opcionCount--;
            addOpcionBtn.style.display = "block"; // Mostrar botón de agregar si hay menos de 5 opciones
        });
    }
    
    // Si se alcanza el límite de 5 opciones, ocultar el botón de agregar
    if (opcionCount === 5) {
        addOpcionBtn.style.display = "none";
    }
});
formEncuesta.addEventListener("submit", function (event) {
    event.preventDefault(); // Evita que se recargue la página al enviar el formulario

    // Obtenemos los valores de los campos
    const pregunta = document.getElementById("pregunta").value.trim();
    const opciones = Array.from(document.querySelectorAll("input[name='opcion[]']"))
        .map(input => input.value.trim())
        .filter(opcion => opcion !== ""); // Filtrar opciones vacías

    // Validación básica
    if (!pregunta) {
        alert("Por favor, completa la pregunta.");
        return;
    }
    if (opciones.length < 2) {
        alert("Debes proporcionar al menos 2 opciones.");
        return;
    }

    const token = localStorage.getItem("token"); // Obtén el token de autorización
    if (!token) {
        alert("No estás autenticado. Por favor, inicia sesión.");
        return;
    }

    // Crear el cuerpo de la solicitud
    const encuestaData = {
        pregunta,
        opciones
    };

    console.log("Enviando datos al backend:", encuestaData); // Depuración

    // Enviar la encuesta al backend
    fetch("http://localhost:8080/api/encuestas", { // Cambié aquí la URL
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}` // Agregar el token en el header
        },
        body: JSON.stringify(encuestaData)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.error || "Error al crear la encuesta");
                });
            }
            return response.json();
        })
        .then(data => {
            console.log("Encuesta creada:", data);

            // Reiniciar el formulario después del envío exitoso
            formEncuesta.reset();
            opcionesContainer.innerHTML = ""; // Borra las opciones dinámicas
            opcionCount = 2; // Reinicia el contador de opciones a las iniciales

            // Vuelve a crear las dos primeras opciones
            opcionesContainer.innerHTML = `
                <div class="form-group opcion">
                    <label for="opcion1">Opción 1:</label>
                    <input type="text" id="opcion1" name="opcion[]" placeholder="Escribe la opción 1">
                </div>
                <div class="form-group opcion">
                    <label for="opcion2">Opción 2:</label>
                    <input type="text" id="opcion2" name="opcion[]" placeholder="Escribe la opción 2">
                </div>
            `;
            addOpcionBtn.style.display = "block"; // Vuelve a mostrar el botón
            alert("Encuesta creada con éxito!");
        })
        .catch(error => {
            console.error("Error:", error.message);
            alert(`Hubo un problema al crear la encuesta: ${error.message}`);
        });
});
