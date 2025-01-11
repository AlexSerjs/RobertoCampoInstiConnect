document.addEventListener('DOMContentLoaded', function () {
    const statusMessage = document.getElementById('statusMessage');
    const verifyEmailButton = document.getElementById('verifyEmailButton');
    const sendEmailButton = document.getElementById('sendEmailButton');
    const loadingOverlay = document.getElementById('loadingOverlay');

    // Intentos almacenados en localStorage
    let attempts = parseInt(localStorage.getItem('verificationAttempts')) || 0;
    let isVerified = localStorage.getItem('isVerified') === 'true';

    function showLoading() {
        loadingOverlay.style.display = 'flex';
    }

    function hideLoading() {
        loadingOverlay.style.display = 'none';
    }

    // Verificar si la creación del perfil fue exitosa
    if (new URLSearchParams(window.location.search).get('status') === 'success') {
        statusMessage.textContent = "Perfil Creado";
        statusMessage.style.color = "green";

        // Si no está verificado, mostrar el botón para enviar el correo
        if (!isVerified) {
            sendEmailButton.style.display = 'inline-block';
        }

        // Si se alcanzó el límite de intentos
        if (attempts >= 3) {
            statusMessage.textContent = "Límite de reenvíos alcanzado.";
            statusMessage.style.color = "red";
            sendEmailButton.style.display = 'none';
            verifyEmailButton.style.display = 'none';
            setTimeout(() => {
                window.location.href = 'indexFct.html';
            }, 3000);
            return;
        }

        // Evento de click para reenviar el correo de verificación
        sendEmailButton.addEventListener('click', function () {
            if (attempts < 3) {
                attempts++;
                localStorage.setItem('verificationAttempts', attempts); // Guardar intentos
                showLoading();

                // Llamada para reenviar el correo
                reenviarCorreoVerificacion()
                    .then(() => {
                        hideLoading();
                        statusMessage.textContent = "Correo de verificación reenviado. Revisa tu bandeja de entrada.";
                        statusMessage.style.color = "green";

                        // Mostrar botón para verificar el correo solo después de enviarlo
                        verifyEmailButton.style.display = 'inline-block';
                    })
                    .catch(error => {
                        hideLoading();
                        statusMessage.textContent = `Error al reenviar. Intentos restantes: ${3 - attempts}`;
                        statusMessage.style.color = "red";

                        if (attempts >= 3) {
                            sendEmailButton.disabled = true;
                            verifyEmailButton.disabled = true;
                            sendEmailButton.textContent = "Límite de reenvíos alcanzado.";
                            setTimeout(() => {
                                window.location.href = 'indexFct.html';
                            }, 3000);
                        }
                    });
            } else {
                statusMessage.textContent = "Límite de reenvíos alcanzado.";
                statusMessage.style.color = "red";
                sendEmailButton.disabled = true;
                verifyEmailButton.disabled = true;
                setTimeout(() => {
                    window.location.href = 'indexFct.html';
                }, 3000);
            }
        });

        verifyEmailButton.addEventListener('click', function () {
            const verificationToken = localStorage.getItem('verificationToken');
            if (verificationToken) {
                showLoading();

                // Intentar verificar si el correo ya está verificado
                fetch(`http://localhost:8080/api/test-verify?token=${verificationToken}`, {
                    method: 'GET'
                })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    } else if (response.status === 400) {
                        throw new Error("Token inválido o expirado.");
                    } else {
                        throw new Error("Error al verificar el correo.");
                    }
                })
                .then(result => {
                    console.log(result); // <-- Aquí se imprime la respuesta del servidor
                    if (result && result.status && result.status.includes('verificado')) {
                        hideLoading();
                        statusMessage.textContent = "Cuenta verificada. Redirigiendo al inicio...";
                        statusMessage.style.color = "green";
                        verifyEmailButton.style.display = 'none';

                        // Guardar estado de verificación como true
                        localStorage.setItem('isVerified', 'true');

                        // Mostrar pantalla de carga y redirigir después de 3 segundos
                        showLoading();
                        setTimeout(() => {
                            window.location.href = 'indexFct.html';
                        }, 3000);
                    } else {
                        throw new Error("Verificación fallida. Vuelve a intentarlo.");
                    }
                })
                .catch(error => {
                    hideLoading();
                    statusMessage.textContent = `Error al verificar el correo: ${error.message}`;
                    statusMessage.style.color = "red";
                });
            }
        });

    } else {
        // Si el perfil no fue creado correctamente
        statusMessage.textContent = "Perfil No Creado";
        statusMessage.style.color = "red";
        const retryButton = document.createElement('button');
        retryButton.textContent = "Volver al Inicio";
        retryButton.addEventListener('click', function () {
            window.location.href = 'indexFct.html';
        });
        document.querySelector('.status-container').appendChild(retryButton);
    }

    // Función para reenviar el correo de verificación
    function reenviarCorreoVerificacion() {
        const verificationToken = localStorage.getItem('verificationToken');

        if (!verificationToken) {
            return Promise.reject(new Error("Token de verificación no está disponible."));
        }

        return fetch(`http://localhost:8080/api/resend-verification`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ token: verificationToken })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Error al reenviar el correo de verificación.");
                }
            });
    }
});
