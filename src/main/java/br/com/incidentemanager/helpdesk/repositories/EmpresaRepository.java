package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long>{

	Optional<EmpresaEntity> findByCnpj(String cnpj);

}
