package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long>{

	Optional<EmpresaEntity> findByCnpj(String cnpj);

	Page<EmpresaEntity> findByNomeContainingIgnoreCaseOrCnpjContainingIgnoreCase(String nome, String cpnj,
			Pageable pagination);

}
