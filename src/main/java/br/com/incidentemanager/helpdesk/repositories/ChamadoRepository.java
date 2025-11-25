package br.com.incidentemanager.helpdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;

public interface ChamadoRepository extends JpaRepository<ChamadoEntity, Long> {

	Page<ChamadoEntity> findAllBySolicitante(Pageable pagination, UsuarioEntity solicitante);

	Optional<ChamadoEntity> findByIdAndSolicitante(Long id, UsuarioEntity solicitante);

	@Query("""
			SELECT c FROM ChamadoEntity c
			WHERE c.tecnicoResponsavel = :tecnico
			AND c.ativo = true
			AND c.status != :status
			AND (:prioridade IS NULL OR c.prioridade = :prioridade)
			AND (
			    :busca IS NULL OR
			    LOWER(c.titulo) LIKE LOWER(CONCAT('%', :busca, '%')) OR
			    LOWER(c.protocolo) LIKE LOWER(CONCAT('%', :busca, '%'))
			)
			ORDER BY
			    CASE c.prioridade
			        WHEN 'CRITICA' THEN 1
			        WHEN 'ALTA' THEN 2
			        WHEN 'MEDIA' THEN 3
			        WHEN 'BAIXA' THEN 4
			        ELSE 5
			    END,
			    c.dataCriacao ASC
			""")
	Page<ChamadoEntity> findAllByTecnicoResponsavelFiltrado(Pageable pagination,
			@Param("tecnico") UsuarioEntity tecnicoResponsavel, @Param("prioridade") PrioridadeEnum prioridade,
			@Param("status") StatusChamadoEnum status, @Param("busca") String busca);

	Optional<ChamadoEntity> findByIdAndTecnicoResponsavel(Long id, UsuarioEntity tecnicoResponsavel);

	List<ChamadoEntity> findBySolicitanteAndStatusNot(UsuarioEntity solicitante, StatusChamadoEnum status);

	@Query("""
			SELECT c FROM ChamadoEntity c
			WHERE c.solicitante = :solicitante
			AND (:status IS NULL OR c.status = :status)
			AND (
			    :searchTerm IS NULL OR
			    UPPER(c.titulo) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR
			    UPPER(c.descricao) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR
			    UPPER(c.protocolo) LIKE UPPER(CONCAT('%', :searchTerm, '%'))
			)
			""")
	Page<ChamadoEntity> findAllBySolicitanteFiltrado(Pageable pagination,
			@Param("solicitante") UsuarioEntity solicitante, @Param("searchTerm") String searchTerm,
			@Param("status") StatusChamadoEnum status);
}
