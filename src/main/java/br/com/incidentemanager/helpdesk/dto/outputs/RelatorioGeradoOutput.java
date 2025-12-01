package br.com.incidentemanager.helpdesk.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RelatorioGeradoOutput {

    private String nomeArquivo;
    
    private byte[] dados;
    
    private String contentType;
}
