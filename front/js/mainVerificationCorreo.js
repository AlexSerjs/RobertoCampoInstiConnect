// Función para verificar el código
function verificarCodigo() {
    const userCode = document.getElementById('verificationCode').value.trim();
    const correctCode = '34452'; 

    if (userCode === '') {
        alert('Por favor, introduce el código de verificación.');
    } else if (userCode === correctCode) {
        // Redirigir inmediatamente sin mostrar mensajes
        window.location.replace ("../frontend/perfilUsuario.html");
    } else {
        alert('Código incorrecto. Por favor, intenta de nuevo.');
    }
}

// Escuchar el evento de clic en el botón de verificación
document.getElementById('verifyButton').addEventListener('click', function(e) {
    e.preventDefault(); 
    verificarCodigo();
});

// Escuchar el evento de tecla presionada en el campo de código de verificación
document.getElementById('verificationCode').addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault(); 
        verificarCodigo(); 
    }
});

// Simulación del reenvío del código
document.getElementById('resendBtn').addEventListener('click', function() {
    alert('Se ha reenviado un nuevo código de verificación a tu correo.');
    
});
