document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token"); // Obtener el token desde localStorage

    const formComunicado = document.getElementById("form-comunicado");
    const tituloInput = document.getElementById("titulo-comunicado");
    const comunicadoInput = document.getElementById("comunicado");

    formComunicado.addEventListener("submit", function (e) {
        e.preventDefault(); // Evita que la página se recargue al enviar el formulario

        const titulo = tituloInput.value.trim();
        const contenido = comunicadoInput.value.trim();

        if (!titulo || !contenido) {
            alert("Por favor, completa todos los campos.");
            return;
        }
        
        const comunicadoData = {
            titulo: titulo,
            contenido: contenido,
        };

        fetch("http://localhost:8080/api/comunicados/comunicado", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + token,
            },
            body: JSON.stringify(comunicadoData),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.json().then((errorData) => {
                        throw new Error(errorData.error || "Error desconocido al crear el comunicado.");
                    });
                }
                return response.json();
            })
            .then((data) => {
              //  alert("Comunicado creado exitosamente.");
               // console.log("Comunicado creado:", data);
                tituloInput.value = "";
                comunicadoInput.value = "";
            })
            .catch((error) => {
              //  console.error("Error:", error.message);
                alert("Hubo un problema al crear el comunicado: " + error.message);
            });
    });
});
