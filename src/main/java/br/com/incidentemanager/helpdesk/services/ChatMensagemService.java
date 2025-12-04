package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.incidentemanager.helpdesk.dto.inputs.AnexoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.ChatMensagemInput;
import br.com.incidentemanager.helpdesk.entities.AnexoEntity;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.ChatMensagemEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.enums.TipoAnexoEnum;
import br.com.incidentemanager.helpdesk.enums.TipoMensagemEnum;
import br.com.incidentemanager.helpdesk.enums.TipoNotificacaoEnum; // Import Novo
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.UnauthorizedAccessBusinessException;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.ChatMensagemRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class ChatMensagemService {

	@Autowired
	private ChatMensagemRepository chatMensagemRepository;

	@Autowired
	private AnexoService anexoService;

	@Autowired
	private ChamadoRepository chamadoRepository;

	@Autowired
	private NotificacaoService notificacaoService; // <--- INJEÇÃO DA NOTIFICAÇÃO

	private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("pdf", "doc", "docx", "png", "jpg", "jpeg", "zip");

	@Transactional
	public ChatMensagemEntity enviar(ChatMensagemEntity mensagemEntity, @Valid ChatMensagemInput mensagemInput,
			Long chamadoId, UsuarioEntity usuarioLogado) {

		ChamadoEntity chamado = chamadoRepository.findById(chamadoId)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado não encontrado"));

		// 1. Validações
		validarPermissaoChat(chamado, usuarioLogado);
		validarStatusChamado(chamado);
		validarConteudo(mensagemInput);

		// 2. Prepara a Entidade
		mensagemEntity.setChamado(chamado);
		mensagemEntity.setRemetente(usuarioLogado);

		if (Boolean.TRUE.equals(mensagemInput.getPrivado()) && !usuarioLogado.getPerfil().equals(PerfilEnum.USUARIO)) {
			mensagemEntity.setVisivelParaCliente(false);
		} else {
			mensagemEntity.setVisivelParaCliente(true);
		}

		definirDestinatario(mensagemEntity, chamado, usuarioLogado);

		mensagemEntity.setTipo(detectarTipoMensagem(mensagemInput.getArquivos()));

		if (mensagemEntity.getConteudo() == null)
			mensagemEntity.setConteudo("");

		// 3. Salva Parcial
		ChatMensagemEntity mensagemSalva = chatMensagemRepository.saveAndFlush(mensagemEntity);

		// 4. Processa Anexos
		defineNovosAnexos(mensagemSalva, mensagemInput, usuarioLogado);

		// 5. Atualiza Chamado
		chamado.setDataUltimaAtualizacao(Instant.now());
		chamadoRepository.save(chamado);

		// 6. NOTIFICA O DESTINATÁRIO (NOVO)
		notificarDestinatario(mensagemSalva, chamado, usuarioLogado);

		return chatMensagemRepository.save(mensagemSalva);
	}

	// ... (listarMensagens, defineNovosAnexos, identificarTipoAnexo... MANTÉM
	// IGUAL)

	// --- LÓGICA DE NOTIFICAÇÃO ---

	private void notificarDestinatario(ChatMensagemEntity mensagem, ChamadoEntity chamado, UsuarioEntity remetente) {
		UsuarioEntity destinatario = mensagem.getDestinatario();

		// Se não tem destinatário (ex: admin comentando em chamado sem técnico), não
		// notifica
		if (destinatario == null)
			return;

		// SEGURANÇA: Se a mensagem é privada (visivelParaCliente = false),
		// e o destinatário é um CLIENTE, NÃO enviamos notificação.
		if (Boolean.FALSE.equals(mensagem.getVisivelParaCliente())
				&& destinatario.getPerfil().equals(PerfilEnum.USUARIO)) {
			return;
		}

		// Monta o texto do alerta
		String titulo = "Nova mensagem: " + chamado.getProtocolo();
		String corpo = montarTextoNotificacao(mensagem, remetente);

		notificacaoService.criar(destinatario, titulo, corpo, chamado, TipoNotificacaoEnum.NOVA_MENSAGEM);
	}

	private String montarTextoNotificacao(ChatMensagemEntity mensagem, UsuarioEntity remetente) {
		String base = remetente.getNome() + ": ";

		if (mensagem.getConteudo() != null && !mensagem.getConteudo().isBlank()) {
			// Se tem texto, usa o texto (truncado se for longo)
			String texto = mensagem.getConteudo();
			if (texto.length() > 50)
				texto = texto.substring(0, 47) + "...";
			return base + texto;
		} else {
			// Se não tem texto, deve ter anexo
			return base + "Enviou um anexo 📎";
		}
	}

	// ... (Restante dos métodos auxiliares: detectarTipoMensagem, validarExtensao,
	// etc... MANTÉM IGUAL)

	// Apenas para garantir que o contexto está completo, aqui estão os métodos que
	// já existiam:
	@Transactional
	public List<ChatMensagemEntity> listarMensagens(Long chamadoId, UsuarioEntity usuarioLogado) {
		ChamadoEntity chamado = chamadoRepository.findById(chamadoId)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado não encontrado"));

		validarPermissaoChat(chamado, usuarioLogado);

		chatMensagemRepository.marcarTodasComoLidas(chamadoId, usuarioLogado.getId());

		List<ChatMensagemEntity> todasMensagens = chatMensagemRepository.findByChamadoIdOrderByEnviadoEmAsc(chamadoId);

		if (usuarioLogado.getPerfil().equals(PerfilEnum.USUARIO)) {
			return todasMensagens.stream().filter(ChatMensagemEntity::getVisivelParaCliente).toList();
		}

		return todasMensagens;
	}

	private void defineNovosAnexos(ChatMensagemEntity mensagemEntity, ChatMensagemInput mensagemInput,
			UsuarioEntity usuarioLogado) {

		if (mensagemInput.getArquivos() != null && !mensagemInput.getArquivos().isEmpty()) {
			for (MultipartFile arquivo : mensagemInput.getArquivos()) {
				validarExtensao(arquivo);

				AnexoInput anexoInput = new AnexoInput();
				anexoInput.setArquivo(arquivo);
				anexoInput.setNomeArquivo(arquivo.getOriginalFilename());
				anexoInput.setTamanhoBytes(arquivo.getSize());

				anexoInput.setTipo(identificarTipoAnexo(arquivo));

				AnexoEntity anexoCriado = anexoService.criarParaChat(anexoInput, mensagemEntity, usuarioLogado);

				mensagemEntity.getAnexos().add(anexoCriado);
			}
		}
	}

	private TipoAnexoEnum identificarTipoAnexo(MultipartFile arquivo) {
		String nomeOriginal = arquivo.getOriginalFilename();
		String ext = "";

		if (nomeOriginal != null && nomeOriginal.contains(".")) {
			ext = nomeOriginal.substring(nomeOriginal.lastIndexOf(".") + 1).toLowerCase();
		}

		return switch (ext) {
		case "pdf" -> TipoAnexoEnum.PDF;
		case "png" -> TipoAnexoEnum.PNG;
		case "jpg", "jpeg" -> TipoAnexoEnum.JPG;
		case "zip" -> TipoAnexoEnum.ZIP;
		case "docx" -> TipoAnexoEnum.DOCX;
		case "doc" -> TipoAnexoEnum.DOC;
		default -> TipoAnexoEnum.DOC;
		};
	}

	private TipoMensagemEnum detectarTipoMensagem(List<MultipartFile> arquivos) {
		if (arquivos == null || arquivos.isEmpty()) {
			return TipoMensagemEnum.TEXTO;
		}

		boolean apenasImagens = arquivos.stream().allMatch(file -> {
			String nome = file.getOriginalFilename().toLowerCase();
			return nome.endsWith("png") || nome.endsWith("jpg") || nome.endsWith("jpeg");
		});

		if (apenasImagens) {
			return TipoMensagemEnum.IMAGEM;
		} else {
			return TipoMensagemEnum.ARQUIVO;
		}
	}

	private void validarExtensao(MultipartFile arquivo) {
		String nome = arquivo.getOriginalFilename();
		if (nome == null || !nome.contains("."))
			throw new BadRequestBusinessException("Arquivo inválido.");

		String ext = nome.substring(nome.lastIndexOf(".") + 1).toLowerCase();

		if (!EXTENSOES_PERMITIDAS.contains(ext)) {
			throw new BadRequestBusinessException(
					"Formato não permitido: " + ext + ". Use: PDF, DOC, DOCX, PNG, JPG, ZIP.");
		}
	}

	private void definirDestinatario(ChatMensagemEntity mensagem, ChamadoEntity chamado, UsuarioEntity remetente) {
		if (chamado.getSolicitante().getId().equals(remetente.getId())) {
			if (chamado.getTecnicoResponsavel() != null) {
				mensagem.setDestinatario(chamado.getTecnicoResponsavel());
			}
		} else if (chamado.getTecnicoResponsavel() != null
				&& chamado.getTecnicoResponsavel().getId().equals(remetente.getId())) {
			mensagem.setDestinatario(chamado.getSolicitante());
		} else {
			mensagem.setDestinatario(chamado.getSolicitante());
		}
	}

	private void validarPermissaoChat(ChamadoEntity chamado, UsuarioEntity usuario) {
		boolean ehSolicitante = chamado.getSolicitante().getId().equals(usuario.getId());
		boolean ehTecnicoResponsavel = chamado.getTecnicoResponsavel() != null
				&& chamado.getTecnicoResponsavel().getId().equals(usuario.getId());
		boolean ehAdmin = PerfilEnum.ADMIN.equals(usuario.getPerfil())
				|| (PerfilEnum.ADMIN_EMPRESA.equals(usuario.getPerfil())
						&& chamado.getSolicitante().getEmpresa().getId().equals(usuario.getEmpresa().getId()));

		if (!ehSolicitante && !ehTecnicoResponsavel && !ehAdmin) {
			throw new UnauthorizedAccessBusinessException("Você não tem permissão para participar deste chat.");
		}
	}

	private void validarStatusChamado(ChamadoEntity chamado) {
		if (StatusChamadoEnum.CONCLUIDO.equals(chamado.getStatus())) {
			throw new BadRequestBusinessException("Chat encerrado: O chamado está concluído.");
		}
	}

	private void validarConteudo(ChatMensagemInput input) {
		boolean semTexto = input.getConteudo() == null || input.getConteudo().isBlank();
		boolean semAnexo = input.getArquivos() == null || input.getArquivos().isEmpty();

		if (semTexto && semAnexo) {
			throw new BadRequestBusinessException("A mensagem deve conter texto ou anexo.");
		}
	}
}