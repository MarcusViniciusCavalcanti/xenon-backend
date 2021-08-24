package br.edu.utfpr.tsi.xenon.application.config.bean;

import br.edu.utfpr.tsi.xenon.application.config.property.EmailProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@RequiredArgsConstructor
public class EmailConfiguration {

    private final EmailProperty emailProperty;

    @Bean
    public JavaMailSender javaMailSender() {
        var emailSender = new JavaMailSenderImpl();

        emailSender.setHost(emailProperty.getHost());
        emailSender.setPort(emailProperty.getPort());
        emailSender.setUsername(emailProperty.getUsername());
        emailSender.setPassword(emailProperty.getPassword());

        var props = emailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", emailProperty.getAuth().toString());
        props.put("mail.smtp.starttls.enable", emailProperty.getTls().getEnable().toString());

        return emailSender;
    }
}
