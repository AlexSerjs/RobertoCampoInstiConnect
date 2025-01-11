//perimosWebSocket.js
function conectarPermisoWebSocket(token, mensajeCallback) {
    const socketPermiso = new WebSocket('ws://localhost:8080/ws/permisos');

    socketPermiso.onopen = () => {
        console.log('WebSocket de permisos conectado.');
        socketPermiso.send(JSON.stringify({ tipo: 'SUSCRIPCION', token }));
    };

    socketPermiso.onmessage = (event) => {
        try {
            const mensaje = JSON.parse(event.data);
            if (typeof mensajeCallback === 'function') {
                mensajeCallback(mensaje);
            }
        } catch (error) {
            console.error('Error al procesar el mensaje del WebSocket:', event.data, error);
        }
    };

    socketPermiso.onerror = (error) => {
        console.error('Error en el WebSocket de permisos:', error);
    };

    socketPermiso.onclose = () => {
        console.warn('WebSocket de permisos cerrado.');
    };

    return socketPermiso;
}
