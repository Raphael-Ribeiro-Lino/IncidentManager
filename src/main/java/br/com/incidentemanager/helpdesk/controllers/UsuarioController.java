package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.UsuarioConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.AlteraMeusDadosInput;
import br.com.incidentemanager.helpdesk.dto.inputs.AlteraSenhaInput;
import br.com.incidentemanager.helpdesk.dto.inputs.AlteraUsuarioInput;
import br.com.incidentemanager.helpdesk.dto.inputs.EmailRedefinirSenhaInput;
import br.com.incidentemanager.helpdesk.dto.inputs.RedefinirSenhaInput;
import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.dto.outputs.UsuarioOutput;
import br.com.incidentemanager.helpdesk.entities.TokenAcaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.TipoTokenEnum;
import br.com.incidentemanager.helpdesk.handler.ProblemExceptionOutput;
import br.com.incidentemanager.helpdesk.services.TokenAcaoService;
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
	private TokenAcaoService tokenAcaoService;

	@Autowired
	private UsuarioConvert usuarioConvert;

	@PostMapping
	@PodeAcessarSe.TemPerfilAdmEmpresa
	@ResponseStatus(code = HttpStatus.CREATED)
	public UsuarioOutput cadastra(@RequestBody @Valid UsuarioInput usuarioInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		usuarioService.verificaSenhas(usuarioInput.getSenha(), usuarioInput.getRepetirSenha());
		UsuarioEntity usuarioEntity = usuarioConvert.inputToEntity(usuarioInput);
		return usuarioConvert.entityToOutput(usuarioService.cadastra(usuarioInput, usuarioEntity, usuarioLogado));
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
	public Page<UsuarioOutput> lista(
			@PageableDefault(size = 10, sort = "nome", direction = Direction.ASC) Pageable pagination) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<UsuarioEntity> usuarios = usuarioService.lista(pagination, usuarioLogado);
		return usuarioConvert.pageEntityToPageOutput(usuarios);
	}

	@PutMapping("/altera-meus-dados")
	@PodeAcessarSe.EstaAutenticado
	public UsuarioOutput alteraMeusDados(@RequestBody @Valid AlteraMeusDadosInput alteraMeusDadosInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		usuarioService.verificaEmailParaAlterar(usuarioLogado.getEmail(), alteraMeusDadosInput.getEmail());
		usuarioConvert.copyInputToEntity(usuarioLogado, alteraMeusDadosInput);
		return usuarioConvert.entityToOutput(usuarioService.altera(usuarioLogado));
	}

	@PutMapping("/{id}/altera-dados")
	@PodeAcessarSe.TemPerfilAdmEmpresa
	public UsuarioOutput alteraDados(@PathVariable Long id, @RequestBody @Valid AlteraUsuarioInput alteraUsuarioInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		UsuarioEntity usuarioEncontrado = usuarioService.buscaPorIdComMesmaEmpresa(id, usuarioLogado);
		usuarioConvert.copyInputToEntity(usuarioEncontrado, alteraUsuarioInput);
		return usuarioConvert.entityToOutput(usuarioService.altera(usuarioEncontrado));
	}

	@PutMapping("/altera-senha")
	@PodeAcessarSe.EstaAutenticado
	public void alteraSenha(@RequestBody @Valid AlteraSenhaInput alteraSenhaInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		usuarioService.alteraSenha(usuarioLogado, alteraSenhaInput.getSenhaAtual(), alteraSenhaInput.getNovaSenha(),
				alteraSenhaInput.getRepetirNovaSenha());
	}

	@PostMapping("/redefinir-senha")
	public ResponseEntity<ProblemExceptionOutput> enviaEmailRedefinirSenha(
			@RequestBody @Valid EmailRedefinirSenhaInput emailRedefinirSenhaInput) {
		UsuarioEntity usuarioEncontrado = usuarioService
				.buscaPorEmailRedefinirSenha(emailRedefinirSenhaInput.getEmail());
		usuarioService.enviaEmailRedefinirSenha(usuarioEncontrado);
		ProblemExceptionOutput resposta = new ProblemExceptionOutput(HttpStatus.OK.value(),
				"Se o e-mail estiver cadastrado, enviaremos um link de redefinição.");
		return ResponseEntity.ok(resposta);
	}

	@GetMapping("/redefinir-senha/{hash}")
	public void verificaHash(@PathVariable String hash) {
		tokenAcaoService.verificaHash(hash, TipoTokenEnum.REDEFINICAO_SENHA);
	}

	@PutMapping("/redefinir-senha/{hash}")
	public void redefinirSenha(@PathVariable String hash, @RequestBody @Valid RedefinirSenhaInput redefinirSenhaInput) {
		TokenAcaoEntity tokenAcaoEntity  = tokenAcaoService.verificaHash(hash, TipoTokenEnum.REDEFINICAO_SENHA);
		usuarioService.redefinirSenha(tokenAcaoEntity, redefinirSenhaInput.getSenha(),
				redefinirSenhaInput.getRepetirSenha());
		usuarioService.enviarEmailAvisoSenhaAlterada(tokenAcaoEntity);
	}

}
