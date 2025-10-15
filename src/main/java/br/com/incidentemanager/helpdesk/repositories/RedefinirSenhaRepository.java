package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.RedefinirSenhaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;

public interface RedefinirSenhaRepository extends JpaRepository<RedefinirSenhaEntity, Long>{

	Optional<RedefinirSenhaEntity> findByUsuarioAndUsedFalse(UsuarioEntity usuario);

	Optional<RedefinirSenhaEntity> findByHash(String hash);

}
