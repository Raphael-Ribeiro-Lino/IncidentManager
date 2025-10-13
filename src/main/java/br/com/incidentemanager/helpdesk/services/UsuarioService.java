package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private EmpresaService empresaService;

	public boolean existeAdm() {
		if (usuarioRepository.findByPerfil(PerfilEnum.ADMIN).isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	private void existeUsuario(String email) {
		if (usuarioRepository.findByEmail(email).isPresent()) {
			throw new BadRequestBusinessException(
					"O endereço de e-mail já está registrado. Por favor, escolha um endereço de e-mail diferente.");
		}
	}

	@Transactional
	public void cadastraAdm(UsuarioEntity usuarioEntity) {
		usuarioEntity.setSenha(new BCryptPasswordEncoder().encode(usuarioEntity.getSenha()));
		usuarioRepository.save(usuarioEntity);
	}

	@Transactional
	public UsuarioEntity cadastra(@Valid UsuarioInput usuarioInput, UsuarioEntity usuarioEntity,
			UsuarioEntity usuarioLogado) {
		existeUsuario(usuarioEntity.getEmail());
		usuarioEntity = converteEmpresa(usuarioInput, usuarioEntity, usuarioLogado);
		usuarioEntity.setSenha(new BCryptPasswordEncoder().encode(usuarioEntity.getSenha()));
		return usuarioRepository.save(usuarioEntity);
	}

	private UsuarioEntity converteEmpresa(@Valid UsuarioInput usuarioInput, UsuarioEntity usuarioEntity,
			UsuarioEntity usuarioLogado) {
		if (usuarioLogado.getPerfil().equals(PerfilEnum.ADMIN)) {
			usuarioEntity.setEmpresa(empresaService.buscaPorId(usuarioInput.getEmpresa()));
			return usuarioEntity;
		}
		usuarioEntity.setEmpresa(usuarioLogado.getEmpresa());
		return usuarioEntity;
	}

	public UsuarioEntity buscaPorEmail(String name) {
		return usuarioRepository.findByEmail(name)
				.orElseThrow(() -> new NotFoundBusinessException("Usuário não encontrado"));
	}

	public UsuarioEntity buscaPorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new NotFoundBusinessException("Usuário " + id + " não encontrado"));
	}

	public void verificaSenhas(String senha, String repetirSenha) {
		if (!senha.equals(repetirSenha)) {
			throw new BadRequestBusinessException("Senha e confirmação de senha devem ser iguais.");
		}
	}

	public UsuarioEntity buscaPorIdComMesmaEmpresa(Long id, UsuarioEntity usuarioLogado) {
		if (usuarioLogado.getPerfil().equals(PerfilEnum.ADMIN)) {
			return usuarioRepository.findById(id)
					.orElseThrow(() -> new NotFoundBusinessException("Usuário " + id + " não encontrado"));
		}
		return usuarioRepository.findByIdAndEmpresa(id, usuarioLogado.getEmpresa())
				.orElseThrow(() -> new NotFoundBusinessException("Usuário " + id + " não encontrado"));
	}

	public Page<UsuarioEntity> lista(Pageable pagination, UsuarioEntity usuarioLogado) {
		if (usuarioLogado.getPerfil().equals(PerfilEnum.ADMIN)) {
			return usuarioRepository.findAll(pagination);
		}
		return usuarioRepository.findAllByEmpresa(pagination, usuarioLogado.getEmpresa());
	}

}
