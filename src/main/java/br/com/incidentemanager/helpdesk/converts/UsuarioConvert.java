package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.inputs.AlteraMeusDadosInput;
import br.com.incidentemanager.helpdesk.dto.inputs.AlteraUsuarioInput;
import br.com.incidentemanager.helpdesk.dto.inputs.UsuarioInput;
import br.com.incidentemanager.helpdesk.dto.outputs.UsuarioOutput;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import jakarta.validation.Valid;

@Component
public class UsuarioConvert {

	@Autowired
	private ModelMapper modelMapper;

	public UsuarioEntity inputToEntity(@Valid UsuarioInput usuarioInput) {
		return modelMapper.map(usuarioInput, UsuarioEntity.class);
	}

	public UsuarioOutput entityToOutput(UsuarioEntity usuarioEntity) {
		return modelMapper.map(usuarioEntity, UsuarioOutput.class);
	}

	public Page<UsuarioOutput> pageEntityToPageOutput(Page<UsuarioEntity> usuarios) {
		return usuarios.map(this::entityToOutput);
	}

	public void copyInputToEntity(UsuarioEntity usuarioLogado, @Valid AlteraMeusDadosInput alteraMeusDadosInput) {
		modelMapper.map(alteraMeusDadosInput, usuarioLogado);
	}

	public void copyInputToEntity(UsuarioEntity usuarioEncontrado, @Valid AlteraUsuarioInput alteraUsuarioInput) {
		modelMapper.map(alteraUsuarioInput, usuarioEncontrado);
	}
}
