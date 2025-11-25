package br.com.incidentemanager.helpdesk.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.EmpresaRepository;
import br.com.incidentemanager.helpdesk.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class EmpresaService {

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ChamadoRepository chamadoRepository;

	@Transactional
	public EmpresaEntity cadastra(EmpresaEntity empresaEntity) {
		existeCnpj(empresaEntity.getCnpj());
		return empresaRepository.save(empresaEntity);
	}

	private void existeCnpj(String cnpj) {
		if (empresaRepository.findByCnpj(cnpj).isPresent()) {
			throw new BadRequestBusinessException("Este CNPJ já está em uso por outra empresa.");
		}
	}

	public EmpresaEntity buscaPorId(Long idEmpresa) {
		return empresaRepository.findById(idEmpresa)
				.orElseThrow(() -> new NotFoundBusinessException("Empresa " + idEmpresa + " não encontrada"));
	}

	public Page<EmpresaEntity> listarComFiltros(String search, Boolean ativo, Pageable pagination) {
		return empresaRepository.buscarComFiltros(search, ativo, pagination);
	}

	@Transactional
	public EmpresaEntity alterar(EmpresaEntity empresaEntity) {
		if (!empresaEntity.isAtivo()) {
			realizarInativacaoEmCascata(empresaEntity);
		}
		return empresaRepository.save(empresaEntity);
	}

	@Transactional
	private void realizarInativacaoEmCascata(EmpresaEntity empresa) {
		List<UsuarioEntity> usuariosDaEmpresa = usuarioRepository.findByEmpresa(empresa);
		for (UsuarioEntity usuario : usuariosDaEmpresa) {
			if (usuario.isAtivo()) {
				usuario.setAtivo(false);
			}
			List<ChamadoEntity> chamadosDoUsuario = chamadoRepository.findBySolicitanteAndStatusNot(usuario,
					StatusChamadoEnum.CONCLUIDO);
			for (ChamadoEntity chamado : chamadosDoUsuario) {
				chamado.setAtivo(false);
			}
			chamadoRepository.saveAll(chamadosDoUsuario);
		}
		usuarioRepository.saveAll(usuariosDaEmpresa);
	}
}
