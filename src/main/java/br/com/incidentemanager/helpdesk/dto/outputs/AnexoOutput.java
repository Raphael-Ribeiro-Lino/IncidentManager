package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.incidentemanager.helpdesk.enums.TipoAnexoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnexoOutput {

    private String nomeArquivo;
    
    private String storagePath;
    
    private TipoAnexoEnum tipo;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private Instant uploadedAt;
    
    private UsuarioOutput enviadoPor;
}
