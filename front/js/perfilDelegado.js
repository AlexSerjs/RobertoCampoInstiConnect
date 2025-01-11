const token = localStorage.getItem('token'); 
document.addEventListener('DOMContentLoaded', function () {

    if (!token) {
        window.location.href = 'indexFct.html';
        return;
    } 

   // Cargar información inicial
    cargarPerfilUsuario(token);
    cargarIntegrantes(token);
    cargarAsignaturasGuardadas(token);
    loadSchedule();
    sincronizarFoto();

     //--------------------------------------------------------------------------------------------
    // Configurar WebSocket para permisos
    const socketPermiso = conectarPermisoWebSocket(token, (mensaje) => {
        switch (mensaje.tipo) {
            case 'ACTUALIZACION_PERMISO':
                console.log('Permiso actualizado:', mensaje.mensaje);
                cargarIntegrantes(token); // Recargar la lista de integrantes
                break;
            case 'CONFIRMACION':
                console.log('Confirmación del servidor:', mensaje.mensaje);
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

    //------------------------------------------------------------------------------
    const socketHorario = conectarHorarioWebSocket(token, (mensaje) => {
        switch (mensaje.tipo) {
            case 'ACTUALIZACION_HORARIO':
                console.log('Notificación de horario actualizado:', mensaje.mensaje);
                loadSchedule(); // Recargar vista del horario
                break;
            case 'CONFIRMACION':
                console.log('Confirmación del servidor:', mensaje.mensaje);
                break;
            default:
                console.warn('Tipo de mensaje no reconocido:', mensaje.tipo);
        }
    });
    socketHorario.onerror = (error) => {
        //console.error('Error en WebSocket:', error);
        alert('Hubo un problema con la conexión al servidor. Intenta nuevamente.');
    };
    
    socketHorario.onclose = () => {
        //console.warn('WebSocket cerrado. Intentando reconectar...');
        setTimeout(() => conectarHorarioWebSocket(token, mensajeCallback), 5000); // Reintenta después de 5 segundos
    };
    

    history.replaceState(null, '', window.location.href);

    //--------------------------------------------------------------------------------------------
    // Conectar al WebSocket para notificaciones de perfil
    const socket = conectarWebSocket(token);

    socket.onmessage = (event) => {
        try {
            const mensaje = JSON.parse(event.data);
            switch (mensaje.tipo) {
                case "ACTUALIZACION_NOMBRE":
                // console.log("Notificación de nombre:", mensaje.mensaje);
                // alert(mensaje.mensaje);
                    cargarPerfilUsuario(token); // Actualizar dinámicamente el perfil
                    break;

                case "ACTUALIZACION_CONTRASENA":
                //console.log("Notificación de contraseña:", mensaje.mensaje);
                    alert(mensaje.mensaje);
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
    setTimeout(() => conectarWebSocket(token, mensajeCallback), 5000);
    };
        
        
        
        // Eventos
        configurarEventosAsignaturas();
        configurarEventosHorario();
        // Llamada para configurar eventos al cargar la página

});

// Función para conectar al WebSocket
function conectarWebSocket(token) {
    const socket = new WebSocket("ws://localhost:8080/ws/datos-alumno"); // Ruta del WebSocket para datos de alumno

    socket.onopen = () => {
      //  console.log("WebSocket de datos de alumno conectado.");
    };

    return socket;
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Funciones de Perfil
//---------------------------------------------------------------------------------------------------------------------------------------------------------------
  
// Función para cargar perfil del usuario

function cargarPerfilUsuario(token) {
    fetch(`http://localhost:8080/api/alumnos/info`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Error al obtener los datos del perfil');
        return response.json();
    })
    .then(data => {
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
    .catch(error => console.error('Error al cargar el perfil del usuario:', error));
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Función para cargar integrantes del grupo
function cargarIntegrantes(token) {
    fetch(`http://localhost:8080/api/grupo/integrantes`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Error al obtener los integrantes del grupo');
        return response.json();
    })
    .then(integrantes => {
        const groupTableBody = document.querySelector('.group-table tbody');
        groupTableBody.innerHTML = '';

        if (integrantes.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="5" style="text-align: center;">No hay integrantes en el grupo</td>`;
            groupTableBody.appendChild(row);
        } else {
            integrantes.forEach(integrante => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${integrante.nombreCompleto.toUpperCase()}</td>
                    <td>${integrante.email}</td>
                    <td><span class="verification-indicator ${integrante.verificado ? 'verified' : 'not-verified'}"></span></td>
                    <td><input type="checkbox" class="publish-toggle" data-id="${integrante.id}" ${integrante.puedePublicar ? 'checked' : ''}></td>
                    <td>${integrante.status.toUpperCase()}</td>
                `;
                groupTableBody.appendChild(row);
            });

            document.querySelectorAll('.publish-toggle').forEach(checkbox => {
                checkbox.addEventListener('change', (event) => {
                    const alumnoId = event.target.getAttribute('data-id');
                    const puedePublicar = event.target.checked;
                
                    if (alumnoId && alumnoId !== "undefined") {
                        actualizarPermisoPublicacion(alumnoId, puedePublicar, token);
                    } else {
                        console.error('Alumno ID está undefined o vacío');
                    }
                });
            });
        }
    })
    .catch(error => console.error('Error al cargar los integrantes del grupo:', error));
}


// Función para actualizar el permiso de publicación
function actualizarPermisoPublicacion(alumnoId, puedePublicar, token) {
    fetch(`http://localhost:8080/api/grupo/integrantes/${alumnoId}/publicar?puedePublicar=${puedePublicar}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
           // console.log('Permiso de publicación actualizado correctamente');
        } else {
            throw new Error('Error al actualizar el permiso de publicación');
        }
    })
    .catch(error => console.error('Error al actualizar el permiso de publicación:', error));
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Funciones de Asignaturas
//---------------------------------------------------------------------------------------------------------------------------------------------------------------
 
// Evento para agregar asignatura
function configurarEventosAsignaturas() {
    const addSubjectBtn = document.getElementById('addSubjectBtn');
    if (addSubjectBtn) {
        addSubjectBtn.addEventListener('click', function () {
            const subjectElement = document.getElementById('subject');
            const professorElement = document.getElementById('professor');
            const emailElement = document.getElementById('emailInput');
            const subject = subjectElement ? subjectElement.value.toUpperCase() : ''; 
            const professor = professorElement ? professorElement.value.toUpperCase() : ''; 
            const email = emailElement ? emailElement.value : '';

            if (subject && professor && email) {
                agregarAsignatura({ nombre: subject, profesor: professor, email }, token);
                subjectElement.value = '';
                professorElement.value = '';
                emailElement.value = '';
            } else {
                alert('Por favor, completa todos los campos.');
            }
        });
    } else {
        console.error("No se encontró el botón 'addSubjectBtn'");
    }

    const saveChangesBtn = document.getElementById('saveChangesBtn');
    if (saveChangesBtn) {
        saveChangesBtn.addEventListener('click', function () {
            guardarCambiosAsignatura();
        });
    }

    const closeModalBtn = document.getElementById('closeModalBtn');
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', function () {
            cerrarModal();
        });
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Función para agregar una asignatura
function agregarAsignatura(asignatura, token) {
    console.log("Datos enviados:", JSON.stringify(asignatura)); // Log para verificar los datos
    fetch(`http://localhost:8080/api/grupo/agregarAsignaturas`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(asignatura)
    })
    .then(response => {
        if (response.ok) {
            console.log('Asignatura agregada correctamente');
            cargarAsignaturasGuardadas(token);  // Cargar asignaturas actualizadas
            document.getElementById('subject').value = '';
            document.getElementById('professor').value = '';
            document.getElementById('emailInput').value = ''; // Usar el id correcto
        } else {
            throw new Error('Error al agregar la asignatura');
        }
    })
    .catch(error => console.error('Error al agregar la asignatura:', error));
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Función para Cargar Asignaturas 

function cargarAsignaturasGuardadas(token) {
    fetch(`http://localhost:8080/api/grupo/asignaturas`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(asignaturas => {
        const tableBody = document.getElementById('savedSubjectsBody');
        tableBody.innerHTML = '';

        asignaturas.forEach(asignatura => {
            const row = document.createElement('tr');
            row.id = `asignatura-${asignatura.id}`; // ID único para cada fila basado en el ID

            row.innerHTML = `
                <td>${asignatura.nombre}</td>
                <td>${asignatura.profesor}</td>
                <td>${asignatura.email}</td>
               <td>
                <button onclick="editarAsignatura('${asignatura.id}', '${asignatura.nombre}', '${asignatura.profesor}', '${asignatura.email}')">Modificar</button>
                <button onclick="eliminarAsignatura('${asignatura.id}', token)">Eliminar</button>
               </td>
            `;
            tableBody.appendChild(row);
        });
    })
    .catch(error => console.error('Error al cargar las asignaturas:', error));
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Función para eliminar una asignatura 
function eliminarAsignatura(id, token) {
    const confirmar = confirm("¿Estás seguro de que quieres borrar esta asignatura?");

    if (confirmar) {
        console.log(`ID enviado para eliminar: ${id}`);
        fetch(`http://localhost:8080/api/grupo/eliminarAsignaturas?id=${encodeURIComponent(id)}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error en la respuesta del servidor: ${response.status}`);
            }
            return response.text();
        })
        .then(data => {
            console.log('Respuesta del servidor:', data);
            cargarAsignaturasGuardadas(token); // Refresca la lista de asignaturas
        })
        .catch(error => console.error('Error al eliminar la asignatura:', error));
    } else {
        console.log('Eliminación cancelada por el usuario');
    }
}



//---------------------------------------------------------------------------------------------------------------------------------------------------------------
       
let asignaturaIdActual; // Para almacenar el ID de la asignatura que se está editando

// Función para editar asignatura
function editarAsignatura(id, nombre, profesor, email) {
    asignaturaIdActual = id; // Guardar el ID actual

    // Cargar los datos actuales en el formulario
    document.getElementById('editSubject').value = nombre;
    document.getElementById('editProfessor').value = profesor;
    document.getElementById('editEmail').value = email;

    // Mostrar el modal centrado
    const modal = document.getElementById('editModal');
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
}

// Guardar cambios de asignatura
async function guardarCambiosAsignatura() {
    const nombre = document.getElementById('editSubject').value.trim().toUpperCase();
    const profesor = document.getElementById('editProfessor').value.trim().toUpperCase();
    const email = document.getElementById('editEmail').value.trim();

    const asignaturaModificada = { nombre, profesor, email };

    if (asignaturaIdActual) {
        await actualizarAsignatura(asignaturaIdActual, asignaturaModificada);
    } else {
        console.error('ID de asignatura no encontrado. No se puede proceder con la actualización.');
    }
    
    cerrarModal();
}

// Cerrar el modal
function cerrarModal() {
    document.getElementById('editModal').style.display = 'none';
}
//---------------------------------------------------------------------------------------------------------------------------------------------------------------
 
// Función de actualización de asignatura
async function actualizarAsignatura(asignaturaId, asignatura) {
    try {
        const response = await fetch(`http://localhost:8080/api/grupo/actualizarAsignatura/${asignaturaId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(asignatura)
        });

        if (response.ok) {
            console.log('Asignatura actualizada correctamente');
            cargarAsignaturasGuardadas(token); // Refresca la lista de asignaturas
        } else {
            throw new Error('Error al actualizar la asignatura');
        }
    } catch (error) {
        console.error('Error al actualizar la asignatura:', error);
    }
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------
// Funciones de Horario
//---------------------------------------------------------------------------------------------------------------------------------------------------------------
function loadSchedule() {
    const createDefaultScheduleBtn = document.getElementById("createDefaultScheduleBtn");
    const modifyScheduleBtn = document.getElementById("modifyScheduleBtn");
    const scheduleContainer = document.getElementById("scheduleContainer");
    const scheduleButtons = document.getElementById("scheduleButtons");

    fetch("http://localhost:8080/api/horario/existe", {
        headers: { "Authorization": `Bearer ${token}` }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Network response was not ok. Status: " + response.status);
        }
        return response.json();
    })
    .then(data => {
     //  console.log("Respuesta del servidor:", data); // Verifica si existe el horario
     //   console.log("Estado del botón Crear Horario antes:", createDefaultScheduleBtn.style.display);
        const horarioExiste = false; 
        if (data.existe) {
            createDefaultScheduleBtn.style.display = "none";
            modifyScheduleBtn.style.display = "block";
            scheduleContainer.style.display = "block";
            scheduleButtons.style.display = "flex";
            populateSchedule();
        } else {
            createDefaultScheduleBtn.style.display = "block";
            modifyScheduleBtn.style.display = "none";
            scheduleContainer.style.display = "none";
            scheduleButtons.style.display = "none";
        }
      //  console.log("Estado del botón Crear Horario después:", createDefaultScheduleBtn.style.display);
    })
    
    .catch(error => console.error("Error al verificar el horario:", error));
}

// Llamar a loadSchedule al cargar la página
window.onload = loadSchedule;


//---------------------------------------------------------------------------------------------------------------------------------------------------------------

function populateSchedule() {
    fetch("http://localhost:8080/api/horario/datos", {
        headers: { "Authorization": `Bearer ${token}` }
    })
    .then(response => response.json())
    .then(horarioData => {
        //console.log("Datos recibidos:", horarioData); // Muestra los datos en la consola
        const horarioBody = document.getElementById("horarioBody");
        horarioBody.innerHTML = "";  // Limpia la tabla antes de agregar nuevas filas

        horarioData.forEach(item => {
            const row = document.createElement("tr");
            row.innerHTML = `
                 <td>${item.horaInicio.substring(0, 5)}</td>  
                <td>${item.horaFin.substring(0, 5)}</td>
                <td>${item.lunes || "Libre"}</td>
                <td>${item.martes || "Libre"}</td>
                <td>${item.miercoles || "Libre"}</td>
                <td>${item.jueves || "Libre"}</td>
                <td>${item.viernes || "Libre"}</td>
            `;
            horarioBody.appendChild(row);
        });
    })
    .catch(error => console.error("Error al llenar el horario:", error));
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------------

function configurarEventosHorario() {
    const createDefaultScheduleBtn = document.getElementById("createDefaultScheduleBtn");

    if (createDefaultScheduleBtn) {
        createDefaultScheduleBtn.addEventListener("click", () => {
            fetch("http://localhost:8080/api/horario/crearPredeterminado", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Network response was not ok. Status: " + response.status);
                }
                return response.text();
            })
            .then(data => {
                console.log("Horario predeterminado creado:", data);
                loadSchedule(); // Recargar el horario para mostrarlo actualizado
            })
            .catch(error => console.error("Error al crear el horario predeterminado:", error));
        });
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------------

// Función para abrir el modal de horario y cargar los datos
function abrirModalHorario() {
    // Ocultar el botón "Modificar Horario" al hacer clic
    document.getElementById("modifyScheduleBtn").style.display = "none";

    // Mostrar el modal
    document.getElementById("scheduleEditModal").style.display = "flex";
    fetch("http://localhost:8080/api/horario/datos", {
        headers: { "Authorization": `Bearer ${token}` }
    })
    .then(response => response.json())
    .then(horarioData => {
        generarHorarioEditable(horarioData);
    })
    .catch(error => console.error("Error al cargar el horario:", error));
}

// Función para generar el horario editable con las asignaturas en los desplegables
function generarHorarioEditable(horarioData) {
    const cuerpoHorario = document.querySelector("#scheduleEditableTable tbody");
    cuerpoHorario.innerHTML = "";  // Limpiar el contenido anterior

    obtenerAsignaturas().then(asignaturas => {
        horarioData.forEach(item => {
            const fila = document.createElement("tr");

            // Columna Hora de Inicio
            const celdaHoraInicio = document.createElement("td");
            const selectHoraInicio = document.createElement("input");
            selectHoraInicio.type = "time";
            selectHoraInicio.value = item.horaInicio || "08:00";  
            celdaHoraInicio.appendChild(selectHoraInicio);
            fila.appendChild(celdaHoraInicio);

            // Columna Hora de Fin
            const celdaHoraFin = document.createElement("td");
            const selectHoraFin = document.createElement("input");
            selectHoraFin.type = "time";
            selectHoraFin.value = item.horaFin || "09:00";  
            celdaHoraFin.appendChild(selectHoraFin);
            fila.appendChild(celdaHoraFin);

            // Celdas de días de la semana (Lunes a Viernes)
            ["lunes", "martes", "miercoles", "jueves", "viernes"].forEach(dia => {
                const celdaDia = document.createElement("td");
                const selectAsignatura = document.createElement("select");

                // Opción "Libre" como predeterminada
                const opcionLibre = document.createElement("option");
                opcionLibre.value = "Libre";
                opcionLibre.textContent = "Libre";
                selectAsignatura.appendChild(opcionLibre);

                // Agregar asignaturas al desplegable
                asignaturas.forEach(asignatura => {
                    const opcion = document.createElement("option");
                    opcion.value = asignatura.nombre;
                    opcion.textContent = asignatura.nombre;

                    // Seleccionar la asignatura si coincide con el horario actual
                    if (item[dia] === asignatura.nombre) {
                        opcion.selected = true;
                    }

                    selectAsignatura.appendChild(opcion);
                });

                celdaDia.appendChild(selectAsignatura);
                fila.appendChild(celdaDia);
            });

            cuerpoHorario.appendChild(fila);
        });
    });
}


function obtenerAsignaturas() {
    return fetch("http://localhost:8080/api/horario/asignaturas", {  // Cambié la URL del endpoint
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al obtener las asignaturas");
        }
        return response.json();
    })
    .then(data => {
        console.log("Asignaturas obtenidas:", data);
        return data;
    })
    .catch(error => {
        console.error("Error al obtener las asignaturas:", error);
        return [];
    });
}

function guardarCambiosHorario() {
    const horarioEditado = [];
    const bloques = ['primeraHora', 'segundaHora', 'terceraHora', 'cuartaHora', 'quintaHora', 'sextaHora'];

    // Seleccionar todas las filas de la tabla editable
    const filas = document.querySelectorAll("#scheduleEditableTable tbody tr");
    filas.forEach((fila, index) => {
        const horaInicio = fila.cells[0].querySelector("input").value;
        const horaFin = fila.cells[1].querySelector("input").value;
        const lunes = fila.cells[2].querySelector("select").value;
        const martes = fila.cells[3].querySelector("select").value;
        const miercoles = fila.cells[4].querySelector("select").value;
        const jueves = fila.cells[5].querySelector("select").value;
        const viernes = fila.cells[6].querySelector("select").value;

        horarioEditado.push({
            bloque: bloques[index], // Asigna el nombre de bloque exacto
            horaInicio: horaInicio.length === 5 ? horaInicio + ":00" : horaInicio,
            horaFin: horaFin.length === 5 ? horaFin + ":00" : horaFin,
            lunes,
            martes,
            miercoles,
            jueves,
            viernes
        });
    });

    // Enviar el horario editado al backend
    fetch("http://localhost:8080/api/horario/actualizar", {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(horarioEditado)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al actualizar el horario");
        }
        return response.text();
    })
    .then(message => {
        console.log(message);
       // alert("Horario actualizado correctamente");
        cerrarModalHorario(); // Cerrar el modal después de guardar
        loadSchedule(); // Recargar el horario actualizado
    })
    .catch(error => console.error("Error al guardar el horario:", error));
}




// Función para cerrar el modal
function cerrarModalHorario() {
    document.getElementById("modifyScheduleBtn").style.display = "block"; // Mostrar el botón "Modificar Horario"
    document.getElementById("scheduleEditModal").style.display = "none"; // Ocultar el modal
}



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
        if (nuevaRutaFoto) {
            actualizarVistaFoto(`http://localhost:8080/${nuevaRutaFoto}`);
        }
    })
    .catch(error => {
        console.error("Error al sincronizar la foto:", error);
    });
}

/**
 * Actualiza la vista de la foto en el DOM.
 * @param {string} nuevaRutaFoto - Ruta de la nueva foto.
 */
function actualizarVistaFoto(nuevaRutaFoto) {
    const fotoUsuario = document.getElementById('fotoUsuario');
    if (fotoUsuario && nuevaRutaFoto) {
        fotoUsuario.src = `${nuevaRutaFoto}?t=${new Date().getTime()}`; // Marca de tiempo para evitar caché
    }
}