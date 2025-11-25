package br.com.incidentemanager.helpdesk.dto.outputs;

import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferenciaOutput {

	private Long id;
	
	private ChamadoOutput chamado;
	
	private String motivo;
	
	private String dataSolicitacao;
	
	private StatusTransferenciaEnum status;
	
}
