const token = localStorage.getItem('token');

document.addEventListener('DOMContentLoaded', function () {
    if (token) {
        cargarPerfilUsuario(token);
        verificarHorarioExiste(token);
        verificarPermisoPublicar(token);
        cargarAsignaturasDelGrupo(token);

        // Configurar intervalos para actualizar dinámicamente la información
        //setInterval(() => verificarPermisoPublicar(token), 1000);
        //setInterval(() => cargarHorarioAlumno(token), 1000);
        //setInterval(() => cargarAsignaturasDelGrupo(token), 1000);

        sincronizarFoto();
        history.replaceState(null, '', window.location.href); // Elimina parámetros en la URL si los hay
    } else {
        // Redirige al login si no hay token
        window.location.href = 'indexFct.html';
    }


      //------------------------------------------------------------------------------------
    // Configurar WebSocket para permisos
    const socketPermiso = conectarPermisoWebSocket(token, (mensaje) => {
        switch (mensaje.tipo) {
            case 'ACTUALIZACION_PERMISO':
                console.log('Permiso actualizado:', mensaje.mensaje);
                verificarPermisoPublicar(token); // Recargar la lista de integrantes dinámicamente
                break;
            case 'CONFIRMACION':
                console.log('Confirmación recibida:', mensaje.mensaje);
                break;
            default:
                console.warn('Tipo de mensaje no reconocido:', mensaje.tipo);
        }
    });

    socketPermiso.onerror = (error) => {
        console.error('Error en WebSocket de permisos:', error);
        alert('Hubo un problema con la conexión al servidor de permisos.');
    };

    socketPermiso.onclose = () => {
        console.warn('WebSocket de permisos cerrado. Intentando reconectar...');
        setTimeout(() => conectarPermisoWebSocket(token, mensajeCallback), 5000); // Reintenta después de 5 segundos
    };
    //------------------------------------------------------------------------------------
   // Configurar WebSocket para asignaturas
const socketAsignaturas = conectarAsignaturaWebSocket(token, (mensaje) => {
    switch (mensaje.tipo) {
        case 'ACTUALIZACION_ASIGNATURA':
            console.log('Asignatura actualizada:', mensaje.mensaje);
            cargarAsignaturasDelGrupo(token); // Recargar las asignaturas dinámicamente
            break;
        case 'NUEVA_ASIGNATURA': // Caso para nueva asignatura
            console.log('Nueva asignatura añadida:', mensaje.mensaje);
            cargarAsignaturasDelGrupo(token); // Recargar las asignaturas dinámicamente
            break;
        case 'ELIMINAR_ASIGNATURA': // Caso para eliminar asignatura
            console.log('Asignatura eliminada:', mensaje.mensaje);
            cargarAsignaturasDelGrupo(token); // Recargar las asignaturas dinámicamente
            break;
        case 'CONFIRMACION':
            //console.log('Confirmación recibida:', mensaje.mensaje);
            break;
        default:
            console.warn('Tipo de mensaje no reconocido:', mensaje.tipo);
    }
});

socketAsignaturas.onerror = (error) => {
    console.error('Error en WebSocket:', error);
    alert('Hubo un problema con la conexión al servidor.');
};

socketAsignaturas.onclose = () => {
    console.warn('WebSocket de asignaturas cerrado. Intentando reconectar...');
    setTimeout(() => conectarAsignaturaWebSocket(token, mensajeCallback), 5000); // Reintenta después de 5 segundos
};

   //-------------------------------------------------------------------------------------
    // Configurar WebSocket para horario
    const socketHorario = conectarHorarioWebSocket(token, (mensaje) => {
        switch (mensaje.tipo) {
            case 'ACTUALIZACION_HORARIO':
                console.log('Horario actualizado:', mensaje.mensaje);
                cargarHorarioAlumno(token); // Recargar el horario dinámicamente
                break;
            default:
                console.warn('Tipo de mensaje no reconocido:', mensaje.tipo);
        }
    });
    socketHorario.onerror = (error) => {
        console.error('Error en WebSocket:', error);
        alert('Hubo un problema con la conexión al servidor. Intenta nuevamente.');
    };
    
    socketHorario.onclose = () => {
        console.warn('WebSocket cerrado. Intentando reconectar...');
        setTimeout(() => conectarHorarioWebSocket(token, mensajeCallback), 5000); // Reintenta después de 5 segundos
    };
    
//--------------------------------------------------------------------------------------
 // Conectar al WebSocket para notificaciones de perfil
 const socket = conectarWebSocket(token);

 socket.onmessage = (event) => {
     try {
         const mensaje = JSON.parse(event.data);
         switch (mensaje.tipo) {
             case "ACTUALIZACION_NOMBRE":
                // console.log("Notificación de nombre:", mensaje.mensaje);
               //  alert(mensaje.mensaje);
                 cargarPerfilUsuario(token); // Actualizar dinámicamente el perfil
                 break;

             case "ACTUALIZACION_CONTRASENA":
                // console.log("Notificación de contraseña:", mensaje.mensaje);
               //  alert(mensaje.mensaje);
                 break;

             default:
               //  console.warn("Tipo de mensaje no reconocido:", mensaje.tipo);
         }
     } catch (error) {
        // console.error("Error al procesar el mensaje del WebSocket:", event.data, error);
     }
 };

 socket.onerror = (error) => {
   //  console.error("Error en el WebSocket:", error);
 };

 socket.onclose = () => {
    // console.warn("WebSocket cerrado.");
 };
});

// Función para conectar al WebSocket
function conectarWebSocket(token) {
    const socket = new WebSocket("ws://localhost:8080/ws/datos-alumno"); // Ruta del WebSocket para datos de alumno

    socket.onopen = () => {
      //  console.log("WebSocket de datos de alumno conectado.");
    };

    return socket;
}

// *** Función para cargar los datos del perfil del usuario ***
function cargarPerfilUsuario(token) {
    fetch(`http://localhost:8080/api/alumnos/info`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error al obtener los datos del perfil');
        }
        return response.json();
    })
    .then(data => {
        // Asignar los valores a los elementos del DOM
        document.getElementById('nombre_completo').textContent = data.nombreCompleto || '';
        document.getElementById('ciclo_formativo').textContent = data.grado || '';
        document.getElementById('comunidad_autonoma').textContent = data.nombreComunidadAutonoma || '';
        document.getElementById('nombre_instituto').textContent = data.nombreInstituto || '';
        document.getElementById('anio_lectivo').textContent = data.anioLectivo || '';
        document.getElementById('email').textContent = data.correo || '';

        const profilePhoto = document.getElementById('fotoUsuario');
        if (data.foto && data.foto !== "iconDefault.jpg") {
            actualizarVistaFoto(`http://localhost:8080/${data.foto}`);
        }
    })
    .catch(error => {
        console.error('Error al cargar el perfil del usuario:', error);
    });
}

// *** Función para verificar si existe un horario ***
function verificarHorarioExiste(token) {
    fetch('http://localhost:8080/api/horario/existe', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error al verificar el horario');
        }
        return response.json();
    })
    .then(data => {
        if (data.existe) {
            cargarHorarioAlumno(token);
        } else {
            document.querySelector('.schedule-container').innerHTML = '<p>Horario en proceso, el delegado lo actualizará pronto.</p>';
        }
    })
    .catch(error => {
        console.error('Error al verificar el horario:', error);
        document.querySelector('.schedule-container').innerHTML = '<p>No se pudo verificar el horario. Intenta más tarde.</p>';
    });
}

// *** Función para cargar el horario del alumno ***
function cargarHorarioAlumno(token) {
    fetch('http://localhost:8080/api/horario/alumno', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error al obtener el horario');
        }
        return response.json();
    })
    .then(data => {
        const tbody = document.querySelector('.schedule-table tbody');
        tbody.innerHTML = ''; // Limpiar cualquier contenido previo

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">No hay horario disponible.</td></tr>';
            return;
        }

        data.forEach(horario => {
            const row = document.createElement('tr');
            const horaCell = document.createElement('td');
            horaCell.textContent = `${horario.horaInicio || ''} - ${horario.horaFin || ''}`;
            row.appendChild(horaCell);

            ['lunes', 'martes', 'miercoles', 'jueves', 'viernes'].forEach(dia => {
                const cell = document.createElement('td');
                cell.textContent = horario[dia] || 'Libre';
                row.appendChild(cell);
            });

            tbody.appendChild(row);
        });
    })
    .catch(error => {
        console.error('Error al cargar el horario:', error);
    });
}

// *** Función para verificar permiso de publicación ***
let permisoAnterior = null;

function verificarPermisoPublicar(token) {
    fetch('http://localhost:8080/api/alumnos/puede-publicar', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        const permiso = data.puedePublicar;

        if (!permiso && permisoAnterior !== false) {
            document.body.style.backgroundColor = '#FFEBEE';
            alert("No tienes permiso para publicar.");
        } else if (permiso && permisoAnterior === false) {
            document.body.style.backgroundColor = '#E0F7FA';
            alert("Ya tienes permiso para publicar.");
        }

        permisoAnterior = permiso;
    })
    .catch(error => {
        console.error('Error al verificar permiso de publicar:', error);
    });
}

// *** Función para cargar las asignaturas del grupo ***
function cargarAsignaturasDelGrupo(token) {
    fetch('http://localhost:8080/api/horario/asignaturas', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error al obtener las asignaturas');
        }
        return response.json();
    })
    .then(data => {
        const tbody = document.querySelector('.asignaturas-table tbody');
        tbody.innerHTML = ''; // Limpiar contenido previo

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3">No hay asignaturas disponibles.</td></tr>';
            return;
        }

        data.forEach(asignatura => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${asignatura.nombre || ''}</td>
                <td>${asignatura.profesor || ''}</td>
                <td>${asignatura.email || ''}</td>
            `;
            tbody.appendChild(row);
        });
    })
    .catch(error => {
        console.error('Error al cargar las asignaturas:', error);
    });
}

// *** Sincronizar foto ***
function sincronizarFoto() {
    fetch('http://localhost:8080/api/muro/obtenerFoto', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error al sincronizar la foto: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        const nuevaRutaFoto = data.rutaFoto;
        actualizarVistaFoto(nuevaRutaFoto ? `http://localhost:8080/${nuevaRutaFoto}` : 'images/iconDefault.jpg');
    })
    .catch(error => {
        console.error("Error al sincronizar la foto:", error);
    });
}

function actualizarVistaFoto(nuevaRutaFoto) {
    const fotoUsuario = document.getElementById('fotoUsuario');
    if (fotoUsuario) {
        fotoUsuario.src = `${nuevaRutaFoto}?t=${new Date().getTime()}`;
    }
}
