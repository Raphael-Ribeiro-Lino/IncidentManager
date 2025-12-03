package br.com.incidentemanager.helpdesk.converts;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.dto.outputs.AnexoOutput;
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

	public ChamadoDetalhadoOutput entityToDetalhadoOutputPublico(ChamadoEntity entity) {
		return converterComFiltro(entity, true); // true = aplica filtro de segurança
	}

	// Método para técnicos (vê tudo)
	public ChamadoDetalhadoOutput entityToDetalhadoOutput(ChamadoEntity entity) {
		return converterComFiltro(entity, false); // false = sem filtro
	}

	private ChamadoDetalhadoOutput converterComFiltro(ChamadoEntity entity, boolean apenasPublicos) {
		ChamadoDetalhadoOutput output = modelMapper.map(entity, ChamadoDetalhadoOutput.class);

		if (entity.getAnexos() != null) {
			List<AnexoOutput> anexosFiltrados = entity.getAnexos().stream().filter(anexo -> {
				if (!apenasPublicos)
					return true;

				if (anexo.getChatMensagem() == null)
					return true;

				return Boolean.TRUE.equals(anexo.getChatMensagem().getVisivelParaCliente());
			}).map(anexo -> modelMapper.map(anexo, AnexoOutput.class)).collect(Collectors.toList());

			output.setAnexos(anexosFiltrados);
		}

		if (entity.getInteracoes() != null) {
			List<InteracaoOutput> historico = entity.getInteracoes().stream()
					.filter(interacao -> !apenasPublicos || interacao.isVisivelCliente())
					.sorted((a, b) -> a.getDataHora().compareTo(b.getDataHora())).map(interacao -> {
						InteracaoOutput out = modelMapper.map(interacao, InteracaoOutput.class);
						if (interacao.getAutor() != null) {
							out.setAutorNome(interacao.getAutor().getNome());
							if (interacao.getAutor().getPerfil().name().contains("TECNICO")
									|| interacao.getAutor().getPerfil().name().contains("ADMIN")) {
								out.setAutorPerfil("Equipe TI");
							} else {
								out.setAutorPerfil("Cliente");
							}
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
