package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Data;

@Data
public class ClaveDetalleDTO {

    private Integer id; // ID de la clave
    private String clave; // La clave en sí
    private String comunidadNombre; // Nombre de la comunidad autónoma
    private String institutoNombre; // Nombre del instituto
    private String administradorNombre; // Nombre del administrador que generó la clave
    private Boolean estado; // TRUE si está usada, FALSE si no
    private String fechaCreacion; // Fecha y hora de creación
    private String fechaUtilizacion; // Fecha y hora de utilización (puede ser null)
}
