package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;

import br.com.incidentemanager.helpdesk.enums.TipoAnexoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnexoOutput {

    private String nomeArquivo;
    
    private String storagePath;
    
    private TipoAnexoEnum tipo;
    
    private Instant uploadedAt;
    
    private UsuarioOutput enviadoPor;
}
