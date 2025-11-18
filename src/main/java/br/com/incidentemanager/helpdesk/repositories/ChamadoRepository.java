package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;

public interface ChamadoRepository extends JpaRepository<ChamadoEntity, Long> {

	@Query("""
			SELECT c FROM ChamadoEntity c
			WHERE c.solicitante = :solicitante
			AND (
			    UPPER(c.titulo) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR
			    UPPER(c.descricao) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR
			    UPPER(c.protocolo) LIKE UPPER(CONCAT('%', :searchTerm, '%'))
			)
			ORDER BY c.dataUltimaAtualizacao ASC
			""")
	Page<ChamadoEntity> findAllBySolicitanteAndSearchTerm(Pageable pagination, UsuarioEntity solicitante,
			String searchTerm);

	Page<ChamadoEntity> findAllBySolicitante(Pageable pagination, UsuarioEntity solicitante);

	Optional<ChamadoEntity> findByIdAndSolicitante(Long id, UsuarioEntity solicitante);

	@Query("""
			    SELECT c FROM ChamadoEntity c
			    WHERE c.tecnicoResponsavel = :tecnicoResponsavel
			    ORDER BY
			        CASE c.prioridade
			            WHEN 'CRITICA' THEN 1
			            WHEN 'ALTA' THEN 2
			            WHEN 'MEDIA' THEN 3
			            WHEN 'BAIXA' THEN 4
			        END
			""")
	Page<ChamadoEntity> findAllByTecnicoResponsavel(Pageable pagination, UsuarioEntity tecnicoResponsavel);

	Optional<ChamadoEntity> findByIdAndTecnicoResponsavel(Long id, UsuarioEntity tecnicoResponsavel);

}
