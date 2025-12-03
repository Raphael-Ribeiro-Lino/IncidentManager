package br.com.incidentemanager.helpdesk.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.ChatMensagemEntity;

public interface ChatMensagemRepository extends JpaRepository<ChatMensagemEntity, Long>{

	List<ChatMensagemEntity> findByChamadoIdOrderByEnviadoEmAsc(Long chamadoId);

}
