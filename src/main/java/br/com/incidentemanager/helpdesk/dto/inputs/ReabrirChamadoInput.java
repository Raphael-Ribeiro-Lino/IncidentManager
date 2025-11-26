package br.com.incidentemanager.helpdesk.dto.inputs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReabrirChamadoInput {

	@NotBlank(message = "O motivo da reabertura é obrigatório")
	@Size(max = 1000, message = "O motivo deve ter no máximo 1000 caracteres")
	private String motivo;
}
