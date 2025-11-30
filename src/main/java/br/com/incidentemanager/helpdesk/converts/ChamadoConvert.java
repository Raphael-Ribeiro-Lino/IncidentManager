package br.com.incidentemanager.helpdesk.converts;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoDetalhadoOutput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoOutput;
import br.com.incidentemanager.helpdesk.dto.outputs.InteracaoOutput;
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

	public Page<ChamadoOutput> pageEntityToPageOutput(Page<ChamadoEntity> chamados) {
		return chamados.map(this::entityToOutput);
	}

	public ChamadoDetalhadoOutput entityToDetalhadoOutput(ChamadoEntity entity) {
		return converterComFiltro(entity, false);
	}

	public ChamadoDetalhadoOutput entityToDetalhadoOutputPublico(ChamadoEntity entity) {
		return converterComFiltro(entity, true);
	}

	private ChamadoDetalhadoOutput converterComFiltro(ChamadoEntity entity, boolean apenasPublicos) {
		ChamadoDetalhadoOutput output = modelMapper.map(entity, ChamadoDetalhadoOutput.class);
		if (entity.getInteracoes() != null) {
			List<InteracaoOutput> historico = entity.getInteracoes().stream()
					.filter(interacao -> !apenasPublicos || interacao.isVisivelCliente())
					.sorted((a, b) -> a.getDataHora().compareTo(b.getDataHora())).map(interacao -> {
						InteracaoOutput out = modelMapper.map(interacao, InteracaoOutput.class);
						if (interacao.getAutor() != null) {
							out.setAutorNome(interacao.getAutor().getNome());
							out.setAutorPerfil(interacao.getAutor().getPerfil().name());
						} else {
							out.setAutorNome("Sistema");
							out.setAutorPerfil("SYSTEM");
						}
						return out;
					}).collect(Collectors.toList());
			output.setHistoricoEventos(historico);
		}
		return output;
	}

}
