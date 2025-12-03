package br.com.incidentemanager.helpdesk.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.ChatMensagemConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.ChatMensagemInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChatMensagemOutput;
import br.com.incidentemanager.helpdesk.entities.ChatMensagemEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.ChatMensagemService;
import br.com.incidentemanager.helpdesk.services.TokenService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/chamado/{id}/chat")
public class ChatMensagemController {

	@Autowired
	private ChatMensagemService chatMensagemService;

	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private ChatMensagemConvert chatMensagemConvert;
	
	@PostMapping(consumes = { "multipart/form-data" })
	@PodeAcessarSe.EstaAutenticado
	public ResponseEntity<ChatMensagemOutput> enviarMensagem(
			@PathVariable Long id,
			@ModelAttribute @Valid ChatMensagemInput mensagemInput) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		ChatMensagemEntity mensagemEntity = chatMensagemConvert.inputToEntity(mensagemInput);
		ChatMensagemEntity mensagemSalva = chatMensagemService.enviar(mensagemEntity, mensagemInput, id, usuarioLogado);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(chatMensagemConvert.entityToOutput(mensagemSalva, usuarioLogado));
	}
	
	@GetMapping
	@PodeAcessarSe.EstaAutenticado
	public ResponseEntity<List<ChatMensagemOutput>> listarMensagens(@PathVariable Long id) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		
		List<ChatMensagemEntity> mensagens = chatMensagemService.listarMensagens(id, usuarioLogado);
		
		List<ChatMensagemOutput> output = mensagens.stream()
				.map(msg -> chatMensagemConvert.entityToOutput(msg, usuarioLogado))
				.toList();
				
		return ResponseEntity.ok(output);
	}

}
