package br.com.incidentemanager.helpdesk.dto.inputs;

import org.hibernate.validator.constraints.Length;

import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioInput {

	@NotBlank(message = "O nome é obrigatório")
	@Length(max = 100, message = "O nome deve ter no máximo 100 caracteres")
	@Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?:[ -][A-Za-zÀ-ÖØ-öø-ÿ]+)*$", message = "Nome inválido. O nome deve atender aos seguintes critérios: Começar com uma ou mais letras maiúsculas ou minúsculas ou letras acentuadas comuns (à, é, í, etc.). Pode incluir um espaço ou hífen seguido por mais letras maiúsculas, minúsculas ou letras acentuadas comuns. O nome não pode conter números ou outros caracteres especiais.")
	private String nome;

	@NotBlank(message = "O e-mail é obrigatório")
	@Length(max = 320, message = "O e-mail deve ter no máximo 320 caracteres")
	@Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", message = "Endereço de e-mail inválido. O endereço de e-mail deve atender aos seguintes critérios: Deve começar com um ou mais caracteres alfanuméricos, traços ou sublinhados. Deve conter o símbolo @. O domínio do e-mail deve começar com um caractere alfanumérico ou sublinhado e pode incluir pontos seguidos por mais caracteres alfanuméricos ou sublinhados. O domínio do e-mail deve ter pelo menos duas letras após o último ponto.")
	private String email;

	@NotBlank(message = "O telefone é obrigatório")
	@Size(min = 10, max = 15, message = "O telefone deve ter entre 10 e 15 caracteres")
	@Pattern(regexp = "^\\+?\\d{0,3}?\\s?\\(?\\d{2,3}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Telefone inválido. Use um formato válido, como (11) 98765-4321 ou +55 11 98765-4321")
	private String telefone;

	@NotBlank(message = "A senha é obrigatória")
	@Length(min = 8, max = 255, message = "A senha deve ter no mínimo 8 caracteres e no máximo 255 caracteres")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Senha inválida. A senha deve atender aos seguintes critérios: Ter pelo menos 8 caracteres, incluir pelo menos uma letra maiúscula ou minúscula, incluir pelo menos um número e incluir pelo menos um caractere especial entre @, $, !, %, *, ?, &.")
	private String password;

	@NotBlank(message = "Repetir a senha é obrigatório")
	@Length(min = 8, max = 255, message = "A senha deve ter no mínimo 8 caracteres e no máximo 255 caracteres")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Senha inválida. A senha deve atender aos seguintes critérios: Ter pelo menos 8 caracteres, incluir pelo menos uma letra maiúscula ou minúscula, incluir pelo menos um número e incluir pelo menos um caractere especial entre @, $, !, %, *, ?, &.")
	private String repeatPassword;
	
	@NotNull(message = "O ativo é obrigatório")
	private boolean ativo = true;
	
	@NotNull(message = "O perfil é obrigatório")
	private PerfilEnum perfil;
	
	@NotNull(message = "A empresa é obrigatória")
	private Long empresaId;
}
