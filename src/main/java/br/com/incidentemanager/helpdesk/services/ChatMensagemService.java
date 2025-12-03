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
		mensagemEntity.setVisivelParaCliente(true);

		definirDestinatario(mensagemEntity, chamado, usuarioLogado);

		// Define o Tipo da Mensagem (Visualização no Chat)
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

		return chatMensagemRepository.save(mensagemSalva);
	}

	public List<ChatMensagemEntity> listarMensagens(Long chamadoId, UsuarioEntity usuarioLogado) {
		ChamadoEntity chamado = chamadoRepository.findById(chamadoId)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado não encontrado"));

		validarPermissaoChat(chamado, usuarioLogado);

		return chatMensagemRepository.findByChamadoIdOrderByEnviadoEmAsc(chamadoId);
	}

	// --- LÓGICA AUXILIAR ---

	private void defineNovosAnexos(ChatMensagemEntity mensagemEntity, ChatMensagemInput mensagemInput,
			UsuarioEntity usuarioLogado) {

		if (mensagemInput.getArquivos() != null && !mensagemInput.getArquivos().isEmpty()) {
			for (MultipartFile arquivo : mensagemInput.getArquivos()) {
				validarExtensao(arquivo);

				AnexoInput anexoInput = new AnexoInput();
				anexoInput.setArquivo(arquivo);
				anexoInput.setNomeArquivo(arquivo.getOriginalFilename());
				anexoInput.setTamanhoBytes(arquivo.getSize());

				// Usa o método ajustado para mapear corretamente o Enum
				anexoInput.setTipo(identificarTipoAnexo(arquivo));

				AnexoEntity anexoCriado = anexoService.criarParaChat(anexoInput, mensagemEntity, usuarioLogado);

				mensagemEntity.getAnexos().add(anexoCriado);
			}
		}
	}

	// Mapeia o arquivo para o seu TipoAnexoEnum (PDF, DOC, DOCX, PNG, JPG, ZIP)
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
		default -> TipoAnexoEnum.DOC; // Fallback seguro, ou lance exceção
		};
	}

	private TipoMensagemEnum detectarTipoMensagem(List<MultipartFile> arquivos) {
		if (arquivos == null || arquivos.isEmpty()) {
			return TipoMensagemEnum.TEXTO;
		}

		// Verifica se TODOS são imagens para exibir galeria
		boolean apenasImagens = arquivos.stream().allMatch(file -> {
			String nome = file.getOriginalFilename().toLowerCase();
			return nome.endsWith("png") || nome.endsWith("jpg") || nome.endsWith("jpeg");
		});

		if (apenasImagens) {
			return TipoMensagemEnum.IMAGEM;
		} else {
			return TipoMensagemEnum.ARQUIVO; // Misto ou Docs
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
