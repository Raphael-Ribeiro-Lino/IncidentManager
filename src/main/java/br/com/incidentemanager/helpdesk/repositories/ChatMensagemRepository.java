package br.com.incidentemanager.helpdesk.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.incidentemanager.helpdesk.entities.ChatMensagemEntity;

public interface ChatMensagemRepository extends JpaRepository<ChatMensagemEntity, Long> {

	List<ChatMensagemEntity> findByChamadoIdOrderByEnviadoEmAsc(Long chamadoId);

	@Modifying
	@Query("UPDATE ChatMensagemEntity m SET m.lidoEm = CURRENT_TIMESTAMP WHERE m.chamado.id = :chamadoId AND m.destinatario.id = :usuarioId AND m.lidoEm IS NULL")
	void marcarTodasComoLidas(@Param("chamadoId") Long chamadoId, @Param("usuarioId") Long usuarioId);

}