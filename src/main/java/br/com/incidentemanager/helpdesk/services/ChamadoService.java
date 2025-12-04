package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.AlteraStatusChamadoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.AnexoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.AvaliacaoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.ChamadoInput;
import br.com.incidentemanager.helpdesk.dto.inputs.ReabrirChamadoInput;
import br.com.incidentemanager.helpdesk.entities.AnexoEntity;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;
import br.com.incidentemanager.helpdesk.enums.TipoNotificacaoEnum; // Enum criado anteriormente
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
	private UsuarioRepository usuarioRepository;

	@Autowired
	private NotificacaoService notificacaoService; // <--- 1. INJEÇÃO DA NOTIFICAÇÃO

	@Transactional
	public ChamadoEntity criar(ChamadoEntity chamadoEntity, @Valid ChamadoInput chamadoInput,
			UsuarioEntity usuarioLogado) {
		chamadoEntity.setSolicitante(usuarioLogado);
		defineTecnicoResponsavel(chamadoEntity);
		chamadoEntity.setAnexos(null);

		ChamadoEntity chamadoCriado = chamadoRepository.saveAndFlush(chamadoEntity);
		defineNovosAnexos(chamadoCriado, chamadoInput, usuarioLogado);

		ChamadoEntity salvo = chamadoRepository.save(chamadoCriado);

		// NOTIFICAÇÃO 1: Avisar o Técnico que ele recebeu um chamado novo
		if (salvo.getTecnicoResponsavel() != null) {
			notificacaoService.criar(salvo.getTecnicoResponsavel(), "Novo Chamado Atribuído: " + salvo.getProtocolo(),
					"Você foi definido como responsável pelo chamado: " + salvo.getTitulo(), salvo,
					TipoNotificacaoEnum.TRANSFERENCIA // Ou MUDANCA_STATUS se preferir
			);
		}

		return salvo;
	}

	@Transactional
	public ChamadoEntity atualizarStatus(ChamadoEntity chamadoEntity,
			@Valid AlteraStatusChamadoInput alteraStatusChamadoInput, UsuarioEntity usuarioLogado) {

		StatusChamadoEnum novoStatus = alteraStatusChamadoInput.getStatus();

		if (StatusChamadoEnum.CONCLUIDO.equals(novoStatus)) {
			throw new BadRequestBusinessException(
					"O técnico não pode encerrar o chamado diretamente. Altere para 'RESOLVIDO' e aguarde a avaliação do cliente.");
		}
		if (StatusChamadoEnum.ABERTO.equals(novoStatus)) {
			throw new BadRequestBusinessException(
					"Não é possível voltar o chamado para ABERTO. Use EM_ATENDIMENTO ou TRIAGEM.");
		}

		chamadoEntity.setStatus(novoStatus);
		chamadoEntity.setDataUltimaAtualizacao(Instant.now());
		chamadoRepository.save(chamadoEntity);

		// NOTIFICAÇÃO 2: Avisar o Cliente sobre a mudança
		if (StatusChamadoEnum.RESOLVIDO.equals(novoStatus)) {
			// Caso especial: Resolvido (Exige ação do usuário)
			notificacaoService.criar(chamadoEntity.getSolicitante(),
					"Chamado Resolvido! " + chamadoEntity.getProtocolo(),
					"O técnico informou que o problema foi solucionado. Clique aqui para confirmar e avaliar.",
					chamadoEntity, TipoNotificacaoEnum.RESOLUCAO);
		} else {
			// Mudança comum (Em atendimento, Aguardando, etc)
			notificacaoService.criar(chamadoEntity.getSolicitante(),
					"Atualização no chamado " + chamadoEntity.getProtocolo(),
					"O status do seu chamado mudou para: " + novoStatus, chamadoEntity,
					TipoNotificacaoEnum.MUDANCA_STATUS);
		}

		return chamadoEntity;
	}

	@Transactional
	public ChamadoEntity avaliarEFechar(ChamadoEntity chamadoEntity, @Valid AvaliacaoInput avaliacaoInput) {
		if (StatusChamadoEnum.CONCLUIDO.equals(chamadoEntity.getStatus())) {
			throw new BadRequestBusinessException("Este chamado já foi avaliado e encerrado.");
		}
		if (!chamadoEntity.getStatus().equals(StatusChamadoEnum.RESOLVIDO)) {
			throw new BadRequestBusinessException("Este chamado ainda não foi resolvido pelo técnico responsável.");
		}

		chamadoEntity.setAvaliacaoNota(avaliacaoInput.getNota());
		chamadoEntity.setAvaliacaoComentario(avaliacaoInput.getComentario());
		chamadoEntity.setStatus(StatusChamadoEnum.CONCLUIDO);
		chamadoEntity.setDataFechamento(Instant.now());
		chamadoEntity.setDataUltimaAtualizacao(Instant.now());

		ChamadoEntity salvo = chamadoRepository.save(chamadoEntity);

		// NOTIFICAÇÃO 3: Avisar o Técnico sobre a nota recebida (Feedback)
		if (salvo.getTecnicoResponsavel() != null) {
			notificacaoService.criar(salvo.getTecnicoResponsavel(), "Chamado Avaliado: " + salvo.getProtocolo(),
					"O cliente encerrou o chamado com nota " + avaliacaoInput.getNota() + "/5.", salvo,
					TipoNotificacaoEnum.MUDANCA_STATUS // Se não tiver no Enum, use MUDANCA_STATUS ou RESOLUCAO
			);
		}

		return salvo;
	}

	@Transactional
	public ChamadoEntity reabrir(Long id, @Valid ReabrirChamadoInput reabrirChamadoInput, UsuarioEntity usuarioLogado,
			ChamadoEntity chamadoEntity) {
		if (!StatusChamadoEnum.RESOLVIDO.equals(chamadoEntity.getStatus())) {
			throw new BadRequestBusinessException(
					"Apenas chamados no status RESOLVIDO (aguardando aprovação) podem ser reabertos.");
		}

		chamadoEntity.setStatus(StatusChamadoEnum.REABERTO);
		chamadoEntity.setDataUltimaAtualizacao(Instant.now());
		if (chamadoEntity.getDataFechamento() != null) {
			chamadoEntity.setDataFechamento(null);
		}

		ChamadoEntity salvo = chamadoRepository.save(chamadoEntity);

		// NOTIFICAÇÃO 4: Alerta Crítico para o Técnico (Reabertura)
		if (salvo.getTecnicoResponsavel() != null) {
			notificacaoService.criar(salvo.getTecnicoResponsavel(), "⚠️ Chamado Reaberto: " + salvo.getProtocolo(),
					"O cliente não aceitou a solução. Motivo: " + reabrirChamadoInput.getMotivo(), salvo,
					TipoNotificacaoEnum.REABERTURA);
		}

		return salvo;
	}

	// --- MÉTODOS AUXILIARES E DE LEITURA (SEM ALTERAÇÃO NA LÓGICA DE NOTIFICAÇÃO)
	// ---

	private void defineNovosAnexos(ChamadoEntity chamadoEntity, ChamadoInput chamadoInput,
			UsuarioEntity usuarioLogado) {
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
			// Se não tiver técnico, o chamado fica sem responsável (na fila da empresa)
			// Futuramente pode notificar o ADMIN_EMPRESA aqui que chegou um chamado sem
			// técnico
			// throw new NotFoundBusinessException("Nenhum técnico disponível para
			// atribuição.");
		} else {
			chamadoEntity.setTecnicoResponsavel(tecnicoComMenosChamados);
		}
	}

	public Page<ChamadoEntity> lista(Pageable pagination, UsuarioEntity usuarioLogado, String searchTerm,
			StatusChamadoEnum status) {
		return chamadoRepository.findAllBySolicitanteFiltrado(pagination, usuarioLogado, searchTerm, status);
	}

	public ChamadoEntity buscaPorId(Long id, UsuarioEntity usuarioLogado) {
		return chamadoRepository.findByIdAndSolicitante(id, usuarioLogado)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado " + id + " não encontrado"));
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
		if (chamadoInput.getAnexos() != null) {
			anexoService.atualizarAnexos(chamadoEntity, chamadoInput.getAnexos(), chamadoEntity.getSolicitante());
		}
		return chamadoRepository.save(chamadoEntity);
	}

	public void verificaSeStatusDoChamadoEstaAberto(StatusChamadoEnum status) {
		if (!status.equals(StatusChamadoEnum.ABERTO) && !status.equals(StatusChamadoEnum.TRIAGEM)
				&& !status.equals(StatusChamadoEnum.REABERTO)) {
			throw new BadRequestBusinessException("O chamado não pode ser alterado!");
		}
	}

	public Page<ChamadoEntity> listaMeusAtentimentos(Pageable pagination, UsuarioEntity usuarioLogado,
			PrioridadeEnum prioridade, String busca) {
		return chamadoRepository.findAllByTecnicoResponsavelFiltrado(pagination, usuarioLogado, prioridade,
				StatusChamadoEnum.CONCLUIDO, busca, StatusTransferenciaEnum.PENDENTE);
	}

	public ChamadoEntity buscaAtendimentoPorId(Long id, UsuarioEntity usuarioLogado) {
		return chamadoRepository.findByIdAndTecnicoResponsavel(id, usuarioLogado)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado " + id + " não encontrado"));
	}

	public void verificaSeChamadoFoiConcluido(ChamadoEntity chamadoEntity) {
		if (chamadoEntity.getStatus() == StatusChamadoEnum.CONCLUIDO) {
			throw new BadRequestBusinessException("Chamado concluído não pode ser alterado!");
		}
	}
}