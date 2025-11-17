package br.com.incidentemanager.helpdesk.dto.outputs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioOutput {

	private Long id;
	
	private String nome;
	
	private String email;
	
	private String telefone;
	
	private boolean ativo;
	
	private EmpresaOutput empresa;
}
