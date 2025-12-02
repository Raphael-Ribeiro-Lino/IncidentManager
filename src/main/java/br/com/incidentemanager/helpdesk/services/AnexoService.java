package br.com.incidentemanager.helpdesk.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.incidentemanager.helpdesk.dto.inputs.AnexoInput;
import br.com.incidentemanager.helpdesk.entities.AnexoEntity;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.ChatMensagemEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.exceptions.BusinessException;
import br.com.incidentemanager.helpdesk.repositories.AnexoRepository;
import jakarta.transaction.Transactional;

@Service
public class AnexoService {

	@Autowired
	private AnexoRepository anexoRepository;

	@Autowired
	private S3Service s3Service;

	@Value("${api.bucket-name}")
	private String bucketName;

	@Transactional
	public AnexoEntity criar(AnexoInput anexoInput, ChamadoEntity chamadoEntity, UsuarioEntity usuarioLogado) {
		AnexoEntity anexoEntity = new AnexoEntity();
		anexoEntity.setEnviadoPor(usuarioLogado);
		anexoEntity.setNomeArquivo(anexoInput.getNomeArquivo());
		anexoEntity.setTamanhoBytes(anexoInput.getTamanhoBytes());
		anexoEntity.setTipo(anexoInput.getTipo());
		anexoEntity.setChamado(chamadoEntity);

		defineLinkAnexos(anexoInput, chamadoEntity, usuarioLogado, anexoEntity);

		return anexoRepository.save(anexoEntity);
	}

	public void defineLinkAnexos(AnexoInput anexoInput, ChamadoEntity chamadoEntity, UsuarioEntity usuarioLogado,
			AnexoEntity anexoEntity) {
		String key = String.format("chamados/%d/anexos/%d_%d_%s", chamadoEntity.getId(), usuarioLogado.getId(),
				System.currentTimeMillis(), anexoInput.getNomeArquivo().replaceAll("\\s+", "_"));
		try (InputStream inputStream = anexoInput.getArquivo().getInputStream()) {
			s3Service.saveFile(key, inputStream, anexoInput.getArquivo().getSize(),
					anexoInput.getArquivo().getContentType(), bucketName);
			anexoEntity.setStoragePath(key);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException("Erro ao salvar o arquivo, tente novamente!");
		}
	}

	public File convertToFile(MultipartFile multipartFile) throws IOException {
		File convFile = new File(multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convFile)) {
			fos.write(multipartFile.getBytes());
		}
		return convFile;
	}

	public String geraLinkTemporario(AnexoEntity anexoEntity) {
		return s3Service.generatePresignedUrl(anexoEntity.getStoragePath(), bucketName);
	}

	public void atualizarAnexos(ChamadoEntity chamadoEntity, List<AnexoInput> novosAnexos,
			UsuarioEntity usuarioEntity) {
		List<AnexoEntity> anexosAtuais = chamadoEntity.getAnexos();

		anexosAtuais.removeIf(anexoAtual -> {
			boolean existeNaNovaLista = novosAnexos.stream()
					.anyMatch(a -> a.getNomeArquivo().equals(anexoAtual.getNomeArquivo())
							&& a.getTamanhoBytes().equals(anexoAtual.getTamanhoBytes())
							&& a.getTipo().equals(anexoAtual.getTipo()));
			if (!existeNaNovaLista) {
				deletar(anexoAtual);
				return true;
			}
			return false;
		});

		for (AnexoInput novoAnexo : novosAnexos) {
			boolean jaExiste = anexosAtuais.stream()
					.anyMatch(a -> a.getNomeArquivo().equals(novoAnexo.getNomeArquivo())
							&& a.getTamanhoBytes().equals(novoAnexo.getTamanhoBytes())
							&& a.getTipo().equals(novoAnexo.getTipo()));

			if (!jaExiste) {
				AnexoEntity criado = criar(novoAnexo, chamadoEntity, usuarioEntity);
				anexosAtuais.add(criado);
			}
		}

		chamadoEntity.setAnexos(anexosAtuais);
	}

	@Transactional
	public void deletar(AnexoEntity anexoEntity) {
		try {
			s3Service.deleteFile(anexoEntity.getStoragePath(), bucketName);
			anexoRepository.delete(anexoEntity);
		} catch (Exception e) {
			throw new BusinessException("Erro ao excluir anexo: " + anexoEntity.getNomeArquivo());
		}
	}

	@Transactional
	public AnexoEntity criarParaChat(AnexoInput anexoInput, ChatMensagemEntity mensagem, UsuarioEntity usuarioLogado) {
		AnexoEntity anexoEntity = new AnexoEntity();
		anexoEntity.setEnviadoPor(usuarioLogado);
		anexoEntity.setNomeArquivo(anexoInput.getNomeArquivo());
		anexoEntity.setTamanhoBytes(anexoInput.getTamanhoBytes());
		anexoEntity.setTipo(anexoInput.getTipo());

		anexoEntity.setChatMensagem(mensagem);
		anexoEntity.setChamado(mensagem.getChamado());

		defineLinkAnexosChat(anexoInput, mensagem, usuarioLogado, anexoEntity);

		return anexoRepository.save(anexoEntity);
	}

	private void defineLinkAnexosChat(AnexoInput anexoInput, ChatMensagemEntity mensagem, UsuarioEntity usuarioLogado,
			AnexoEntity anexoEntity) {
		String key = String.format("chamados/%d/chat/%d/%d_%s", mensagem.getChamado().getId(), mensagem.getId(),
				System.currentTimeMillis(), anexoInput.getNomeArquivo().replaceAll("\\s+", "_"));

		try (InputStream inputStream = anexoInput.getArquivo().getInputStream()) {
			s3Service.saveFile(key, inputStream, anexoInput.getArquivo().getSize(),
					anexoInput.getArquivo().getContentType(), bucketName);
			anexoEntity.setStoragePath(key);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException("Erro ao salvar o arquivo do chat, tente novamente!");
		}
	}
}
