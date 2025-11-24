package br.com.incidentemanager.helpdesk.services;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.entities.TokenAcaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.enums.TipoTokenEnum;
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
	private TokenAcaoService tokenAcaoService;

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
		usuarioRepository.save(usuarioEntity);
		enviaEmailDefinirSenha(usuarioEntity);
		return usuarioEntity;
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

	public Page<UsuarioEntity> lista(Pageable pagination, UsuarioEntity usuarioLogado, String search) {
		EmpresaEntity empresaEntity = usuarioLogado.getPerfil().equals(PerfilEnum.ADMIN) ? null
				: usuarioLogado.getEmpresa();

		if (search != null && !search.isBlank()) {
			return usuarioRepository.findByEmpresaAndNomeOrEmailExcluindoLogado(empresaEntity, search,
					usuarioLogado.getId(), pagination);
		} else {
			return usuarioRepository.findAllByEmpresaOptionalExcluindoLogado(empresaEntity, usuarioLogado.getId(),
					pagination);
		}
	}

	@Transactional
	public UsuarioEntity altera(UsuarioEntity usuarioLogado) {
		return usuarioRepository.save(usuarioLogado);
	}

	public void verificaEmailParaAlterar(String emailEncontrado, String emailAlterado) {
		Optional<UsuarioEntity> usuarioEntity = usuarioRepository.findByEmail(emailAlterado);
		if (usuarioEntity.isPresent() && !usuarioEntity.get().getEmail().equals(emailEncontrado)) {
			throw new BadRequestBusinessException(
					"O endereço de e-mail já está registrado. Por favor, escolha um endereço de e-mail diferente.");
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
	public void definirSenha(TokenAcaoEntity tokenAcaoEntity, String senha, String repetirSenha) {
		verificaSenhas(senha, repetirSenha);
		defineSenhaESalvaUsuario(tokenAcaoEntity.getUsuario(), senha);
		tokenAcaoService.definirTokenComoUsado(tokenAcaoEntity);
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

	@Transactional
	public void enviaEmailRedefinirSenha(UsuarioEntity usuario) {
		TokenAcaoEntity tokenAcaoEntity = tokenAcaoService.renovarTokenDoUsuario(usuario,
				TipoTokenEnum.REDEFINICAO_SENHA);
		LayoutEmailEntity layout = layoutEmailService.buscaPorNome("Redefinir Senha");

		Map<String, String> placeholders = Map.of("{hash}", tokenAcaoEntity.getHash());

		enviaEmailComLayout(usuario.getEmail(), layout, placeholders);
	}

	@Transactional
	public void enviarEmailAvisoSenhaAlterada(TokenAcaoEntity tokenAcaoEntity) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		LayoutEmailEntity layout = layoutEmailService.buscaPorNome("Aviso de alteração de senha");

		Map<String, String> placeholders = Map.of("{dataHoraAlteracao}", tokenAcaoEntity.getUsedAt().format(formatter));

		enviaEmailComLayout(tokenAcaoEntity.getUsuario().getEmail(), layout, placeholders);
	}

	@Transactional
	public void enviaEmailDefinirSenha(UsuarioEntity usuarioEntity) {
		if (usuarioEntity.getSenha() != null && !usuarioEntity.getSenha().isBlank()) {
			throw new BadRequestBusinessException("Este usuário já possui uma senha definida.");
		}
		TokenAcaoEntity tokenAcaoEntity = tokenAcaoService.renovarTokenDoUsuario(usuarioEntity,
				TipoTokenEnum.CRIACAO_SENHA);
		LayoutEmailEntity layout = layoutEmailService.buscaPorNome("Criação de senha de acesso");
		Map<String, String> placeholders = Map.of("{nomeUsuario}", tokenAcaoEntity.getUsuario().getNome(), "{hash}",
				tokenAcaoEntity.getHash());
		enviaEmailComLayout(usuarioEntity.getEmail(), layout, placeholders);
	}

	@Transactional
	public void enviaEmailComLayout(String destinatario, LayoutEmailEntity layoutEmail,
			Map<String, String> placeholders) {
		String corpoFormatado = layoutEmail.getBody();
		if (placeholders != null) {
			for (Map.Entry<String, String> entry : placeholders.entrySet()) {
				corpoFormatado = corpoFormatado.replace(entry.getKey(), entry.getValue());
			}
		}

		emailService.enviaEmail(destinatario, layoutEmail.getName(), layoutEmail.getSourceEmail(),
				layoutEmail.getSubject(), corpoFormatado);
	}

	@Transactional
	public void verificaSeDefiniuSenha(UsuarioEntity usuarioEncontrado) {
		if (usuarioEncontrado.getSenha() == null || usuarioEncontrado.getSenha().isBlank()) {
			enviaEmailDefinirSenha(usuarioEncontrado);
			throw new BadRequestBusinessException(
					"Você ainda não definiu sua senha. Enviamos um novo e-mail com o link para criação.");
		}
	}

}
