package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.TokenAcaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.TipoTokenEnum;

public interface TokenAcaoRepository extends JpaRepository<TokenAcaoEntity, Long>{

	Optional<TokenAcaoEntity> findByHashAndTipo(String hash, TipoTokenEnum tipo);

	Optional<TokenAcaoEntity> findByUsuarioAndUsedFalseAndTipo(UsuarioEntity usuario, TipoTokenEnum tipo);

}
