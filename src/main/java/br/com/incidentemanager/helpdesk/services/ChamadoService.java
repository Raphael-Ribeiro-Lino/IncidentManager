package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import br.com.incidentemanager.helpdesk.dto.inputs.AnexoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.entities.AnexoEntity;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class ChamadoService {

	@Autowired
	private ChamadoRepository chamadoRepository;

	@Autowired
	private AnexoService anexoService;

	@Autowired
	private InteracaoService interacaoService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Transactional
	public ChamadoEntity criar(ChamadoEntity chamadoEntity, @Valid ChamadoInput chamadoInput,
			UsuarioEntity usuarioLogado) {
		chamadoEntity.setSolicitante(usuarioLogado);
		defineTecnicoResponsavel(chamadoEntity);
		chamadoEntity.setAnexos(null);
		ChamadoEntity chamadoCriado = chamadoRepository.saveAndFlush(chamadoEntity);
		defineNovosAnexos(chamadoCriado, chamadoInput, usuarioLogado);
		interacaoService.registrarAberturaChamado(chamadoCriado, usuarioLogado);
		return chamadoRepository.save(chamadoCriado);
	}

	private void defineNovosAnexos(ChamadoEntity chamadoEntity, ChamadoInput chamadoInput, UsuarioEntity usuarioLogado) {
		List<AnexoEntity> anexos = new ArrayList<>();
		if (chamadoInput.getAnexos() != null) {
			for (AnexoInput anexoInput : chamadoInput.getAnexos()) {
				AnexoEntity anexoCriado = anexoService.criar(anexoInput, chamadoEntity, usuarioLogado);
				anexos.add(anexoCriado);
			}
		}
		chamadoEntity.setAnexos(anexos);
	}

	private void defineTecnicoResponsavel(ChamadoEntity chamadoEntity) {
		Long idEmpresa = chamadoEntity.getSolicitante().getEmpresa().getId();
		UsuarioEntity tecnicoComMenosChamados = usuarioRepository.findTecnicoComMenosChamados(idEmpresa);
		if (tecnicoComMenosChamados == null) {
			throw new NotFoundBusinessException("Nenhum técnico disponível para atribuição.");
		}
		chamadoEntity.setTecnicoResponsavel(tecnicoComMenosChamados);
	}

	public Page<ChamadoEntity> lista(Pageable pagination, UsuarioEntity usuarioLogado) {
		return chamadoRepository.findAllBySolicitante(pagination, usuarioLogado);
	}

	public ChamadoEntity buscaPorId(Long id, UsuarioEntity usuarioLogado) {
		ChamadoEntity chamadoEncontrado = chamadoRepository.findByIdAndSolicitante(id, usuarioLogado)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado " + id + " não encontrado"));
		return chamadoEncontrado;
	}

	public void atualizaStoragePathComLinkTemporario(ChamadoEntity chamadoEncontrado) {
		chamadoEncontrado.getAnexos().forEach(anexo -> anexo.setStoragePath(anexoService.geraLinkTemporario(anexo)));
	}

	@Transactional
	public ChamadoEntity alterarMeuChamado(ChamadoEntity chamadoEntity, ChamadoInput chamadoInput) {
		chamadoEntity.setTitulo(chamadoInput.getTitulo());
		chamadoEntity.setDescricao(chamadoInput.getDescricao());
		chamadoEntity.setPrioridade(chamadoInput.getPrioridade());
		chamadoEntity.setDataUltimaAtualizacao(Instant.now());
		if(chamadoInput.getAnexos() != null) {
			anexoService.atualizarAnexos(chamadoEntity, chamadoInput.getAnexos(), chamadoEntity.getSolicitante());
		}
		return chamadoRepository.save(chamadoEntity);
	}

	public void verificaSeStatusDoChamadoEstaAberto(StatusChamadoEnum status) {
		if(!status.equals(StatusChamadoEnum.ABERTO) && !status.equals(StatusChamadoEnum.TRIAGEM) && !status.equals(StatusChamadoEnum.REABERTO)) {
			throw new BadRequestBusinessException("O chamado não pode ser alterado!");
		}
	}
}
