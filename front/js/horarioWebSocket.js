function conectarHorarioWebSocket(token, mensajeCallback) {
    const socketHorario = new WebSocket('ws://localhost:8080/ws/horario');

    socketHorario.onopen = () => {
        console.log('WebSocket de horario conectado.');
        // Enviar un mensaje inicial si es necesario
        socketHorario.send(JSON.stringify({ tipo: 'SUSCRIPCION', token }));
    };

    socketHorario.onmessage = (event) => {
        try {
            const mensaje = JSON.parse(event.data);
            if (typeof mensajeCallback === 'function') {
                mensajeCallback(mensaje);
            } else {
                switch (mensaje.tipo) {
                    case 'ACTUALIZACION_HORARIO':
                        console.log('Horario actualizado:', mensaje.mensaje);
                        cargarHorarioAlumno(token); // Recargar horario
                        break;
                    case 'CONFIRMACION':
                        console.log('Mensaje de confirmación recibido:', mensaje.mensaje);
                        break;
                    case 'ELIMINAR_ASIGNATURA': // Manejar el mensaje de eliminar asignatura
                        console.log('Se eliminó una asignatura:', mensaje.mensaje);
                        cargarAsignaturasDelGrupo(token); // Actualiza las asignaturas en la interfaz
                        break;
                    case 'NUEVA_ASIGNATURA': // Manejar el mensaje de nueva asignatura
                        console.log('Nueva asignatura añadida:', mensaje.mensaje);
                        cargarAsignaturasDelGrupo(token); // Refresca la lista de asignaturas
                        break;
                    default:
                        console.warn('Tipo de mensaje no reconocido:', mensaje.tipo);
                }
            }
        } catch (error) {
            console.error('Error al procesar el mensaje del WebSocket:', event.data, error);
        }
    };

    socketHorario.onerror = (error) => {
        console.error('Error en el WebSocket:', error);
    };

    socketHorario.onclose = () => {
        console.warn('WebSocket de horario cerrado.');
    };

    return socketHorario;
}
