package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class BasePublicacionDTO {
    private int id;
    private String usuario;
    private Date fechaCreacion;
    private String tipoContenido;

}
