package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;
import java.util.ArrayList;
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
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.enums.TipoAnexoEnum;
import br.com.incidentemanager.helpdesk.enums.TipoMensagemEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.ChatMensagemRepository;
import jakarta.validation.Valid;

@Service
public class ChatMensagemService {

	@Autowired
	private ChatMensagemRepository chatMensagemRepository;

	@Autowired
	private AnexoService anexoService;
	
	@Autowired
	private ChamadoRepository chamadoRepository;

	private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("pdf", "docx", "doc", "png", "jpg", "jpeg", "zip");

	public ChatMensagemEntity enviar(ChatMensagemEntity mensagemEntity, @Valid ChatMensagemInput mensagemInput, Long chamadoId,
			UsuarioEntity usuarioLogado) {
		ChamadoEntity chamado = chamadoRepository.findById(chamadoId)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado não encontrado"));

		validarRegrasEnvio(chamado, mensagemInput);

		mensagemEntity.setChamado(chamado);
		mensagemEntity.setRemetente(usuarioLogado);
		mensagemEntity.setVisivelParaCliente(true);

		boolean temAnexo = mensagemInput.getArquivos() != null && !mensagemInput.getArquivos().isEmpty();
		mensagemEntity.setTipo(temAnexo ? TipoMensagemEnum.ARQUIVO : TipoMensagemEnum.TEXTO);

		if (mensagemEntity.getConteudo() == null)
			mensagemEntity.setConteudo("");

		// 2. Salva Parcial (Gera ID para vincular anexos)
		ChatMensagemEntity mensagemSalva = chatMensagemRepository.saveAndFlush(mensagemEntity);

		// 3. Processa Anexos (Padrão defineNovosAnexos)
		defineNovosAnexos(mensagemSalva, mensagemInput, usuarioLogado);

		// 4. Atualiza Chamado (Data de modificação para subir na lista)
		chamado.setDataUltimaAtualizacao(Instant.now());
		chamadoRepository.save(chamado);

		return chatMensagemRepository.save(mensagemSalva);
	}
	
	private void defineNovosAnexos(ChatMensagemEntity mensagemEntity, ChatMensagemInput mensagemInput,
			UsuarioEntity usuarioLogado) {
		List<AnexoEntity> anexos = new ArrayList<>();
		
		if (mensagemInput.getArquivos() != null) {
			for (MultipartFile arquivo : mensagemInput.getArquivos()) {
				validarExtensao(arquivo);

				// Converte MultipartFile para AnexoInput para usar o AnexoService existente
				AnexoInput anexoInput = new AnexoInput();
				anexoInput.setArquivo(arquivo);
				anexoInput.setNomeArquivo(arquivo.getOriginalFilename());
				anexoInput.setTamanhoBytes(arquivo.getSize());
				anexoInput.setTipo(identificarTipo(arquivo.getContentType()));

				// Chama o método novo no AnexoService
				AnexoEntity anexoCriado = anexoService.criarParaChat(anexoInput, mensagemEntity, usuarioLogado);
				anexos.add(anexoCriado);
			}
		}
		mensagemEntity.setAnexos(anexos);
	}
	
	private void validarRegrasEnvio(ChamadoEntity chamado, ChatMensagemInput input) {
		if (StatusChamadoEnum.CONCLUIDO.equals(chamado.getStatus())) {
			throw new BadRequestBusinessException("Não é possível enviar mensagens em um chamado concluído.");
		}
		
		boolean semTexto = input.getConteudo() == null || input.getConteudo().isBlank();
		boolean semAnexo = input.getArquivos() == null || input.getArquivos().isEmpty();

		if (semTexto && semAnexo) {
			throw new BadRequestBusinessException("A mensagem deve conter texto ou anexo.");
		}
	}

	private void validarExtensao(MultipartFile arquivo) {
		String nome = arquivo.getOriginalFilename();
		if (nome == null || !nome.contains(".")) throw new BadRequestBusinessException("Arquivo inválido.");
		String ext = nome.substring(nome.lastIndexOf(".") + 1).toLowerCase();
		if (!EXTENSOES_PERMITIDAS.contains(ext)) {
			throw new BadRequestBusinessException("Formato não permitido: " + ext);
		}
	}

	private TipoAnexoEnum identificarTipo(String contentType) {
		if (contentType == null) return TipoAnexoEnum.DOC;
		if (contentType.contains("image")) return TipoAnexoEnum.JPG;
		if (contentType.contains("pdf")) return TipoAnexoEnum.PDF;
		return TipoAnexoEnum.DOCX;
	}

}
