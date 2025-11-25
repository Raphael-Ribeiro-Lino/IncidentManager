package br.com.incidentemanager.helpdesk.dto.inputs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitarTransferenciaInput {

    @NotNull(message = "O técnico de destino é obrigatório")
    private Long tecnicoDestinoId;

    @NotBlank(message = "O motivo da transferência é obrigatório")
    private String motivo;
}
