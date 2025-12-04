package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.incidentemanager.helpdesk.entities.NotificacaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;

public interface NotificacaoRepository extends JpaRepository<NotificacaoEntity, Long> {

	@Query("""
			SELECT n FROM NotificacaoEntity n
			WHERE n.destinatario = :destinatario
			AND (:lido IS NULL OR n.lido = :lido)
			""")
	Page<NotificacaoEntity> findAllByDestinatarioFiltrado(Pageable pageable,
			@Param("destinatario") UsuarioEntity destinatario, @Param("lido") Boolean lido);

	long countByDestinatarioAndLidoFalse(UsuarioEntity destinatario);

	@Modifying
	@Query("UPDATE NotificacaoEntity n SET n.lido = true WHERE n.destinatario = :destinatario AND n.lido = false")
	void marcarTodasComoLidas(@Param("destinatario") UsuarioEntity destinatario);

}
