package br.com.incidentemanager.helpdesk.converts;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.outputs.TransferenciaDetalhadaOutput;
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

	public TransferenciaDetalhadaOutput entityToDetalhadaOutput(TransferenciaEntity entity) {
		TransferenciaDetalhadaOutput output = modelMapper.map(entity, TransferenciaDetalhadaOutput.class);

		if (entity.getChamado() != null) {
			output.setChamadoId(entity.getChamado().getId());
			output.setChamadoProtocolo(entity.getChamado().getProtocolo());
			output.setChamadoTitulo(entity.getChamado().getTitulo());
			output.setChamadoDescricao(entity.getChamado().getDescricao());

			if (entity.getChamado().getPrioridade() != null) {
				output.setChamadoPrioridade(entity.getChamado().getPrioridade().name());
			}

			if (entity.getChamado().getStatus() != null) {
				output.setChamadoStatus(entity.getChamado().getStatus().name());
			}

			if (entity.getChamado().getDataCriacao() != null) {
				output.setChamadoDataCriacao(entity.getChamado().getDataCriacao().toString());
			}
		}

		if (entity.getTecnicoDestino() != null) {
			output.setTecnicoDestinoId(entity.getTecnicoDestino().getId());
			output.setTecnicoDestinoNome(entity.getTecnicoDestino().getNome());
		}

		if (entity.getDataSolicitacao() != null) {
			output.setDataSolicitacao(entity.getDataSolicitacao().toString());
		}

		return output;
	}

	public Page<TransferenciaDetalhadaOutput> pageEntityToPageDetalhadaOutput(
			Page<TransferenciaEntity> transferencias) {
		return transferencias.map(this::entityToDetalhadaOutput);
	}

}
