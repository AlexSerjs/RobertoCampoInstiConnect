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
public class NotificacionWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public void notifyClients(String tipo, String mensaje) {
        Map<String, String> payload = new HashMap<>();
        payload.put("tipo", tipo);
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
            System.out.println("Mensaje recibido: " + payload);

            // Parsear el JSON recibido
            Map<String, Object> mensajeRecibido = new ObjectMapper().readValue(payload, HashMap.class);

            // Responder en función del tipo de mensaje
            if ("AUTENTICACION".equals(mensajeRecibido.get("tipo"))) {
                System.out.println("Autenticación recibida: " + mensajeRecibido.get("token"));
                // Puedes validar el token si es necesario
            } else {
                System.out.println("Tipo de mensaje no reconocido");
            }

            // Simulación de una respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("tipo", "ACTUALIZACION_PUBLICACIONES");
            respuesta.put("mensaje", "Nuevo comentario agregado");
            String jsonRespuesta = new ObjectMapper().writeValueAsString(respuesta);

            session.sendMessage(new TextMessage(jsonRespuesta));
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage("{\"error\":\"Error procesando el mensaje\"}"));
        }
    }


}
