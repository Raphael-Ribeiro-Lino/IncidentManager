package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.inputs.ChatMensagemInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChatMensagemOutput;
import br.com.incidentemanager.helpdesk.entities.ChatMensagemEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.S3Service;
import jakarta.validation.Valid;

@Component
public class ChatMensagemConvert {
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private S3Service s3Service;

	public ChatMensagemEntity inputToEntity(@Valid ChatMensagemInput mensagemInput) {
		return modelMapper.map(mensagemInput, ChatMensagemEntity.class);
	}
	
	public ChatMensagemOutput entityToOutput(ChatMensagemEntity entity, UsuarioEntity usuarioLogado) {
		ChatMensagemOutput out = modelMapper.map(entity, ChatMensagemOutput.class);
		
		out.setRemetenteNome(entity.getRemetente().getNome());
		
        if (entity.getRemetente().getPerfil() != null) {
		    out.setRemetentePerfil(entity.getRemetente().getPerfil().toString());
        }
        
		out.setSouEu(entity.getRemetente().getId().equals(usuarioLogado.getId()));
		
        if (entity.getAnexos() != null && !entity.getAnexos().isEmpty()) {
            out.getAnexos().forEach(anexoOut -> {
                anexoOut.setStoragePath(s3Service.generatePresignedUrl(anexoOut.getStoragePath(), "seu-bucket"));
            });
        }

		return out;
	}

}
