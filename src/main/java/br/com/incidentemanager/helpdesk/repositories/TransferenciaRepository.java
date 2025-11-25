package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.TransferenciaEntity;
import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;

public interface TransferenciaRepository extends JpaRepository<TransferenciaEntity, Long>{

	boolean existsByChamadoAndStatus(ChamadoEntity chamado, StatusTransferenciaEnum status);
}
