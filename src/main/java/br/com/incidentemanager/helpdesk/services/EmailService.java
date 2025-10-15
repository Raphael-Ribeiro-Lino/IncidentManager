package br.com.incidentemanager.helpdesk.services;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

	private final Session mailSession;

	public EmailService(Session mailSession) {
		this.mailSession = mailSession;
	}

	@Async
	public void enviaEmail(String emailDestino, String layoutName, String sourceEmail, String subject, String body) {
		LayoutEmailEntity layout = new LayoutEmailEntity(null, layoutName, sourceEmail, subject, body);
		try {
			Message message = buildMessage(mailSession, emailDestino, sourceEmail, layout);
			log.info("Enviando e-mail para {}", emailDestino);

			try (Transport transport = mailSession.getTransport("smtp")) {
				transport.connect();
				transport.sendMessage(message, message.getAllRecipients());
			}

			log.info("E-mail enviado com sucesso para {}", emailDestino);

		} catch (MessagingException e) {
			log.error("Erro ao enviar e-mail para {}: {}", emailDestino, e.getMessage());
			throw new BusinessException("Falha ao enviar e-mail. Detalhes: " + e.getMessage());
		}
	}

	private Message buildMessage(Session session, String to, String from, LayoutEmailEntity layout)
			throws MessagingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject(layout.getSubject());
		message.setContent(layout.getBody(), "text/html; charset=utf-8");
		return message;
	}
}
