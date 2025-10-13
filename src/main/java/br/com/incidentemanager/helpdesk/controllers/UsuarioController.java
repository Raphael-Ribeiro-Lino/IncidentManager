package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.TokenService;
import br.com.incidentemanager.helpdesk.services.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/usuario")
@CrossOrigin(origins = { "http://localhost", "http://localhost:4200", "http://localhost:4200/*" })
public class UsuarioController {
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private UsuarioConvert usuarioConvert;

	@PostMapping
	@PodeAcessarSe.TemPerfilAdmEmpresa
	@ResponseStatus(code = HttpStatus.CREATED)
	public UsuarioOutput cadastra(@RequestBody @Valid UsuarioInput usuarioInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		usuarioService.verificaSenhas(usuarioInput.getSenha(), usuarioInput.getRepetirSenha());
		UsuarioEntity usuarioEntity = usuarioConvert.inputToEntity(usuarioInput);
		return usuarioConvert.entityToOutput( usuarioService.cadastra(usuarioInput, usuarioEntity, usuarioLogado));
	}
	
	@GetMapping("/{id}")
	@PodeAcessarSe.TemPerfilAdmEmpresa
	public UsuarioOutput buscaPorId(@PathVariable Long id) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		UsuarioEntity usuarioEntity = usuarioService.buscaPorIdComMesmaEmpresa(id, usuarioLogado);
		return usuarioConvert.entityToOutput(usuarioEntity);
	}
	
	@GetMapping
	@PodeAcessarSe.EstaAutenticado
	public UsuarioOutput buscaUsuarioLogado() {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		return usuarioConvert.entityToOutput(usuarioLogado);
	}
	
	@GetMapping("/lista")
	@PodeAcessarSe.TemPerfilAdmEmpresa
	public Page<UsuarioOutput> lista(@PageableDefault(size = 10, sort = "nome", direction = Direction.ASC) Pageable pagination){
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<UsuarioEntity> usuarios = usuarioService.lista(pagination, usuarioLogado);
		return usuarioConvert.pageEntityToPageOutput(usuarios);
	}


}
