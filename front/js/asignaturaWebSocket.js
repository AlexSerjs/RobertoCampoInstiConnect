function conectarAsignaturaWebSocket(token, mensajeCallback) {
    const socketAsignaturas = new WebSocket('ws://localhost:8080/ws/asignaturas');
     
    socketAsignaturas.onopen = () => {
        console.log('WebSocket de asignaturas conectado.');
        socketAsignaturas.send(JSON.stringify({ tipo: 'SUSCRIPCION', token }));
    };

    socketAsignaturas.onmessage = (event) => {
        try {
            const mensaje = JSON.parse(event.data);
            if (typeof mensajeCallback === 'function') {
                mensajeCallback(mensaje);
            }
        } catch (error) {
            console.error('Error al procesar el mensaje del WebSocket:', event.data, error);
        }
    };

    socketAsignaturas.onerror = (error) => {
        console.error('Error en el WebSocket de asignaturas:', error);
    };

    socketAsignaturas.onclose = () => {
        console.warn('WebSocket de asignaturas cerrado.');
    };

    return socketAsignaturas;
}
