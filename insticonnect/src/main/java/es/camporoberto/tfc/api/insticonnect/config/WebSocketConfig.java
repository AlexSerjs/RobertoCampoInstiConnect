package es.camporoberto.tfc.api.insticonnect.config;

import es.camporoberto.tfc.api.insticonnect.handler.*;
import es.camporoberto.tfc.api.insticonnect.websocket.ClavesWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public MyWebSocketHandler myWebSocketHandler() {
        return new MyWebSocketHandler();
    }

    @Bean
    public SimpleWebSocketHandler simpleWebSocketHandler() {
        return new SimpleWebSocketHandler();
    }

    @Bean
    public ClavesWebSocketHandler clavesWebSocketHandler() {
        return new ClavesWebSocketHandler();
    }

    @Bean
    public NotificacionWebSocketHandler notificacionWebSocketHandler() {
        return new NotificacionWebSocketHandler();
    }

    @Bean
    public DatosAlumnoWebSocketHandler datosAlumnoWebSocketHandler() {
        return new DatosAlumnoWebSocketHandler();
    }

    @Bean
    public HorarioWebSocketHandler horarioWebSocketHandler() {
        return new HorarioWebSocketHandler();
    }

    @Bean
    public AsignaturasWebSocketHandler asignaturasWebSocketHandler() {
        return new AsignaturasWebSocketHandler();
    }

    @Bean
    public PermisoWebSocketHandler permisoWebSocketHandler() {
        return new PermisoWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler(), "/ws-endpoint")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(simpleWebSocketHandler(), "/ws/test")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(clavesWebSocketHandler(), "/ws/claves")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(notificacionWebSocketHandler(), "/ws/notificaciones")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(datosAlumnoWebSocketHandler(), "/ws/datos-alumno")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(horarioWebSocketHandler(), "/ws/horario")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(asignaturasWebSocketHandler(), "/ws/asignaturas")
                .setAllowedOrigins("http://127.0.0.1:5500");

        registry.addHandler(permisoWebSocketHandler(), "/ws/permisos")
                .setAllowedOrigins("*");
    }
}
