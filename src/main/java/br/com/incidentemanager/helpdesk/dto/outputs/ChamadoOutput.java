package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChamadoOutput {

    private String protocolo;
    
    private String titulo;      
    
    private String descricao;     
    
    private PrioridadeEnum prioridade; 
    
    private StatusChamadoEnum status;  
    
    private String categoria;   
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private Instant dataCriacao;   
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private Instant dataUltimaAtualizacao; 

    private UsuarioOutput solicitante;      
    
    private UsuarioOutput tecnicoResponsavel;  
    
    private List<AnexoOutput> anexos;         
	
}
