package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Data;

import java.util.List;

@Data
public class EncuestaPublicacionDTO extends BasePublicacionDTO {
    private String pregunta;
    private List<String> opciones;


    public static class OpcionDTO {
        private int id;
        private String texto;
        private int votos;

        public OpcionDTO(int id, String texto, int votos) {
            this.id = id;
            this.texto = texto;
            this.votos = votos;
        }
    }
}

