package br.com.incidentemanager.helpdesk.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.RedefinirSenhaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.RedefinirSenhaRepository;
import jakarta.transaction.Transactional;

@Service
public class RedefinirSenhaService {

	@Autowired
	private RedefinirSenhaRepository redefinirSenhaRepository;

	@Transactional
	public RedefinirSenhaEntity renovarTokenDoUsuario(UsuarioEntity usuarioEncontrado) {
		redefinirSenhaRepository.findByUsuarioAndUsedFalse(usuarioEncontrado)
				.ifPresent(redefinirSenhaRepository::delete);
		return criar(usuarioEncontrado);
	}

	@Transactional
	private RedefinirSenhaEntity criar(UsuarioEntity usuarioEncontrado) {
		RedefinirSenhaEntity redefinirSenhaEntity = new RedefinirSenhaEntity();
		String hash = UUID.randomUUID().toString();
		redefinirSenhaEntity.setUsuario(usuarioEncontrado);
		redefinirSenhaEntity.setExpirationTime(LocalDateTime.now().plusMinutes(15));
		redefinirSenhaEntity.setUsed(false);
		redefinirSenhaEntity.setHash(hash);
		return redefinirSenhaRepository.save(redefinirSenhaEntity);
	}

	public RedefinirSenhaEntity verificaHash(String hash) {
		RedefinirSenhaEntity redefinirSenhaEntity = redefinirSenhaRepository.findByHash(hash)
				.orElseThrow(() -> new NotFoundBusinessException("Token inválido"));
		if (redefinirSenhaEntity.isUsed()) {
		    throw new BadRequestBusinessException("Esse link já foi utilizado. Solicite um novo para redefinir sua senha.");
		}
		if (redefinirSenhaEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
			throw new BadRequestBusinessException("Link expirado, solicite uma nova redefinição de senha.");
		}
		
		return redefinirSenhaEntity;
	}

	public RedefinirSenhaEntity buscaPorHash(String hash) {
		return verificaHash(hash);
	}

	@Transactional
	public void definirTokenComoUsado(RedefinirSenhaEntity redefinirSenhaEntity) {
		redefinirSenhaEntity.setUsed(true);
		redefinirSenhaEntity.setUsedAt(LocalDateTime.now());
		redefinirSenhaRepository.save(redefinirSenhaEntity);
	}

}
