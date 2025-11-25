package br.com.incidentemanager.helpdesk.dto.inputs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponderTransferenciaInput {
	
	@NotNull(message = "A decisão de aceite é obrigatória")
	private Boolean aceito;
	
	private String motivoRecusa;

}
