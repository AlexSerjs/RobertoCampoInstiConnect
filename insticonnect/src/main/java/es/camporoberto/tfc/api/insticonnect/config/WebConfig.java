package es.camporoberto.tfc.api.insticonnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
               // .allowedOrigins("http://127.0.0.1:5500")
                .allowedOriginPatterns("http://127.0.0.1:5500", "http://192.168.0.*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                //.allowedMethods("GET", "POST", "PUT", "DELETE")// Permitir todos los encabezados
                .allowCredentials(true);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar acceso a la carpeta uploads como recursos estáticos
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/Users/alexs/OneDrive/Desktop/robertoCampoTFC/insticonnect/uploads/")
                .setCachePeriod(3600);
    }

}
