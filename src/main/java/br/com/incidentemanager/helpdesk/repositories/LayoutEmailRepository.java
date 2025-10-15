package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;

public interface LayoutEmailRepository extends JpaRepository<LayoutEmailEntity, Long>{

	Optional<LayoutEmailEntity> findByName(String name);

}
