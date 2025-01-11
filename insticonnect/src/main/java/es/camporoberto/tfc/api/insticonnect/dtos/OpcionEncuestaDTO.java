package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.OpcionEncuesta;
import lombok.Data;

@Data
public class OpcionEncuestaDTO {
    private int id; // Este campo debe mapear el ID de la opción de encuesta
    private String texto;
    private int votos;

    public OpcionEncuestaDTO(OpcionEncuesta opcionEncuesta) {
        this.id = opcionEncuesta.getId(); // Extraer correctamente el ID desde la entidad
        this.texto = opcionEncuesta.getTexto();
        this.votos = opcionEncuesta.getVotos();
    }
}
