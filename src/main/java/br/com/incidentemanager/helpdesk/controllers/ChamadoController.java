package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.ChamadoConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.AlteraStatusChamadoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.AvaliacaoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.ReabrirChamadoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.SolicitarTransferenciaInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoDetalhadoOutput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoOutput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.services.ChamadoService;
import br.com.incidentemanager.helpdesk.services.TokenService;
import br.com.incidentemanager.helpdesk.services.TransferenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/chamado")
public class ChamadoController {

	@Autowired
	private ChamadoService chamadoService;

	@Autowired
	private ChamadoConvert chamadoConvert;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private TransferenciaService transferenciaService;

	@PostMapping(consumes = { "multipart/form-data" })
	@PodeAcessarSe.EstaAutenticado
	public ChamadoOutput criar(@ModelAttribute @Valid ChamadoInput chamadoInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoConvert.inputToEntity(chamadoInput);
		ChamadoEntity chamadoCriado = chamadoService.criar(chamadoEntity, chamadoInput, usuarioLogado);
		return chamadoConvert.entityToOutput(chamadoCriado);
	}

	@GetMapping("/lista")
	@PodeAcessarSe.EstaAutenticado
	public Page<ChamadoOutput> lista(@RequestParam(required = false) String search,
			@RequestParam(required = false) StatusChamadoEnum status,
			@PageableDefault(size = 10, sort = "dataUltimaAtualizacao", direction = Direction.DESC) Pageable pagination) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<ChamadoEntity> chamados = chamadoService.lista(pagination, usuarioLogado, search, status);
		return chamadoConvert.pageEntityToPageOutput(chamados);
	}

	@GetMapping("/{id}")
	@PodeAcessarSe.EstaAutenticado
	public ChamadoOutput buscaPorId(@PathVariable Long id) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoService.buscaPorId(id, usuarioLogado);
		chamadoService.atualizaStoragePathComLinkTemporario(chamadoEntity);
		return chamadoConvert.entityToOutput(chamadoEntity);
	}

	@PutMapping("/{id}")
	@PodeAcessarSe.EstaAutenticado
	public ChamadoOutput alterarMeuChamado(@ModelAttribute @Valid ChamadoInput chamadoInput, @PathVariable Long id) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoService.buscaPorId(id, usuarioLogado);
		chamadoService.verificaSeStatusDoChamadoEstaAberto(chamadoEntity.getStatus());
		return chamadoConvert.entityToOutput(chamadoService.alterarMeuChamado(chamadoEntity, chamadoInput));
	}

	@GetMapping("/tecnico")
	@PodeAcessarSe.TemPerfilTecnicoTi
	public Page<ChamadoOutput> listaMeusAtentimentos(@PageableDefault(size = 10) Pageable pagination,
			@RequestParam(required = false) PrioridadeEnum prioridade, @RequestParam(required = false) String busca) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<ChamadoEntity> chamados = chamadoService.listaMeusAtentimentos(pagination, usuarioLogado, prioridade,
				busca);
		return chamadoConvert.pageEntityToPageOutput(chamados);
	}

	@GetMapping("/{id}/tecnico")
	public ChamadoDetalhadoOutput buscaAtendimentoPorId(@PathVariable Long id) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoService.buscaAtendimentoPorId(id, usuarioLogado);
		chamadoService.atualizaStoragePathComLinkTemporario(chamadoEntity);
		return chamadoConvert.entityToDetalhadoOutput(chamadoEntity);
	}

	@PatchMapping("/{id}/status")
	@PodeAcessarSe.TemPerfilTecnicoTi
	public ChamadoOutput atualizarStatus(@PathVariable Long id,
			@Valid @RequestBody AlteraStatusChamadoInput alteraStatusChamadoInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoService.buscaAtendimentoPorId(id, usuarioLogado);
		chamadoService.verificaSeChamadoFoiConcluido(chamadoEntity);
		ChamadoEntity atualizado = chamadoService.atualizarStatus(chamadoEntity, alteraStatusChamadoInput,
				usuarioLogado);
		return chamadoConvert.entityToOutput(atualizado);
	}

	@PostMapping("/{id}/solicitar-transferencia")
	@PodeAcessarSe.TemPerfilTecnicoTi
	@ResponseStatus(HttpStatus.CREATED)
	public void solicitarTransferencia(@PathVariable Long id, @RequestBody @Valid SolicitarTransferenciaInput input) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		transferenciaService.solicitar(id, input, usuarioLogado);
	}
	
	@PostMapping("/{id}/avaliar")
	@PodeAcessarSe.EstaAutenticado
	@ResponseStatus(HttpStatus.CREATED)
	public ChamadoOutput avaliarChamado(@PathVariable Long id, @RequestBody @Valid AvaliacaoInput avaliacaoInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoService.buscaPorId(id, usuarioLogado);
		ChamadoEntity chamadoAvaliado = chamadoService.avaliarEFechar(chamadoEntity, avaliacaoInput);
		return chamadoConvert.entityToOutput(chamadoAvaliado);
	}

	
	@PostMapping("/{id}/reabrir")
	@PodeAcessarSe.EstaAutenticado
	@ResponseStatus(HttpStatus.CREATED)
	public ChamadoOutput reabrirChamado(
			@PathVariable Long id, 
			@RequestBody @Valid ReabrirChamadoInput reabrirChamadoInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChamadoEntity chamadoEntity = chamadoService.buscaPorId(id, usuarioLogado);
		ChamadoEntity chamadoReaberto = chamadoService.reabrir(id, reabrirChamadoInput, usuarioLogado, chamadoEntity);
		return chamadoConvert.entityToOutput(chamadoReaberto);
	}
}
