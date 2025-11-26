package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InteracaoOutput {
	
    private Long id;
    
    private String descricao;      
    
    private Instant dataHora;
    
    private String tipo;            
    
    private boolean visivelCliente;
    
    private String autorNome;
    
    private String autorPerfil;
	
}
