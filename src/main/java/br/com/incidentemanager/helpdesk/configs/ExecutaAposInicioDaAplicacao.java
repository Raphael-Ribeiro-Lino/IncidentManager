package br.com.incidentemanager.helpdesk.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.services.LayoutEmailService;
import br.com.incidentemanager.helpdesk.services.UsuarioService;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ExecutaAposInicioDaAplicacao implements ApplicationRunner {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private LayoutEmailService layoutEmailService;

	@Value("${app.frontend.base-url}")
	private String frontendBaseUrl;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		if (!usuarioService.existeAdm()) {
			log.info("Criando usuário administrador padrão...");
			criaUsuarioSeNecessario("SmartDesk Adm", "contato.smartdesk@gmail.com", "(11) 98765-4321",
					"@PrimeiraSenha123", true, PerfilEnum.ADMIN);
		}

		if (!layoutEmailService.existeRedefinirSenha()) {
			log.info("Criando layout padrão para redefinição de senha...");

			String conteudo = """
					<p>Olá,</p>
					<p>Recebemos uma solicitação para redefinir a senha da sua conta no <strong>SmartDesk</strong>.</p>
					<p>Para criar uma nova senha, clique no botão abaixo:</p>

					<table role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary">
					  <tbody>
					    <tr>
					      <td align="center">
					        <table role="presentation" border="0" cellpadding="0" cellspacing="0">
					          <tbody>
					            <tr>
					              <td> <a href="%s/redefinir-senha/{hash}" target="_blank">Redefinir Minha Senha</a> </td>
					            </tr>
					          </tbody>
					        </table>
					      </td>
					    </tr>
					  </tbody>
					</table>

					<p>O link é válido por <strong>15 minutos</strong>.</p>
					<p style="font-size: 14px; color: #999;">Caso não tenha solicitado a alteração, ignore esta mensagem e sua senha permanecerá a mesma.</p>
					"""
					.formatted(frontendBaseUrl);

			criaLayoutSeNecessario("Redefinir Senha", "contato.smartdesk@gmail.com", "Redefina a sua senha",
					gerarHtmlBase("Redefinição de Senha", conteudo));
		}

		if (!layoutEmailService.existeAvisoAlteracaoSenha()) {
			log.info("Criando layout padrão para aviso alteração de senha...");

			String conteudo = """
					<p>Olá,</p>
					<p>Informamos que a senha da sua conta foi alterada recentemente.</p>

					<div style="background-color: #f8f9fa; border-left: 4px solid #007bff; padding: 15px; margin: 20px 0;">
						<strong>Data/Hora da alteração:</strong><br>
						{dataHoraAlteracao}
					</div>

					<p>Se você realizou essa alteração, nenhuma ação é necessária.</p>

					<div style="background-color: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin-top: 20px;">
						<strong>Não foi você?</strong><br>
						Se você não reconhece essa atividade, entre em contato imediatamente com nosso suporte para proteger sua conta.
					</div>
					""";

			criaLayoutSeNecessario("Aviso de alteração de senha", "contato.smartdesk@gmail.com",
					"Notificação de segurança", gerarHtmlBase("Senha Alterada", conteudo));
		}

		if (!layoutEmailService.existeCriacaoDeSenha()) {
			log.info("Criando layout padrão para criação de senha...");

			String conteudo = """
					<p>Olá <strong>{nomeUsuario}</strong>,</p>
					<p>Seja bem-vindo ao <strong>SmartDesk</strong>! Sua conta foi criada com sucesso.</p>
					<p>Para começar a utilizar o sistema, é necessário definir sua senha de acesso inicial.</p>

					<table role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary">
					  <tbody>
					    <tr>
					      <td align="center">
					        <table role="presentation" border="0" cellpadding="0" cellspacing="0">
					          <tbody>
					            <tr>
					              <td> <a href="%s/definir-senha/{hash}" target="_blank">Definir Minha Senha</a> </td>
					            </tr>
					          </tbody>
					        </table>
					      </td>
					    </tr>
					  </tbody>
					</table>

					<p>Este link é válido por <strong>15 minutos</strong>. Após esse período, será necessário solicitar ao Administrador o envio de um novo link.</p>
					<p>Se você não esperava este e-mail, por favor ignore esta mensagem.</p>
					"""
					.formatted(frontendBaseUrl);

			criaLayoutSeNecessario("Criação de senha de acesso", "contato.smartdesk@gmail.com",
					"Bem-vindo ao SmartDesk - Defina sua senha", gerarHtmlBase("Bem-vindo!", conteudo));
		}
	}

	private String gerarHtmlBase(String tituloCabecalho, String conteudoHtml) {
		return """
				<!doctype html>
				<html>
				  <head>
				    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
				    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				    <title>%s</title>
				    <style>
				      body { background-color: #f6f6f6; font-family: sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%%; -webkit-text-size-adjust: 100%%; }
				      table { border-collapse: separate; width: 100%%; }
				      table td { font-family: sans-serif; font-size: 14px; vertical-align: top; }
				      .body { background-color: #f6f6f6; width: 100%%; }
				      .container { display: block; margin: 0 auto !important; max-width: 580px; padding: 10px; width: 580px; }
				      .content { box-sizing: border-box; display: block; margin: 0 auto; max-width: 580px; padding: 10px; }
				      .main { background: #ffffff; border-radius: 3px; width: 100%%; }
				      .wrapper { box-sizing: border-box; padding: 20px; }
				      .btn { box-sizing: border-box; width: 100%%; }
				      .btn > tbody > tr > td { padding-bottom: 15px; }
				      .btn table { width: auto; }
				      .btn table td { background-color: #ffffff; border-radius: 5px; text-align: center; }
				      .btn a { background-color: #ffffff; border: solid 1px #3498db; border-radius: 5px; box-sizing: border-box; color: #3498db; cursor: pointer; display: inline-block; font-size: 14px; font-weight: bold; margin: 0; padding: 12px 25px; text-decoration: none; text-transform: capitalize; }
				      .btn-primary table td { background-color: #3498db; }
				      .btn-primary a { background-color: #3498db; border-color: #3498db; color: #ffffff; }
				      h1, h2, h3 { color: #222222; font-family: sans-serif; font-weight: 400; line-height: 1.4; margin: 0; margin-bottom: 30px; }
				      h1 { font-size: 24px; font-weight: 300; text-align: center; text-transform: capitalize; }
				      p, ul, ol { font-family: sans-serif; font-size: 14px; margin: 0; margin-bottom: 15px; }
				      a { color: #3498db; text-decoration: underline; }
				      .footer { clear: both; margin-top: 10px; text-align: center; width: 100%%; }
				      .footer td, .footer p, .footer span, .footer a { color: #999999; font-size: 12px; text-align: center; }
				    </style>
				  </head>
				  <body>
				    <table role="presentation" border="0" cellpadding="0" cellspacing="0" class="body">
				      <tr>
				        <td>&nbsp;</td>
				        <td class="container">
				          <div class="content">
				            <div style="text-align: center; margin-bottom: 20px;">
				              <h2 style="color: #3498db; font-weight: bold; margin: 0;">SmartDesk</h2>
				            </div>
				            <table role="presentation" class="main">
				              <tr>
				                <td class="wrapper">
				                  <table role="presentation" border="0" cellpadding="0" cellspacing="0">
				                    <tr>
				                      <td>
				                        <h1>%s</h1>
				                        %s
				                      </td>
				                    </tr>
				                  </table>
				                </td>
				              </tr>
				            </table>
				            <div class="footer">
				              <table role="presentation" border="0" cellpadding="0" cellspacing="0">
				                <tr>
				                  <td class="content-block">
				                    <span class="apple-link">SmartDesk Inc. Todos os direitos reservados.</span>
				                    <br> Não responda a este e-mail.
				                  </td>
				                </tr>
				              </table>
				            </div>
				          </div>
				        </td>
				        <td>&nbsp;</td>
				      </tr>
				    </table>
				  </body>
				</html>
				"""
				.formatted(tituloCabecalho, tituloCabecalho, conteudoHtml);
	}

	private void criaLayoutSeNecessario(String name, String sourceEmail, String subject, String body) {
		LayoutEmailEntity layoutEmailEntity = new LayoutEmailEntity(null, name, sourceEmail, subject, body);
		layoutEmailService.cadastra(layoutEmailEntity);
	}

	private void criaUsuarioSeNecessario(String nome, String email, String telefone, String senha, boolean ativo,
			PerfilEnum perfil) {
		UsuarioEntity usuarioEntity = new UsuarioEntity(null, nome, email, telefone, senha, ativo, perfil, null, null);
		usuarioService.cadastraAdm(usuarioEntity);
	}
}
