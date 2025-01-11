document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token"); // Obtener el token desde localStorage
    const codigoGrupoElement = document.getElementById("codigoGrupo");

    if (!token) {
        window.location.href = 'indexFct.html';
        return;
    } 

    fetch("http://localhost:8080/api/alumnos/codigo-grupo-delegado", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token,
        },
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((errorData) => {
                    throw new Error(errorData.error || "Error desconocido al obtener el código del grupo.");
                });
            }
            return response.json();
        })
        .then((data) => {
            // Actualizar el contenido del <span> con el código del grupo
            codigoGrupoElement.textContent = data.codigoGrupo || "No disponible";
        })
        .catch((error) => {
            console.error("Error:", error.message);
            codigoGrupoElement.textContent = "Error al obtener el código del grupo.";
        });
});
