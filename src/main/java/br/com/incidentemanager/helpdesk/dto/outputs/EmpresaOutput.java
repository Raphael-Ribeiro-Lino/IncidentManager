package br.com.incidentemanager.helpdesk.dto.outputs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpresaOutput {
	
	private Long id;
	
	private String nome;
	
	private String cnpj;
	
	private String cep;
	
	private String rua;
	
	private String numero;
	
	private String complemento;
	
	private String bairro;
	
	private String cidade;
	
	private String estado;

	private boolean ativo;
}
