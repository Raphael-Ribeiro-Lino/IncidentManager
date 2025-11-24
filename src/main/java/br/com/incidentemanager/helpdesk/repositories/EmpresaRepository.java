package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long> {

	Optional<EmpresaEntity> findByCnpj(String cnpj);

	@Query("SELECT e FROM EmpresaEntity e WHERE " + "(:ativo IS NULL OR e.ativo = :ativo) AND "
			+ "(:search IS NULL OR :search = '' OR LOWER(e.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR e.cnpj LIKE CONCAT('%', :search, '%'))")
	Page<EmpresaEntity> buscarComFiltros(@Param("search") String search, @Param("ativo") Boolean ativo,
			Pageable pagination);

}
