package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.TransferenciaConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.ResponderTransferenciaInput;
import br.com.incidentemanager.helpdesk.dto.outputs.TransferenciaOutput;
import br.com.incidentemanager.helpdesk.entities.TransferenciaEntity;
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

	@GetMapping("/minhas-pendencias")
	@PodeAcessarSe.TemPerfilTecnicoTi
	public Page<TransferenciaOutput> listarMinhasPendencias(@RequestParam(required = false) String search,
			@PageableDefault(size = 10, sort = "dataSolicitacao", direction = Direction.DESC) Pageable pagination) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<TransferenciaEntity> pendencias = transferenciaService.listarMinhasPendencias(usuarioLogado, search,
				pagination);
		return transferenciaConvert.pageEntityToPageOutput(pendencias);
	}

}
