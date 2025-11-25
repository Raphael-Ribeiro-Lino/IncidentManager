package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.TransferenciaConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.ResponderTransferenciaInput;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.TokenService;
import br.com.incidentemanager.helpdesk.services.TransferenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/transferencia")
public class TransferenciaController {
	
	@Autowired
	private TransferenciaService transferenciaService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private TransferenciaConvert transferenciaConvert;
	
	@PostMapping("/{id}/responder")
	@PodeAcessarSe.TemPerfilTecnicoTi
	public void responder(@PathVariable Long id, 
			@RequestBody @Valid ResponderTransferenciaInput responderTransferenciaInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		transferenciaService.responder(id, responderTransferenciaInput, usuarioLogado);
	}

}
