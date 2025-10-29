package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;

public interface ChamadoRepository extends JpaRepository<ChamadoEntity, Long>{

}
