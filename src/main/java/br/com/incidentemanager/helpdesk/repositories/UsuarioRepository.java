package br.com.incidentemanager.helpdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	@Query(value = """
			    SELECT u.* FROM users u
			    LEFT JOIN chamados c
			        ON c.tecnico_responsavel_id = u.id
			        AND c.status = 'ABERTO'
			    WHERE u.perfil = 'TECNICO_TI'
			    AND u.ativo = true
			    AND u.empresa_id = :empresaId
			    GROUP BY u.id
			    ORDER BY COUNT(c.id) ASC
			    LIMIT 1
			""", nativeQuery = true)
	UsuarioEntity findTecnicoComMenosChamados(@Param("empresaId") Long empresaId);

	@Query("SELECT u FROM UsuarioEntity u WHERE " + "u.id <> :usuarioLogadoId "
			+ "AND (:empresa IS NULL OR u.empresa = :empresa) " + "AND (:ativo IS NULL OR u.ativo = :ativo) "
			+ "AND (:search IS NULL OR :search = '' OR "
			+ "    (LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "     LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))))")
	Page<UsuarioEntity> listarUsuariosComFiltros(@Param("empresa") EmpresaEntity empresa,
			@Param("search") String search, @Param("ativo") Boolean ativo,
			@Param("usuarioLogadoId") Long usuarioLogadoId, Pageable pagination);

	List<UsuarioEntity> findByEmpresa(EmpresaEntity empresa);

	@Query("""
			SELECT u FROM UsuarioEntity u
			WHERE u.empresa = :empresa
			AND u.perfil = :perfil
			AND u.ativo = true
			AND u.id != :idExceto
			AND (
			    :search IS NULL OR
			    LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
			)
			ORDER BY u.nome ASC
			""")
	Page<UsuarioEntity> findTecnicosParaTransferencia(Pageable pageable, @Param("empresa") EmpresaEntity empresa,
			@Param("perfil") PerfilEnum perfil, @Param("idExceto") Long idExceto, @Param("search") String search);

	@Query("""
			SELECT u FROM UsuarioEntity u
			WHERE u.perfil = 'TECNICO_TI'
			AND u.ativo = true
			AND (:empresaId IS NULL OR u.empresa.id = :empresaId)
			AND (
			    :search IS NULL OR
			    LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
			)
			ORDER BY u.nome ASC
			""")
	Page<UsuarioEntity> pesquisarTecnicos(@Param("search") String search, @Param("empresaId") Long empresaId,
			Pageable pageable);

}
