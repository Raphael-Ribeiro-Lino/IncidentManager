package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.NotificacaoConvert;
import br.com.incidentemanager.helpdesk.dto.outputs.NotificacaoOutput;
import br.com.incidentemanager.helpdesk.entities.NotificacaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.NotificacaoService;
import br.com.incidentemanager.helpdesk.services.TokenService;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/notificacao")
public class NotificacaoController {

	@Autowired
	private NotificacaoService notificacaoService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private NotificacaoConvert notificacaoConvert;

	@GetMapping("/lista")
	@PodeAcessarSe.EstaAutenticado
	public ResponseEntity<Page<NotificacaoOutput>> lista(@RequestParam(required = false) Boolean lido,
			@PageableDefault(size = 10, sort = "criadoEm", direction = Direction.DESC) Pageable pagination) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<NotificacaoEntity> notificacoes = notificacaoService.lista(pagination, usuarioLogado, lido);
		return ResponseEntity.ok(notificacaoConvert.pageEntityToPageOutput(notificacoes));
	}

	@GetMapping("/nao-lidas/count")
	@PodeAcessarSe.EstaAutenticado
	public ResponseEntity<Long> contarNaoLidas() {
		UsuarioEntity usuario = tokenService.buscaUsuario();
		return ResponseEntity.ok(notificacaoService.contarNaoLidas(usuario));
	}

	@PatchMapping("/{id}/ler")
	@PodeAcessarSe.EstaAutenticado
	public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
		UsuarioEntity usuario = tokenService.buscaUsuario();
		notificacaoService.marcarComoLida(id, usuario);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/ler-todas")
	@PodeAcessarSe.EstaAutenticado
	public ResponseEntity<Void> marcarTodasComoLidas() {
		UsuarioEntity usuario = tokenService.buscaUsuario();
		notificacaoService.marcarTodasComoLidas(usuario);
		return ResponseEntity.noContent().build();
	}
}
