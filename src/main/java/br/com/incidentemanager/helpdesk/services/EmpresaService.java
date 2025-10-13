package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.EmpresaRepository;
import jakarta.transaction.Transactional;

@Service
public class EmpresaService {

	@Autowired
	private EmpresaRepository empresaRepository;

	@Transactional
	public EmpresaEntity cadastra(EmpresaEntity empresaEntity) {
		existeCnpj(empresaEntity.getCnpj());
		return empresaRepository.save(empresaEntity);
	}

	private void existeCnpj(String cnpj) {
		if(empresaRepository.findByCnpj(cnpj).isPresent()) {
			throw new BadRequestBusinessException("Este CNPJ já está em uso por outra empresa.");
		}
	}

	public EmpresaEntity buscaPorId(Long idEmpresa) {
		return empresaRepository.findById(idEmpresa).orElseThrow(() -> new NotFoundBusinessException("Empresa "+ idEmpresa +" não encontrada"));
	}
}
