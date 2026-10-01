package com.upc.invertu.serviciosimpl;

import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.servicios.CorreoService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/** T-35: correos HTML con plantillas Thymeleaf (src/main/resources/templates) */
@Service
public class CorreoServiceImpl implements CorreoService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String remitente;

    @Override
    public void enviarRecuperacion(String correo, String nombres, String enlace, long minutosVigencia) {
        Context contexto = new Context();
        contexto.setVariable("nombres", nombres);
        contexto.setVariable("enlace", enlace);
        contexto.setVariable("minutos", minutosVigencia);
        enviar(correo, "InvertU - Recupera tu contraseña", "correo-recuperacion", contexto);
    }

    // Arma el HTML con la plantilla y lo envia por SMTP; cualquier fallo -> ServicioExternoException
    private void enviar(String destino, String asunto, String plantilla, Context contexto) {
        try {
            String html = templateEngine.process(plantilla, contexto);
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(html, true); // true = HTML
            mailSender.send(mensaje);
        } catch (MessagingException | MailException ex) {
            throw new ServicioExternoException("No se pudo enviar el correo: " + ex.getMessage());
        }
    }
}
