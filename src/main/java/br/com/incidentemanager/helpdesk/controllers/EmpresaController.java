package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.EmpresaConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.EmpresaInput;
import br.com.incidentemanager.helpdesk.dto.outputs.EmpresaOutput;
import br.com.incidentemanager.helpdesk.entities.EmpresaEntity;
import br.com.incidentemanager.helpdesk.services.EmpresaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/empresa")
public class EmpresaController {

	@Autowired
	private EmpresaService empresaService;

	@Autowired
	private EmpresaConvert empresaConvert;

	@PostMapping
	@PodeAcessarSe.TemPerfilAdm
	@ResponseStatus(code = HttpStatus.CREATED)
	public EmpresaOutput cadastrar(@RequestBody @Valid EmpresaInput empresaInput) {
		EmpresaEntity empresaEntity = empresaConvert.inputToEntity(empresaInput);
		return empresaConvert.entityToOutput(empresaService.cadastra(empresaEntity));
	}

	@GetMapping
	@PodeAcessarSe.TemPerfilAdm
	public Page<EmpresaOutput> listar(@RequestParam(required = false) String search,
			@RequestParam(required = false) Boolean ativo,
			@PageableDefault(size = 10, sort = "nome", direction = Direction.ASC) Pageable pagination) {
		Page<EmpresaEntity> empresas = empresaService.listarComFiltros(search, ativo, pagination);
		return empresaConvert.pageEntityToPageOutput(empresas);
	}
}
