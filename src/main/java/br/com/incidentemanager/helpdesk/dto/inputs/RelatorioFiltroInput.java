package br.com.incidentemanager.helpdesk.dto.inputs;

import java.time.LocalDate;

import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelatorioFiltroInput {

	private LocalDate dataInicio;
	
	private LocalDate dataFim;

	private StatusChamadoEnum status;
	
	private PrioridadeEnum prioridade;

	private Long tecnicoId;
	
	private Long empresaId;
}
