package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@CrossOrigin(origins = { "http://localhost", "http://localhost:4200", "http://localhost:4200/*" })
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
}
