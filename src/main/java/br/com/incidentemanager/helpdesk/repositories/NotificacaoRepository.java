package br.com.incidentemanager.helpdesk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.NotificacaoEntity;

public interface NotificacaoRepository extends JpaRepository<NotificacaoEntity, Long>{

}
