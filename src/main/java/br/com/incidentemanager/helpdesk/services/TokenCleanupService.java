package br.com.incidentemanager.helpdesk.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.repositories.TokensInvalidadosRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenCleanupService {

	@Autowired
	private TokensInvalidadosRepository tokensInvalidadosRepository;

	@Scheduled(cron = "0 0 0 * * *") 
	public void limparTokensExpirados() {
		log.info("Iniciando limpeza de tokens expirados...");
		
		LocalDateTime agora = LocalDateTime.now();
		tokensInvalidadosRepository.deleteByExpiracaoBefore(agora);
		
		log.info("Limpeza de tokens expirados finalizada.");
	}
}