package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

	Optional<UsuarioEntity> findByPerfil(PerfilEnum admin);

	Optional<UsuarioEntity> findByEmail(String email);

	@Query("SELECT u FROM UsuarioEntity u WHERE u.id = :id AND (:empresa IS NULL OR u.empresa = :empresa)")
	Optional<UsuarioEntity> findByIdAndEmpresaOptional(Long id, EmpresaEntity empresa);

	@Query("SELECT u FROM UsuarioEntity u WHERE (:empresa IS NULL OR u.empresa = :empresa)")
	Page<UsuarioEntity> findAllByEmpresaOptional(EmpresaEntity empresa, Pageable pagination);

}
