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
import br.com.incidentemanager.helpdesk.services.TokenAcaoService;
import jakarta.validation.Valid;

@Component
public class UsuarioConvert {

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private TokenAcaoService tokenAcaoService;

	public UsuarioEntity inputToEntity(@Valid UsuarioInput usuarioInput) {
		return modelMapper.map(usuarioInput, UsuarioEntity.class);
	}

	public UsuarioOutput entityToOutput(UsuarioEntity usuarioEntity) {
		UsuarioOutput output = modelMapper.map(usuarioEntity, UsuarioOutput.class);
		boolean expirado = tokenAcaoService.isTokenExpirado(usuarioEntity);
		output.setPodeReenviarEmail(expirado);
		return output;
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
