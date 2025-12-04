package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.outputs.NotificacaoOutput;
import br.com.incidentemanager.helpdesk.entities.NotificacaoEntity;

@Component
public class NotificacaoConvert {
	
	@Autowired
	private ModelMapper modelMapper;

	public NotificacaoOutput entityToOutput(NotificacaoEntity entity) {
		NotificacaoOutput out = modelMapper.map(entity, NotificacaoOutput.class);

		if (entity.getChamado() != null) {
			out.setChamadoId(entity.getChamado().getId());
			out.setChamadoProtocolo(entity.getChamado().getProtocolo());
		}

		return out;
	}

	public Page<NotificacaoOutput> pageEntityToPageOutput(Page<NotificacaoEntity> notificacoes) {
		return notificacoes.map(this::entityToOutput);
	}
}
