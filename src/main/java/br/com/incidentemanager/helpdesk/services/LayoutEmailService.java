package br.com.incidentemanager.helpdesk.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.LayoutEmailRepository;
import jakarta.transaction.Transactional;

@Service
public class LayoutEmailService {

	@Autowired
	private LayoutEmailRepository layoutEmailRepository;

	public boolean existeRedefinirSenha() {
		return existeLayout("Redefinir Senha");
	}

	@Transactional
	public void cadastra(LayoutEmailEntity layoutEmailEntity) {
		layoutEmailRepository.save(layoutEmailEntity);
	}

	public LayoutEmailEntity buscaPorNome(String name) {
		return layoutEmailRepository.findByName(name)
				.orElseThrow(() -> new NotFoundBusinessException("Layout " + name + " não encontrado"));
	}

	public boolean existeAvisoAlteracaoSenha() {
		return existeLayout("Aviso de alteração de senha");
	}

	private boolean existeLayout(String nome) {
		Optional<LayoutEmailEntity> layoutEmailEntity = layoutEmailRepository.findByName(nome);
		if (layoutEmailEntity.isPresent()) {
			return true;
		}
		return false;
	}

	public boolean existeCriacaoDeSenha() {
		return existeLayout("Criação de senha de acesso");
	}
}
