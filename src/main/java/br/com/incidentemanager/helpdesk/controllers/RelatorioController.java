package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.configs.securities.PodeAcessarSe;
import br.com.incidentemanager.helpdesk.converts.ChamadoConvert;
import br.com.incidentemanager.helpdesk.dto.inputs.RelatorioFiltroInput;
import br.com.incidentemanager.helpdesk.dto.outputs.ChamadoOutput;
import br.com.incidentemanager.helpdesk.dto.outputs.RelatorioGeradoOutput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.services.RelatorioService;
import br.com.incidentemanager.helpdesk.services.TokenService;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/relatorio")
public class RelatorioController {

	@Autowired
	private RelatorioService relatorioService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private ChamadoConvert chamadoConvert;

	@GetMapping("/dados")
	@PodeAcessarSe.PodeGerarRelatorio
	public ResponseEntity<Page<ChamadoOutput>> buscarDados(@ModelAttribute RelatorioFiltroInput filtro,
			@PageableDefault(size = 10, sort = "dataCriacao", direction = Direction.DESC) Pageable pageable) {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		Page<ChamadoEntity> chamadosPage = relatorioService.buscarDadosRelatorio(filtro, usuarioLogado, pageable);
		return ResponseEntity.ok(chamadoConvert.pageEntityToPageOutput(chamadosPage));
	}

	@GetMapping("/exportar/excel")
	@PodeAcessarSe.PodeGerarRelatorio
	public ResponseEntity<byte[]> exportarExcel(@ModelAttribute RelatorioFiltroInput filtro) throws Exception {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		RelatorioGeradoOutput relatorio = relatorioService.gerarExcel(filtro, usuarioLogado);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + relatorio.getNomeArquivo())
				.contentType(MediaType.parseMediaType(relatorio.getContentType())).body(relatorio.getDados());
	}

	@GetMapping("/exportar/pdf")
	@PodeAcessarSe.PodeGerarRelatorio
	public ResponseEntity<byte[]> exportarPdf(@ModelAttribute RelatorioFiltroInput filtro) throws Exception {
		UsuarioEntity usuarioLogado = tokenService.buscaUsuario();
		RelatorioGeradoOutput relatorio = relatorioService.gerarPdf(filtro, usuarioLogado);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + relatorio.getNomeArquivo())
				.contentType(MediaType.parseMediaType(relatorio.getContentType())).body(relatorio.getDados());
	}
}
