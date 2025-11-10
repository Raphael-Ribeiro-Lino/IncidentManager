package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.ChamadoConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoOutput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.ChamadoService;
import br.com.incidentemanager.helpdesk.services.TokenService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/chamado")
@CrossOrigin(origins = { "http://localhost", "http://localhost:4200", "http://localhost:4200/*" })
public class ChamadoController {

	@Autowired
	private ChamadoService chamadoService;
	
	@Autowired
	private ChamadoConvert chamadoConvert;
	
	@Autowired
	private TokenService tokenService;
	
	@PostMapping(consumes = {"multipart/form-data"})
	@PodeAcessarSe.EstaAutenticado
	public ChamadoOutput criar(@Valid @ModelAttribute ChamadoInput chamadoInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoConvert.inputToEntity(chamadoInput);
		ChamadoEntity chamadoCriado = chamadoService.criar(chamadoEntity, chamadoInput, usuarioLogado);
		return chamadoConvert.entityToOutput(chamadoCriado);
		
	}
	
	
}
