package br.com.incidentemanager.helpdesk.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import br.com.incidentemanager.helpdesk.entities.TokensInvalidadosEntity;
import jakarta.transaction.Transactional;

public interface TokensInvalidadosRepository extends JpaRepository<TokensInvalidadosEntity, Long> {

	boolean existsByTokenHash(String tokenHash);

	@Transactional
	@Modifying
	void deleteByExpiracaoBefore(LocalDateTime dataAtual);
}
