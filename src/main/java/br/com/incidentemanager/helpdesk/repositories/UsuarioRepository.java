package br.com.incidentemanager.helpdesk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long>{

	Optional<UsuarioEntity> findByPerfil(PerfilEnum admin);

	Optional<UsuarioEntity> findByEmail(String email);

	Optional<UsuarioEntity> findByIdAndEmpresa(Long id, EmpresaEntity empresa);

	Page<UsuarioEntity> findAllByEmpresa(Pageable pagination, EmpresaEntity empresa);

}
