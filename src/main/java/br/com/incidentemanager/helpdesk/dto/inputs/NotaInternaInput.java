package br.com.incidentemanager.helpdesk.dto.inputs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotaInternaInput {

    @NotBlank(message = "O texto da nota é obrigatório")
    @Size(max = 2000, message = "A nota deve ter no máximo 2000 caracteres")
    private String texto;

}
