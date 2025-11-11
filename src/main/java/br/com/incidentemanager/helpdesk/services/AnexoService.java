package br.com.incidentemanager.helpdesk.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.incidentemanager.helpdesk.dto.inputs.AnexoInput;
import br.com.incidentemanager.helpdesk.entities.AnexoEntity;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
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
			anexoEntity.setStoragePath(s3Service.saveFile(key, inputStream, anexoInput.getArquivo().getSize(),
					anexoInput.getArquivo().getContentType(), bucketName));
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
}
