package br.edu.utfpr.tsi.xenon.domain.notification.service;

import br.edu.utfpr.tsi.xenon.domain.notification.model.EmailTemplate;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SenderAdapter {

    private static final String FROM = "noreply@xenon.utfpr.edu.br";
    private final JavaMailSender javaMailSender;

    public void sendEmail(EmailTemplate emailTemplate) {
        log.info("enviando e-mail para: {}", emailTemplate.to());
        try {
            var emailMessage = javaMailSender.createMimeMessage();
            emailMessage.setSubject(emailTemplate.subject(), "UTF-8");

            var helper = new MimeMessageHelper(emailMessage, true, "UTF-8");
            helper.setFrom(FROM);
            helper.setTo(emailTemplate.to());
            helper.setText(emailTemplate.getTemplate(), true);

            javaMailSender.send(emailMessage);
        } catch (MessagingException e) {
            log.error("erro ao enviar e-mail: {}", e.getMessage());
        }
    }
}
