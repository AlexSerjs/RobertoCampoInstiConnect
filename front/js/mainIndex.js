// *** Verificar autenticación al cargar la página de login ***
document.addEventListener('DOMContentLoaded', function () {
    // Eliminar el token si existe al cargar la página de login
    const token = localStorage.getItem('token');
    if (token) {
        localStorage.removeItem('token');
    }
});

// *** Manejo del formulario de login ***
document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    // Mostrar el overlay de carga
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'flex';
    }

    // Obtener valores del formulario
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!username || !password) {
        alert('Por favor, ingresa tanto el nombre de usuario/correo como la contraseña.');
        if (loadingOverlay) {
            loadingOverlay.style.display = 'none';
        }
        return;
    }

    try {
        // Enviar solicitud de login
        const response = await fetch('http://localhost:8080/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ correo: username, clave: password })
        });

        if (response.ok) {
            const data = await response.json();
            const token = data.token;
            const tipoUsuario = data.tipo; // Se espera que el backend devuelva este campo

            // Guardar el token en el localStorage
            localStorage.setItem('token', token);

            // Redirigir según el tipo de usuario
            if (tipoUsuario === 'administrador') {
                window.location.href = 'perfilAdmin.html';
            } else {
                window.location.href = 'muro.html';
            }
        } else if (response.status === 401 || response.status === 404) {
            alert('Usuario o contraseña incorrectos. Inténtalo de nuevo.');
        } else {
            alert('No se pudo iniciar sesión. Verifica tus datos o intenta más tarde.');
        }
    } catch (error) {
        console.error('Error en el login:', error);
        alert('Ocurrió un error durante el inicio de sesión. Inténtalo de nuevo más tarde.');
    } finally {
        // Ocultar el overlay de carga
        if (loadingOverlay) {
            loadingOverlay.style.display = 'none';
        }
    }
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
