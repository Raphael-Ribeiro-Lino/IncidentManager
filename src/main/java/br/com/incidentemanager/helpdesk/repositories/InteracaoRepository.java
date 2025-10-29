package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.InteracaoEntity;

public interface InteracaoRepository extends JpaRepository<InteracaoEntity, Long>{

}
