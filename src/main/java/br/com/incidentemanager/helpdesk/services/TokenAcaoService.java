package br.com.incidentemanager.helpdesk.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.TokenAcaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.TipoTokenEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.TokenAcaoRepository;
import jakarta.transaction.Transactional;

@Service
public class TokenAcaoService {

	@Autowired
	private TokenAcaoRepository tokenAcaoRepository;

	public TokenAcaoEntity verificaHash(String hash, TipoTokenEnum tipo) {
		TokenAcaoEntity tokenAcaoEntity = tokenAcaoRepository.findByHashAndTipo(hash, tipo)
				.orElseThrow(() -> new NotFoundBusinessException("Token inválido"));

		if (tokenAcaoEntity.isUsed()) {
			throw new BadRequestBusinessException("Esse link já foi utilizado. Solicite um novo.");
		}
		if (tokenAcaoEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
			throw new BadRequestBusinessException("Link expirado. Solicite uma nova redefinição.");
		}
		return tokenAcaoEntity;
	}

	@Transactional
	public void definirTokenComoUsado(TokenAcaoEntity tokenAcaoEntity) {
		tokenAcaoEntity.setUsed(true);
		tokenAcaoEntity.setUsedAt(LocalDateTime.now());
		tokenAcaoRepository.save(tokenAcaoEntity);
	}

	@Transactional
	public TokenAcaoEntity renovarTokenDoUsuario(UsuarioEntity usuario, TipoTokenEnum tipo) {
		tokenAcaoRepository.findByUsuarioAndUsedFalseAndTipo(usuario, tipo).ifPresent(tokenAcaoRepository::delete);
		return criar(usuario, tipo);
	}

	@Transactional
	private TokenAcaoEntity criar(UsuarioEntity usuario, TipoTokenEnum tipo) {
		TokenAcaoEntity tokenAcaoEntity = new TokenAcaoEntity();
		tokenAcaoEntity.setHash(UUID.randomUUID().toString());
		tokenAcaoEntity.setUsuario(usuario);
		tokenAcaoEntity.setExpirationTime(LocalDateTime.now().plusMinutes(15));
		tokenAcaoEntity.setUsed(false);
		tokenAcaoEntity.setTipo(tipo);
		return tokenAcaoRepository.save(tokenAcaoEntity);
	}
}
