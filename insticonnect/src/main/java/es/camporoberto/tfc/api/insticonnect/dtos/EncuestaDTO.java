package es.camporoberto.tfc.api.insticonnect.dtos;

import com.sun.istack.NotNull;
import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.Size;


@Data
public class EncuestaDTO {
    @NotNull
    private String pregunta;

    @NotNull
    @Size(min = 2)
    private List<String> opciones;
}
