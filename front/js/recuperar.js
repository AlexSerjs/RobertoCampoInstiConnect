const formEmail = document.getElementById("form-email");
const formCodigo = document.getElementById("form-codigo");
const formPassword = document.getElementById("form-password");
const loadingOverlay = document.getElementById("loadingOverlay");

let emailGlobal = ""; // Variable para guardar el correo a lo largo del flujo

// Mostrar el overlay de carga
function mostrarOverlay() {
    loadingOverlay.style.display = "flex";
}

// Ocultar el overlay de carga
function ocultarOverlay() {
    loadingOverlay.style.display = "none";
}

// Manejar envío del formulario de correo
formEmail.addEventListener("submit", function (e) {
    e.preventDefault();
    const email = document.getElementById("email").value.trim();
    mostrarOverlay();

    fetch("http://localhost:8080/api/recuperar/enviar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((data) => {
                    throw new Error(data.error || "Correo no encontrado.");
                });
            }
            return response.json();
        })
        .then(() => {
            emailGlobal = email; // Guardar el correo para los siguientes pasos
            formEmail.classList.add("hidden");
            formCodigo.classList.remove("hidden");
        })
        .catch((error) => alert(error.message))
        .finally(() => ocultarOverlay());
});

// Manejar envío del formulario de código
formCodigo.addEventListener("submit", function (e) {
    e.preventDefault();
    const codigo = document.getElementById("codigo").value.trim();
    mostrarOverlay();

    fetch("http://localhost:8080/api/recuperar/verificar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: emailGlobal, codigo }),
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((data) => {
                    throw new Error(data.error || "Código incorrecto.");
                });
            }
            return response.json();
        })
        .then(() => {
            formCodigo.classList.add("hidden");
            formPassword.classList.remove("hidden");
        })
        .catch((error) => alert(error.message))
        .finally(() => ocultarOverlay());
});

// Manejar envío del formulario de nueva contraseña
formPassword.addEventListener("submit", function (e) {
    e.preventDefault();
    const nuevaContrasena = document.getElementById("nuevaContrasena").value.trim();
    const repetirContrasena = document.getElementById("repetirContrasena").value.trim();

    if (nuevaContrasena !== repetirContrasena) {
        alert("Las contraseñas no coinciden.");
        return;
    }
    mostrarOverlay();

    fetch("http://localhost:8080/api/recuperar/cambiar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: emailGlobal,
            codigo: document.getElementById("codigo").value.trim(), // Reutilizamos el código ingresado
            nuevaClave: nuevaContrasena,
        }),
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((data) => {
                    throw new Error(data.error || "Error al actualizar la contraseña.");
                });
            }
            return response.json();
        })
        .then(() => {
            alert("Contraseña actualizada con éxito.");
            window.location.href = "indexFct.html";
        })
        .catch((error) => alert(error.message))
        .finally(() => ocultarOverlay());
});

function togglePasswordVisibility(inputId, eyeOpenId, eyeClosedId) {
    const inputElement = document.getElementById(inputId);
    const eyeOpenElement = document.getElementById(eyeOpenId);
    const eyeClosedElement = document.getElementById(eyeClosedId);

    if (inputElement.type === "password") {
        inputElement.type = "text";
        eyeOpenElement.style.display = "none";
        eyeClosedElement.style.display = "block";
    } else {
        inputElement.type = "password";
        eyeOpenElement.style.display = "block";
        eyeClosedElement.style.display = "none";
    }
}