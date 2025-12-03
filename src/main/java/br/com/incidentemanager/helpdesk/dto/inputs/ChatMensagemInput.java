package br.com.incidentemanager.helpdesk.dto.inputs;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMensagemInput {

	@Size(max = 5000, message = "O conteúdo da mensagem deve ter no máximo 5000 caracteres")
	private String conteudo;

	private List<MultipartFile> arquivos;
	
	private Boolean privado = false; 

}
