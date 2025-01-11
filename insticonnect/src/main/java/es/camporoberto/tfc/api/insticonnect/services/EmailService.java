package es.camporoberto.tfc.api.insticonnect.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;  // Elimina la duplicidad, mantén solo una instancia


    // Inyectar el correo del remitente desde application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = "Verificación de correo electrónico";
            String body = "<p>Gracias por registrarte en InstiConnect. Para completar tu registro y verificar tu correo electrónico, por favor haz clic en el siguiente botón:</p>" +
                    "<a href=\"" + verificationLink + "\" style=\"display:inline-block;padding:10px 20px;background-color:#31EC56;color:#fff;text-decoration:none;border-radius:5px;\">Verificar mi Correo Electrónico</a>";

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Correo de verificación enviado a: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar el correo de verificación a: {}", toEmail, e);
        }
    }


    public void sendPasswordRecoveryEmail(String email, String recoveryCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = "Recuperación de Contraseña - InstiConnect";
            String body = "<p>Hola,</p>" +
                    "<p>Hemos recibido una solicitud para recuperar tu contraseña en InstiConnect. Usa el siguiente código para completar el proceso de recuperación:</p>" +
                    "<h2 style=\"text-align:center;color:#31EC56;\">" + recoveryCode + "</h2>" +
                    "<p>Este código es válido por 10 minutos. Si no solicitaste este cambio, ignora este correo.</p>" +
                    "<p>Atentamente,<br>El equipo de InstiConnect</p>";

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Correo de recuperación de contraseña enviado a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar el correo de recuperación de contraseña a: {}", email, e);
        }
    }

}
