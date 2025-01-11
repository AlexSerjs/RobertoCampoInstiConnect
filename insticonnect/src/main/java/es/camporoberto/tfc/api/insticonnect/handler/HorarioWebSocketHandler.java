package es.camporoberto.tfc.api.insticonnect.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HorarioWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Nueva conexión WebSocket de horario establecida. Sesión: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Conexión WebSocket de horario cerrada. Sesión: " + session.getId());
    }

    public void notifyHorarioUpdate(String mensaje) {
        Map<String, String> payload = new HashMap<>();
        payload.put("tipo", "ACTUALIZACION_HORARIO");
        payload.put("mensaje", mensaje);

        try {
            String jsonMensaje = new ObjectMapper().writeValueAsString(payload);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMensaje));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            System.out.println("Mensaje recibido en WebSocket de horario: " + payload);

            // Puedes manejar mensajes entrantes aquí si es necesario
            Map<String, Object> mensajeRecibido = new ObjectMapper().readValue(payload, HashMap.class);

            // Enviar respuesta básica
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("tipo", "CONFIRMACION");
            respuesta.put("mensaje", "Mensaje recibido correctamente.");
            String jsonRespuesta = new ObjectMapper().writeValueAsString(respuesta);

            session.sendMessage(new TextMessage(jsonRespuesta));
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage("{\"error\":\"Error procesando el mensaje\"}"));
        }
    }
}
