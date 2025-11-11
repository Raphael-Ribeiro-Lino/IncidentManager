package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;

public interface ChamadoRepository extends JpaRepository<ChamadoEntity, Long>{

	Page<ChamadoEntity> findAllBySolicitante(Pageable pagination, UsuarioEntity solicitante);

}
