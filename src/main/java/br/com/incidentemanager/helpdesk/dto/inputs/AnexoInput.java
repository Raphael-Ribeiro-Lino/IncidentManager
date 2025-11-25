package br.com.incidentemanager.helpdesk.dto.inputs;

import org.springframework.web.multipart.MultipartFile;

import br.com.incidentemanager.helpdesk.enums.TipoAnexoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnexoInput {

	@NotBlank(message = "O nome do arquivo é obrigatório.")
    @Size(max = 255, message = "O nome do arquivo não pode exceder {max} caracteres.")
    private String nomeArquivo;
	
    @NotNull(message = "O tamanho do arquivo é obrigatório.")
    @Positive(message = "O tamanho do arquivo deve ser positivo.")
    private Long tamanhoBytes;
    
    @NotNull(message = "O tipo do anexo é obrigatório.")
    private TipoAnexoEnum tipo;
    
    private MultipartFile arquivo;
	
}
