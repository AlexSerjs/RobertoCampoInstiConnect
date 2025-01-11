package es.camporoberto.tfc.api.insticonnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Habilita el uso de tareas programadas (por ejemplo, eliminar delegados no verificados)
public class InsticonnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsticonnectApplication.class, args);
	}
}
