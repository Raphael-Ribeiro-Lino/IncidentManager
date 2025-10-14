package br.com.incidentemanager.helpdesk.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
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
		EmpresaEntity empresaEntity = usuarioLogado.getPerfil().equals(PerfilEnum.ADMIN) ? null
				: usuarioLogado.getEmpresa();
		return usuarioRepository.findByIdAndEmpresaOptional(id, empresaEntity)
				.orElseThrow(() -> new NotFoundBusinessException("Usuário " + id + " não encontrado"));
	}

	public Page<UsuarioEntity> lista(Pageable pagination, UsuarioEntity usuarioLogado) {
		EmpresaEntity empresaEntity = usuarioLogado.getPerfil().equals(PerfilEnum.ADMIN) ? null
				: usuarioLogado.getEmpresa();
		return usuarioRepository.findAllByEmpresaOptional(empresaEntity, pagination);
	}

	@Transactional
	public UsuarioEntity altera(UsuarioEntity usuarioLogado) {
		return usuarioRepository.save(usuarioLogado);
	}

	public void verificaEmailParaAlterar(String emailEncontrado, String emailAlterado) {
		Optional<UsuarioEntity> usuarioEntity = usuarioRepository.findByEmail(emailAlterado);
		if(usuarioEntity.isPresent() && !usuarioEntity.get().getEmail().equals(emailEncontrado)) {
			throw new BadRequestBusinessException("Email já cadastrado");
		}
	}

	@Transactional
	public void alteraSenha(UsuarioEntity usuarioLogado, String senhaAtual, String novaSenha, String repetirNovaSenha) {
		BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
		
		if (!bcryptPasswordEncoder.matches(senhaAtual, usuarioLogado.getSenha())) {
	        throw new BadRequestBusinessException("Senha atual incorreta");
	    }
		if (senhaAtual.equals(novaSenha)) {
	        throw new BadRequestBusinessException("A nova senha não pode ser igual à senha atual");
	    }
		if (!novaSenha.equals(repetirNovaSenha)) {
	        throw new BadRequestBusinessException("As senhas informadas não coincidem");
	    }
		usuarioLogado.setSenha(bcryptPasswordEncoder.encode(novaSenha));
		usuarioRepository.save(usuarioLogado);
	}

}
