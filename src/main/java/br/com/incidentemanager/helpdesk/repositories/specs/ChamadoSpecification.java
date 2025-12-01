package br.com.incidentemanager.helpdesk.repositories.specs;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.incidentemanager.helpdesk.dto.inputs.RelatorioFiltroInput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import jakarta.persistence.criteria.Predicate;

public class ChamadoSpecification {

	public static Specification<ChamadoEntity> comFiltros(RelatorioFiltroInput filtro) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filtro.getDataInicio() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataCriacao"), filtro.getDataInicio().atStartOfDay()));
			}
			if (filtro.getDataFim() != null) {
				predicates
						.add(cb.lessThanOrEqualTo(root.get("dataCriacao"), filtro.getDataFim().atTime(LocalTime.MAX)));
			}

			if (filtro.getStatus() != null) {
				predicates.add(cb.equal(root.get("status"), filtro.getStatus()));
			}
			if (filtro.getPrioridade() != null) {
				predicates.add(cb.equal(root.get("prioridade"), filtro.getPrioridade()));
			}

			if (filtro.getTecnicoId() != null) {
				predicates.add(cb.equal(root.get("tecnicoResponsavel").get("id"), filtro.getTecnicoId()));
			}

			if (filtro.getEmpresaId() != null) {
				predicates.add(cb.equal(root.get("solicitante").get("empresa").get("id"), filtro.getEmpresaId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

	}
}