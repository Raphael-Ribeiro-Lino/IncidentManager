package br.com.incidentemanager.helpdesk.dto.outputs;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChamadoDetalhadoOutput extends ChamadoOutput{
	private List<InteracaoOutput> historicoEventos;
}
