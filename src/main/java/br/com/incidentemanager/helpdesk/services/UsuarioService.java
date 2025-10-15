package br.com.incidentemanager.helpdesk.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.entities.RedefinirSenhaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.SafeResponseBusinessException;
import br.com.incidentemanager.helpdesk.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private EmpresaService empresaService;

	@Autowired
	private RedefinirSenhaService redefinirSenhaService;

	@Autowired
	private LayoutEmailService layoutEmailService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

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
		defineSenhaESalvaUsuario(usuarioEntity);
	}

	@Transactional
	public UsuarioEntity cadastra(@Valid UsuarioInput usuarioInput, UsuarioEntity usuarioEntity,
			UsuarioEntity usuarioLogado) {
		existeUsuario(usuarioEntity.getEmail());
		usuarioEntity = converteEmpresa(usuarioInput, usuarioEntity, usuarioLogado);
		return defineSenhaESalvaUsuario(usuarioEntity);
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

	public UsuarioEntity buscaPorEmail(String email) {
		return usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundBusinessException("Usuário não encontrado"));
	}

	public UsuarioEntity buscaPorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new NotFoundBusinessException("Usuário " + id + " não encontrado"));
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
		if (usuarioEntity.isPresent() && !usuarioEntity.get().getEmail().equals(emailEncontrado)) {
			throw new BadRequestBusinessException("Email já cadastrado");
		}
	}

	@Transactional
	public void alteraSenha(UsuarioEntity usuarioLogado, String senhaAtual, String novaSenha, String repetirNovaSenha) {
		if (!passwordEncoder.matches(senhaAtual, usuarioLogado.getSenha())) {
			throw new BadRequestBusinessException("Senha atual incorreta");
		}
		if (senhaAtual.equals(novaSenha)) {
			throw new BadRequestBusinessException("A nova senha não pode ser igual à senha atual");
		}
		verificaSenhas(novaSenha, repetirNovaSenha);
		defineSenhaESalvaUsuario(usuarioLogado, novaSenha);
	}

	public UsuarioEntity buscaPorEmailRedefinirSenha(String email) {
		Optional<UsuarioEntity> usuarioOptional = usuarioRepository.findByEmail(email);

		if (usuarioOptional.isEmpty()) {
			log.warn("Tentativa de redefinir senha com e-mail não cadastrado: {}", email);
			throw new SafeResponseBusinessException(
		            "Se o e-mail estiver cadastrado, enviaremos um link de redefinição");
		}

		UsuarioEntity usuarioEncontrado = usuarioOptional.get();

		if (!usuarioEncontrado.isAtivo()) {
			log.warn("Tentativa de redefinir senha para usuário inativo: {}", email);
			throw new SafeResponseBusinessException(
		            "Se o e-mail estiver cadastrado, enviaremos um link de redefinição");
		}

		return usuarioEncontrado;
	}

	@Transactional
	public void enviaEmailRedefinirSenha(UsuarioEntity usuarioEncontrado) {
		RedefinirSenhaEntity redefinirSenhaEntity = redefinirSenhaService.renovarTokenDoUsuario(usuarioEncontrado);
		LayoutEmailEntity layoutEmailEntity = layoutEmailService.buscaPorNome("Redefinir Senha");
		String emailBody = redefineBody(layoutEmailEntity.getBody(), redefinirSenhaEntity);
		emailService.enviaEmail(usuarioEncontrado.getEmail(), layoutEmailEntity.getName(),
				layoutEmailEntity.getSourceEmail(), layoutEmailEntity.getSubject(), emailBody);
	}

	private String redefineBody(String body, RedefinirSenhaEntity redefinirSenhaEntity) {
		return body.replace("{HASH}", redefinirSenhaEntity.getHash());
	}

	@Transactional
	public void redefinirSenha(RedefinirSenhaEntity redefinirSenhaEntity, String senha, String repetirSenha) {
		verificaSenhas(senha, repetirSenha);
		defineSenhaESalvaUsuario(redefinirSenhaEntity.getUsuario(), senha);
		redefinirSenhaService.definirTokenComoUsado(redefinirSenhaEntity);
	}

	@Transactional
	private UsuarioEntity defineSenhaESalvaUsuario(UsuarioEntity usuarioEntity) {
		usuarioEntity.setSenha(passwordEncoder.encode(usuarioEntity.getSenha()));
		return usuarioRepository.save(usuarioEntity);
	}

	@Transactional
	private void defineSenhaESalvaUsuario(UsuarioEntity usuarioEntity, String novaSenha) {
		usuarioEntity.setSenha(passwordEncoder.encode(novaSenha));
		usuarioRepository.save(usuarioEntity);
	}

	public void verificaSenhas(String senha, String repetirSenha) {
		if (!senha.equals(repetirSenha)) {
			throw new BadRequestBusinessException("As senhas informadas não coincidem");
		}
	}

}
