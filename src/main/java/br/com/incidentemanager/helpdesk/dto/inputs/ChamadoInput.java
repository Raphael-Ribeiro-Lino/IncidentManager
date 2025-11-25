package br.com.incidentemanager.helpdesk.dto.inputs;

import java.util.ArrayList;
import java.util.List;

import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChamadoInput {

	@NotBlank(message = "O título do chamado é obrigatório.")
    @Size(min = 5, max = 100, message = "O título deve conter entre {min} e {max} caracteres.")
    private String titulo;
	
    @NotBlank(message = "A descrição do chamado é obrigatória.")
    @Size(min = 10, max = 2000, message = "A descrição deve conter entre {min} e {max} caracteres.")
    private String descricao;

    @NotNull(message = "O nível de prioridade é obrigatório.")
    private PrioridadeEnum prioridade;
    
    @Valid
    private List<AnexoInput> anexos= new ArrayList<>();
}
