package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.inputs.EmpresaInput;
import br.com.incidentemanager.helpdesk.dto.outputs.EmpresaOutput;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import jakarta.validation.Valid;

@Component
public class EmpresaConvert {
	
	@Autowired
	private ModelMapper modelMapper;

	public EmpresaEntity inputToEntity(@Valid EmpresaInput empresaInput) {
		return modelMapper.map(empresaInput, EmpresaEntity.class);
	}

	public EmpresaOutput entityToOutput(EmpresaEntity empresaEntity) {
		return modelMapper.map(empresaEntity, EmpresaOutput.class);
	}

}
