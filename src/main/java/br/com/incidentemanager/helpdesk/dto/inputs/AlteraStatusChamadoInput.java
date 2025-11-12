package br.com.incidentemanager.helpdesk.dto.inputs;

import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlteraStatusChamadoInput {

    @NotNull(message = "O status é obrigatório.")
    private StatusChamadoEnum status;

    @NotBlank(message = "A observação é obrigatória.")
    private String observacao;
    
    @NotNull(message = "O visível para o cliente é obrigatório.")
    private boolean visivelCliente;
}
