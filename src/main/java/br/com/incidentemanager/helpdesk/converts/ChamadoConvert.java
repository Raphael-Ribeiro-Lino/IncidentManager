package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoOutput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import jakarta.validation.Valid;

@Component
public class ChamadoConvert {
	
	@Autowired
	private ModelMapper modelMapper;

	public ChamadoEntity inputToEntity(@Valid ChamadoInput chamadoInput) {
		return modelMapper.map(chamadoInput, ChamadoEntity.class);
	}

	public ChamadoOutput entityToOutput(ChamadoEntity chamadoCriado) {
		return modelMapper.map(chamadoCriado, ChamadoOutput.class);
	}

}
