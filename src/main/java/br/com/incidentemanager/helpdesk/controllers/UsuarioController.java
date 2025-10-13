package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.UsuarioConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.dto.outputs.UsuarioOutput;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.EmpresaService;
import br.com.incidentemanager.helpdesk.services.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/usuario")
@CrossOrigin(origins = { "http://localhost", "http://localhost:4200", "http://localhost:4200/*" })
public class UsuarioController {
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private EmpresaService empresaService;
	
	@Autowired
	private UsuarioConvert usuarioConvert;

	@PostMapping
	@PodeAcessarSe.TemPerfilAdmEmpresa
	@ResponseStatus(code = HttpStatus.CREATED)
	public UsuarioOutput cadastra(@RequestBody @Valid UsuarioInput usuarioInput) {
		usuarioService.verificaSenhas(usuarioInput.getSenha(), usuarioInput.getRepetirSenha());
		UsuarioEntity usuarioEntity = usuarioConvert.inputToEntity(usuarioInput);
		converteEmpresa(usuarioInput, usuarioEntity);
		return usuarioConvert.entityToOutput( usuarioService.cadastra(usuarioEntity));
	}

	private void converteEmpresa(@Valid UsuarioInput usuarioInput, UsuarioEntity usuarioEntity) {
		EmpresaEntity empresaEntity = empresaService.buscaPorId(usuarioInput.getEmpresa());
		usuarioEntity.setEmpresa(empresaEntity);
	}
}
