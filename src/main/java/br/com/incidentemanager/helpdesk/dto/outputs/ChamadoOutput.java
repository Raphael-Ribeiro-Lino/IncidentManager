package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;
import java.util.List;

import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChamadoOutput {

	private Long id;
	
    private String protocolo;
    
    private String titulo;      
    
    private String descricao;     
    
    private PrioridadeEnum prioridade; 
    
    private StatusChamadoEnum status;  
    
    private Instant dataCriacao;   
    
    private Instant dataUltimaAtualizacao; 

    private UsuarioOutput solicitante;      
    
    private UsuarioOutput tecnicoResponsavel;  
    
    private List<AnexoOutput> anexos;         
	
}
