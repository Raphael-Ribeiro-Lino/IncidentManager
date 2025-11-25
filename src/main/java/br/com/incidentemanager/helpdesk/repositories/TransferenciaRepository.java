package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.TransferenciaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;

public interface TransferenciaRepository extends JpaRepository<TransferenciaEntity, Long> {

	boolean existsByChamadoAndStatus(ChamadoEntity chamado, StatusTransferenciaEnum status);

	@Query("""
			SELECT t FROM TransferenciaEntity t
			WHERE t.tecnicoDestino = :tecnicoDestino
			AND t.status = :status
			AND (
			    :search IS NULL OR
			    LOWER(t.chamado.titulo) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(t.chamado.protocolo) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(t.tecnicoOrigem.nome) LIKE LOWER(CONCAT('%', :search, '%'))
			)
			""")
	Page<TransferenciaEntity> findMinhasPendenciasFiltradas(@Param("tecnicoDestino") UsuarioEntity tecnicoDestino,
			@Param("status") StatusTransferenciaEnum status, @Param("search") String search, Pageable pageable);
}
