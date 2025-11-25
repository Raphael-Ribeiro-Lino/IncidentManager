package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.outputs.TransferenciaOutput;
import br.com.incidentemanager.helpdesk.entities.TransferenciaEntity;

@Component
public class TransferenciaConvert {

	@Autowired
	private ModelMapper modelMapper;

	public TransferenciaOutput entityToOutput(TransferenciaEntity transferenciaEntity) {
		return modelMapper.map(transferenciaEntity, TransferenciaOutput.class);
	}

	public Page<TransferenciaOutput> pageEntityToPageOutput(Page<TransferenciaEntity> transferencias) {
		return transferencias.map(this::entityToOutput);
	}

}
