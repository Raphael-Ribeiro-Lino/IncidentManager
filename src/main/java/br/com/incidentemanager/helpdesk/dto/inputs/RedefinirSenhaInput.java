package br.com.incidentemanager.helpdesk.dto.inputs;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedefinirSenhaInput {

	@NotBlank(message = "A senha é obrigatória")
	@Length(min = 8, max = 255, message = "A senha deve ter no mínimo 8 caracteres e no máximo 255 caracteres")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Senha inválida. A senha deve atender aos seguintes critérios: Ter pelo menos 8 caracteres, incluir pelo menos uma letra maiúscula ou minúscula, incluir pelo menos um número e incluir pelo menos um caractere especial entre @, $, !, %, *, ?, &.")
	private String senha;

	@NotBlank(message = "Repetir a senha é obrigatório")
	@Length(min = 8, max = 255, message = "A senha deve ter no mínimo 8 caracteres e no máximo 255 caracteres")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Senha inválida. A senha deve atender aos seguintes critérios: Ter pelo menos 8 caracteres, incluir pelo menos uma letra maiúscula ou minúscula, incluir pelo menos um número e incluir pelo menos um caractere especial entre @, $, !, %, *, ?, &.")
	private String repetirSenha;
}
