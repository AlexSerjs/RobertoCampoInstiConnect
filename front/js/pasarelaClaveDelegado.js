export async function validarClaveModal() {
    return new Promise((resolve) => {
        // Mostrar el modal
        const modalClave = document.getElementById('claveDelegadoModal');
        const validarClaveDelegadoBtn = document.getElementById('validarClaveDelegadoBtn');
        const cerrarClaveDelegadoModalBtn = document.getElementById('cerrarClaveDelegadoModalBtn');
        const claveDelegadoInput = document.getElementById('claveDelegadoInput');
        const claveDelegadoError = document.getElementById('claveDelegadoError');

        // Mostrar modal
        modalClave.style.display = 'block';
        claveDelegadoInput.value = ''; // Limpiar el input
        claveDelegadoError.style.display = 'none'; // Ocultar mensaje de error

        // Cerrar modal si se cancela
        cerrarClaveDelegadoModalBtn.addEventListener('click', function closeModal() {
            modalClave.style.display = 'none';
            cerrarClaveDelegadoModalBtn.removeEventListener('click', closeModal);
            resolve(false); // Devuelve `false` al cancelar
        });

        // Validar la clave
        validarClaveDelegadoBtn.addEventListener('click', async function validarClave() {
            const clave = claveDelegadoInput.value.trim();

            if (!clave) {
                claveDelegadoError.textContent = 'Por favor, ingrese una clave.';
                claveDelegadoError.style.display = 'block';
                return;
            }

            try {
                const response = await fetch(`http://localhost:8080/api/admin/claves/verificar-clave?clave=${encodeURIComponent(clave)}`, {
                    method: 'GET',
                    headers: { 'Content-Type': 'application/json' },
                });

                if (!response.ok) {
                    throw new Error('Error al validar la clave');
                }

                const data = await response.json();

                if (data.valida) {
                    modalClave.style.display = 'none'; // Cerrar modal
                    validarClaveDelegadoBtn.removeEventListener('click', validarClave);
                    resolve(true); // Devuelve `true` si la clave es válida
                } else {
                    claveDelegadoError.textContent = 'Clave incorrecta. Intente nuevamente.';
                    claveDelegadoError.style.display = 'block';
                }
            } catch (error) {
                console.error('Error al validar la clave:', error);
                claveDelegadoError.textContent = 'Hubo un problema. Inténtelo más tarde.';
                claveDelegadoError.style.display = 'block';
            }
        });
    });
}
