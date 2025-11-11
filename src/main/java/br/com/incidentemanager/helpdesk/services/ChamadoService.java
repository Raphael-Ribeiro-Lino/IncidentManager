package br.com.incidentemanager.helpdesk.services;

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
	private UsuarioRepository usuarioRepository;

	@Transactional
	public ChamadoEntity criar(ChamadoEntity chamadoEntity, @Valid ChamadoInput chamadoInput,
			UsuarioEntity usuarioLogado) {
		chamadoEntity.setSolicitante(usuarioLogado);
		defineTecnicoResponsavel(chamadoEntity);
		chamadoEntity.setAnexos(null);
		ChamadoEntity chamadoCriado = chamadoRepository.saveAndFlush(chamadoEntity);
		defineAnexos(chamadoCriado, chamadoInput, usuarioLogado);

		return chamadoRepository.save(chamadoCriado);
	}

	private void defineAnexos(ChamadoEntity chamadoEntity, ChamadoInput chamadoInput, UsuarioEntity usuarioLogado) {
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
		atualizaLinkTemporario(chamadoEncontrado);
		return chamadoEncontrado;
	}

	private void atualizaLinkTemporario(ChamadoEntity chamadoEncontrado) {
		chamadoEncontrado.getAnexos().forEach(anexo -> anexo.setStoragePath(anexoService.geraLinkTemporario(anexo)));
	}
}
