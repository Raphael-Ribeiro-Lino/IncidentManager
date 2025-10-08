package br.com.incidentemanager.helpdesk.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenOutput {

	private String token;
	
	private String tipo;
}
