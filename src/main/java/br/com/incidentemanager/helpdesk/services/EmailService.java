package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.exceptions.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void enviaEmail(String emailDestino, String layoutName, String sourceEmail, String subject, String body) {
        log.info("Iniciando envio de e-mail para {}", emailDestino);
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(remetente);
            helper.setTo(emailDestino);
            helper.setSubject(subject);
            helper.setText(body, true);
            javaMailSender.send(message);

            log.info("E-mail enviado com sucesso para {}", emailDestino);

        } catch (MessagingException e) {
            log.error("Erro ao montar e-mail para {}: {}", emailDestino, e.getMessage());
            throw new BusinessException("Falha ao enviar e-mail: " + e.getMessage());
        } catch (Exception e) {
             log.error("Erro genérico no envio: ", e);
             throw new BusinessException("Erro inesperado no servidor de e-mail.");
        }
    }
}