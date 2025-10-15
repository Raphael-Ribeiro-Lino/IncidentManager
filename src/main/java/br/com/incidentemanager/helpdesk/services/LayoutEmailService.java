package br.com.incidentemanager.helpdesk.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.repositories.LayoutEmailRepository;
import jakarta.transaction.Transactional;

@Service
public class LayoutEmailService {

	@Autowired
	private LayoutEmailRepository layoutEmailRepository;

	public boolean existeRedefinirSenha() {
		Optional<LayoutEmailEntity> layoutEmailEntity = layoutEmailRepository.findByName("Redefinir Senha");
		if(layoutEmailEntity.isPresent()) {
			return true;
		}
		return false;
	}

	@Transactional
	public void cadastra(LayoutEmailEntity layoutEmailEntity) {
		layoutEmailRepository.save(layoutEmailEntity);
	}
}
