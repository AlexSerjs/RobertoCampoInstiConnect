document.addEventListener('DOMContentLoaded', async function () {
    const token = localStorage.getItem('token');

    if (!token) {
        // Si no hay token, redirige al login
        window.location.href = 'indexFct.html';
        return;
    }

    try {
        // Enviar solicitud al backend
        const response = await fetch('http://localhost:8080/api/administradores/api/administradores/detalles', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const data = await response.json();

            // Actualizar contenido dinámico
            document.getElementById('admin-name').textContent = data.nombre;
            document.getElementById('admin-email').textContent = data.email;
        } else if (response.status === 404) {
            // alert('Administrador no encontrado.');
        } else {
            // alert('Error al obtener los detalles. Por favor, intenta más tarde.');
        }
    } catch (error) {
        // console.error('Error en el fetch:', error);
        // alert('Ocurrió un error al conectar con el servidor.');
    }

    // Añadir funcionalidad para el botón "Cerrar Sesión"
    const cerrarSesionBtn = document.getElementById('cerrar-sesion-btn');
    cerrarSesionBtn.addEventListener('click', function () {
        // Eliminar el token del localStorage
        localStorage.removeItem('token');

        // Redirigir al login
        window.location.href = 'indexFct.html';
    });
});
