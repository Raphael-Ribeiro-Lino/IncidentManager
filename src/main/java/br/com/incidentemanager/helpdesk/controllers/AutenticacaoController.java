package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.dto.inputs.LoginInput;
import br.com.incidentemanager.helpdesk.dto.outputs.TokenOutput;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.services.TokenService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/auth")
@CrossOrigin(origins = {"http://localhost", "http://localhost:4200" })
public class AutenticacaoController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private TokenService tokenService;
	
	@PostMapping
	public TokenOutput autenticar(@RequestBody @Valid LoginInput login) {
		UsernamePasswordAuthenticationToken dadosLogin = login.converter();
		
		try {
			Authentication authentication = (Authentication) authenticationManager.authenticate(dadosLogin);
			String token = tokenService.criaToken(authentication);
			return new TokenOutput(token, "Bearer");
			
		}catch (AuthenticationException e) {
			throw new BadRequestBusinessException("E-mail ou Senha Inválida");
		}
	}

}
