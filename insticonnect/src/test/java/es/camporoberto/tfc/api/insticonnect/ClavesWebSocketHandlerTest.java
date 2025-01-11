package es.camporoberto.tfc.api.insticonnect;

import es.camporoberto.tfc.api.insticonnect.websocket.ClavesWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ClavesWebSocketHandlerTest {

    private ClavesWebSocketHandler handler;

    @Mock
    private WebSocketSession sesion;

    @BeforeEach
    public void configurar() {
        MockitoAnnotations.openMocks(this);
        handler = new ClavesWebSocketHandler();
    }

    @Test
    public void despuesDeConexionEstablecida() throws Exception {
        when(sesion.getAttributes()).thenReturn(Collections.singletonMap("username", "testuser"));
        handler.afterConnectionEstablished(sesion);
        verify(sesion, times(1)).getAttributes();
    }

    @Test
    public void manejarMensajeDeTexto() throws Exception {
        TextMessage mensaje = new TextMessage("Mensaje de prueba");
        handler.handleTextMessage(sesion, mensaje);
    }

    @Test
    public void despuesDeConexionCerrada() throws Exception {
        handler.afterConnectionEstablished(sesion);
        handler.afterConnectionClosed(sesion, null);
        assertEquals(0, handler.sessions.size());
    }

    @Test
    public void notificarClientes() throws Exception {
        handler.afterConnectionEstablished(sesion);
        when(sesion.isOpen()).thenReturn(true);
        handler.notifyClients("Hola clientes");
        verify(sesion, times(1)).sendMessage(new TextMessage("Hola clientes"));
    }
}
