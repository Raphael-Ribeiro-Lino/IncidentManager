package br.com.incidentemanager.helpdesk.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import br.com.incidentemanager.helpdesk.dto.inputs.RelatorioFiltroInput;
import br.com.incidentemanager.helpdesk.dto.outputs.RelatorioGeradoOutput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.specs.ChamadoSpecification;

@Service
public class RelatorioService {

	@Autowired
	private ChamadoRepository chamadoRepository;

	private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private final DateTimeFormatter FILE_NAME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

	public Page<ChamadoEntity>buscarDadosRelatorio(RelatorioFiltroInput filtro, UsuarioEntity usuarioLogado,Pageable pageable) {
		aplicarRegraDeVisibilidade(filtro, usuarioLogado);
		return chamadoRepository.findAll(ChamadoSpecification.comFiltros(filtro), pageable);
	}

	public RelatorioGeradoOutput gerarExcel(RelatorioFiltroInput filtro, UsuarioEntity usuarioLogado) throws Exception {
		aplicarRegraDeVisibilidade(filtro, usuarioLogado);
		List<ChamadoEntity> chamados = chamadoRepository.findAll(ChamadoSpecification.comFiltros(filtro));

		byte[] bytes = gerarExcelBytes(chamados, usuarioLogado);
		String nome = gerarNomeArquivo(".xlsx");

		return new RelatorioGeradoOutput(nome, bytes,
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	public RelatorioGeradoOutput gerarPdf(RelatorioFiltroInput filtro, UsuarioEntity usuarioLogado) throws Exception {
		aplicarRegraDeVisibilidade(filtro, usuarioLogado);
		List<ChamadoEntity> chamados = chamadoRepository.findAll(ChamadoSpecification.comFiltros(filtro));

		byte[] bytes = gerarPdfBytes(chamados, usuarioLogado);
		String nome = gerarNomeArquivo(".pdf");

		return new RelatorioGeradoOutput(nome, bytes, MediaType.APPLICATION_PDF_VALUE);
	}

	private void aplicarRegraDeVisibilidade(RelatorioFiltroInput filtro, UsuarioEntity usuario) {
		if (PerfilEnum.TECNICO_TI.equals(usuario.getPerfil())) {
			filtro.setTecnicoId(usuario.getId());
			filtro.setEmpresaId(null);
		} else if (PerfilEnum.ADMIN_EMPRESA.equals(usuario.getPerfil())) {
			filtro.setEmpresaId(usuario.getEmpresa().getId());
		}
	}

	private String gerarNomeArquivo(String extensao) {
		return "smartdesk_relatorio_" + LocalDateTime.now().format(FILE_NAME_FMT) + extensao;
	}

	private String formatarTextoEnum(Enum<?> enumValue) {
		if (enumValue == null)
			return "-";
		String nome = enumValue.name();
		return switch (nome) {
		case "BAIXA" -> "Baixa";
		case "MEDIA" -> "Média";
		case "ALTA" -> "Alta";
		case "CRITICA" -> "Crítica";
		case "ABERTO" -> "Aberto";
		case "TRIAGEM" -> "Triagem";
		case "EM_ATENDIMENTO" -> "Em Atendimento";
		case "AGUARDANDO_CLIENTE" -> "Aguardando Cliente";
		case "AGUARDANDO_PECA" -> "Aguardando Peça";
		case "RESOLVIDO" -> "Resolvido";
		case "CONCLUIDO" -> "Concluído";
		case "REABERTO" -> "Reaberto";
		default -> nome;
		};
	}

	private String formatarData(java.time.Instant data) {
		if (data == null)
			return "-";
		return data.atZone(ZoneId.systemDefault()).format(DATE_FMT);
	}

	private String calcularDuracao(ChamadoEntity c) {
		if (c.getDataFechamento() == null)
			return "Aberto";
		long horas = Duration.between(c.getDataCriacao(), c.getDataFechamento()).toHours();
		return horas + "h";
	}

	private byte[] gerarPdfBytes(List<ChamadoEntity> chamados, UsuarioEntity geradoPor) throws Exception {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 30);
			PdfWriter.getInstance(document, out);

			HeaderFooter footer = new HeaderFooter(new Phrase("Página ", new com.lowagie.text.Font(
					com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.NORMAL, Color.GRAY)), true);
			footer.setAlignment(Element.ALIGN_RIGHT);
			footer.setBorder(Rectangle.NO_BORDER);
			document.setFooter(footer);

			document.open();

			com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
					com.lowagie.text.Font.BOLD, new Color(44, 62, 80));
			Paragraph titulo = new Paragraph("Relatório de Atendimentos", titleFont);
			titulo.setAlignment(Element.ALIGN_LEFT);
			titulo.setSpacingAfter(5);
			document.add(titulo);

			com.lowagie.text.Font metaFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
					com.lowagie.text.Font.NORMAL, Color.GRAY);
			Paragraph meta = new Paragraph(
					"Gerado por " + geradoPor.getNome() + " em " + LocalDateTime.now().format(DATE_FMT), metaFont);
			meta.setSpacingAfter(25);
			document.add(meta);

			PdfPTable table = new PdfPTable(8);
			table.setWidthPercentage(100);

			table.setWidths(new float[] { 2f, 2.5f, 1.5f, 6f, 3f, 2.5f, 1.5f, 1f });

			String[] headers = { "Protocolo", "Status", "Prio", "Título", "Técnico", "Abertura", "Tempo", "Nota" };
			com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
					com.lowagie.text.Font.BOLD, Color.WHITE);

			for (String h : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
				cell.setBackgroundColor(new Color(44, 62, 80));
				cell.setPadding(6);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}

			com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9,
					com.lowagie.text.Font.NORMAL, Color.DARK_GRAY);
			boolean zebrado = false;

			for (ChamadoEntity c : chamados) {

				Color bgBase;
				if (StatusChamadoEnum.REABERTO.equals(c.getStatus())) {
					bgBase = new Color(255, 235, 230);
				} else if (StatusChamadoEnum.CONCLUIDO.equals(c.getStatus())) {
					bgBase = new Color(235, 250, 235);
				} else {
					bgBase = zebrado ? new Color(248, 249, 250) : Color.WHITE;
				}

				com.lowagie.text.Font rowFont = dataFont;
				if (PrioridadeEnum.CRITICA.equals(c.getPrioridade())) {
					rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD,
							new Color(192, 57, 43));
				}

				addModernPdfCell(table, c.getProtocolo(), rowFont, bgBase, Element.ALIGN_CENTER);
				addModernPdfCell(table, formatarTextoEnum(c.getStatus()), rowFont, bgBase, Element.ALIGN_CENTER);
				addModernPdfCell(table, formatarTextoEnum(c.getPrioridade()), rowFont, bgBase, Element.ALIGN_CENTER);

				addModernPdfCell(table, c.getTitulo(), rowFont, bgBase, Element.ALIGN_LEFT);

				String tec = c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : "-";
				addModernPdfCell(table, tec, rowFont, bgBase, Element.ALIGN_LEFT);

				addModernPdfCell(table, formatarData(c.getDataCriacao()), rowFont, bgBase, Element.ALIGN_CENTER);
				addModernPdfCell(table, calcularDuracao(c), rowFont, bgBase, Element.ALIGN_CENTER);

				String nota = c.getAvaliacaoNota() != null ? c.getAvaliacaoNota().toString() : "-";
				addModernPdfCell(table, nota, rowFont, bgBase, Element.ALIGN_CENTER);

				zebrado = !zebrado;
			}

			document.add(table);
			document.close();
			return out.toByteArray();
		}
	}

	private void addModernPdfCell(PdfPTable table, String text, com.lowagie.text.Font font, Color bg, int align) {
		PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
		cell.setBackgroundColor(bg);
		cell.setPadding(6);
		cell.setHorizontalAlignment(align);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

		cell.setBorder(Rectangle.BOTTOM);
		cell.setBorderColor(new Color(220, 220, 220));

		table.addCell(cell);
	}

	private byte[] gerarExcelBytes(List<ChamadoEntity> chamados, UsuarioEntity geradoPor) throws Exception {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Relatório Analítico");
			sheet.setDisplayGridlines(false);

			CellStyle titleStyle = workbook.createCellStyle();
			Font titleFont = workbook.createFont();
			titleFont.setFontHeightInPoints((short) 16);
			titleFont.setBold(true);
			titleFont.setColor(IndexedColors.DARK_TEAL.getIndex());
			titleStyle.setFont(titleFont);

			CellStyle metaStyle = workbook.createCellStyle();
			Font metaFont = workbook.createFont();
			metaFont.setItalic(true);
			metaFont.setFontHeightInPoints((short) 10);
			metaFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
			metaStyle.setFont(metaFont);

			CellStyle headerStyle = criarEstiloCabecalhoExcel(workbook);

			CellStyle styleCentro = criarEstiloDadosExcel(workbook, HorizontalAlignment.CENTER);
			CellStyle styleTexto = criarEstiloDadosExcel(workbook, HorizontalAlignment.LEFT);
			CellStyle styleTextoWrap = criarEstiloDadosExcel(workbook, HorizontalAlignment.LEFT);
			styleTextoWrap.setWrapText(true);

			CellStyle styleRowReaberto = criarEstiloDadosExcelColorido(workbook, IndexedColors.CORAL,
					HorizontalAlignment.LEFT);
			CellStyle styleRowReabertoCentro = criarEstiloDadosExcelColorido(workbook, IndexedColors.CORAL,
					HorizontalAlignment.CENTER);

			CellStyle styleRowConcluido = criarEstiloDadosExcelColorido(workbook, IndexedColors.LIGHT_GREEN,
					HorizontalAlignment.LEFT);
			CellStyle styleRowConcluidoCentro = criarEstiloDadosExcelColorido(workbook, IndexedColors.LIGHT_GREEN,
					HorizontalAlignment.CENTER);

			Row rowTitle = sheet.createRow(0);
			Cell cellTitle = rowTitle.createCell(0);
			cellTitle.setCellValue("SmartDesk - Relatório Analítico");
			cellTitle.setCellStyle(titleStyle);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

			Row rowMeta = sheet.createRow(1);
			Cell cellMeta = rowMeta.createCell(0);
			cellMeta.setCellValue("Gerado por " + geradoPor.getNome() + " em " + LocalDateTime.now().format(DATE_FMT));
			cellMeta.setCellStyle(metaStyle);
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

			Row headerRow = sheet.createRow(3);
			headerRow.setHeightInPoints(30);

			String[] colunas = { "Protocolo", "Status", "Prioridade", "Título", "Descrição Resumida", "Solicitante",
					"Empresa", "Técnico", "Abertura", "Atualização", "Fechamento", "Duração", "Nota", "Comentário" };

			for (int i = 0; i < colunas.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(colunas[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowIdx = 4;
			for (ChamadoEntity c : chamados) {
				Row row = sheet.createRow(rowIdx++);
				row.setHeightInPoints(22);

				CellStyle currentStyleTexto = styleTexto;
				CellStyle currentStyleCentro = styleCentro;

				if (StatusChamadoEnum.REABERTO.equals(c.getStatus())) {
					currentStyleTexto = styleRowReaberto;
					currentStyleCentro = styleRowReabertoCentro;
				} else if (StatusChamadoEnum.CONCLUIDO.equals(c.getStatus())) {
					currentStyleTexto = styleRowConcluido;
					currentStyleCentro = styleRowConcluidoCentro;
				}

				criarCelula(row, 0, c.getProtocolo(), currentStyleCentro);
				criarCelula(row, 1, formatarTextoEnum(c.getStatus()), currentStyleCentro);
				criarCelula(row, 2, formatarTextoEnum(c.getPrioridade()), currentStyleCentro);

				criarCelula(row, 3, c.getTitulo(), currentStyleTexto);

				String desc = (c.getDescricao() != null && c.getDescricao().length() > 50)
						? c.getDescricao().substring(0, 50) + "..."
						: c.getDescricao();
				criarCelula(row, 4, desc, currentStyleTexto);

				criarCelula(row, 5, c.getSolicitante().getNome(), currentStyleTexto);
				criarCelula(row, 6, c.getSolicitante().getEmpresa().getNome(), currentStyleTexto);
				criarCelula(row, 7, c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : "-",
						currentStyleTexto);

				criarCelula(row, 8, formatarData(c.getDataCriacao()), currentStyleCentro);
				criarCelula(row, 9, formatarData(c.getDataUltimaAtualizacao()), currentStyleCentro);
				criarCelula(row, 10, formatarData(c.getDataFechamento()), currentStyleCentro);

				criarCelula(row, 11, calcularDuracao(c), currentStyleCentro);
				criarCelula(row, 12, c.getAvaliacaoNota() != null ? c.getAvaliacaoNota().toString() : "-",
						currentStyleCentro);
				criarCelula(row, 13, c.getAvaliacaoComentario(), currentStyleTexto);
			}

			for (int i = 0; i < colunas.length; i++) {
				sheet.autoSizeColumn(i);
				if (sheet.getColumnWidth(i) > 12000)
					sheet.setColumnWidth(i, 12000);
			}
			sheet.setColumnWidth(13, 10000);

			sheet.setAutoFilter(new CellRangeAddress(3, rowIdx - 1, 0, colunas.length - 1));
			sheet.createFreezePane(0, 4);

			workbook.write(out);
			return out.toByteArray();
		}
	}

	private CellStyle criarEstiloCabecalhoExcel(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		Font font = workbook.createFont();
		font.setColor(IndexedColors.WHITE.getIndex());
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		style.setFont(font);
		return style;
	}

	private CellStyle criarEstiloDadosExcel(Workbook workbook, HorizontalAlignment align) {
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(align);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
		return style;
	}

	private CellStyle criarEstiloDadosExcelColorido(Workbook workbook, IndexedColors corFundo,
			HorizontalAlignment align) {
		CellStyle style = criarEstiloDadosExcel(workbook, align);
		style.setFillForegroundColor(corFundo.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}

	private void criarCelula(Row row, int col, String valor, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(valor != null ? valor : "");
		cell.setCellStyle(style);
	}
}