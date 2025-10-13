package br.com.incidentemanager.helpdesk.dto.inputs;

import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpresaInput {

	@NotBlank(message = "O nome da empresa é obrigatório")
    @Size(min = 2, max = 100, message = "O nome da empresa deve ter entre 2 e 100 caracteres")
	private String nome;
	
	@NotBlank(message = "O CEP é obrigatório")
	@Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve estar no formato 00000-000")
	private String cep;
	
    @NotBlank(message = "A rua é obrigatória")
    @Size(max = 120, message = "A rua deve ter no máximo 120 caracteres")
	private String rua;
	
    @NotBlank(message = "O número é obrigatório")
    @Size(max = 10, message = "O número deve ter no máximo 10 caracteres")
	private String numero;
	
    @Size(max = 50, message = "O complemento deve ter no máximo 50 caracteres")
	private String complemento;
	
    @NotBlank(message = "O bairro é obrigatório")
    @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres")
	private String bairro;
	
    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
	private String cidade;
	
    @NotBlank(message = "O estado é obrigatório")
    @Size(min = 2, max = 2, message = "O estado deve conter a sigla (ex: SP, RJ, BA)")
	private String estado;
	
    @NotBlank(message = "O CNPJ é obrigatório")
    @CNPJ(message = "O CNPJ informado é inválido")
	private String cnpj;
	
	@NotNull(message = "O ativo é obrigatório")
	private boolean ativo = true;
}
