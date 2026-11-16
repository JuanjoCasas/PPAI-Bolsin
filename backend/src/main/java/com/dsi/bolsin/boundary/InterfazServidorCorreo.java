package com.dsi.bolsin.boundary;

import java.util.ArrayList;
import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * <<boundary>> con el actor secundario "Servidor de Correo" (CU 29 Notificar recepción de bolsín).
 *
 * No es una pantalla: es la interfaz con un sistema externo.
 * - Con SMTP configurado (backend/mail-local.properties), el mail se envía de verdad.
 * - Sin configurar, el envío se simula: el mail se escribe en el log.
 * En los dos casos el mail queda registrado en memoria.
 */
@Component
public class InterfazServidorCorreo {

    private static final Logger log = LoggerFactory.getLogger(InterfazServidorCorreo.class);

    public static final String ENVIADO = "Enviado";
    public static final String SIMULADO = "Simulado (sin servidor de correo configurado)";

    // cuerpo: texto plano; cuerpoHtml: la versión que muestran los clientes de correo
    public record Mail(String destinatario, String asunto, String cuerpo, String cuerpoHtml, String resultado) {
    }

    private final JavaMailSender mailSender;
    private final boolean habilitado;
    private final String remitente;
    private final List<Mail> enviados = new ArrayList<>();

    public InterfazServidorCorreo(ObjectProvider<JavaMailSender> mailSender,
            @Value("${notificaciones.mail.habilitado:false}") boolean habilitado,
            @Value("${spring.mail.username:}") String remitente) {
        this.mailSender = mailSender.getIfAvailable();
        this.habilitado = habilitado && this.mailSender != null;
        this.remitente = remitente;
        log.info("[Servidor de Correo] {}", this.habilitado ? "envío real por SMTP" : "envío simulado (solo log)");
    }

    /**
     * Devuelve el resultado del envío. Si el servidor de correo falla no se lanza excepción:
     * la recepción del bolsín ya quedó registrada y no se debe deshacer por un mail.
     */
    public String enviarMail(String destinatario, String asunto, String cuerpo, String cuerpoHtml) {
        String resultado = habilitado ? enviarPorSmtp(destinatario, asunto, cuerpo, cuerpoHtml) : SIMULADO;
        enviados.add(new Mail(destinatario, asunto, cuerpo, cuerpoHtml, resultado));
        log.info("[Servidor de Correo] {} | Para: {} | Asunto: {}\n{}", resultado, destinatario, asunto, cuerpo);
        return resultado;
    }

    // Mail multiparte: HTML con el texto plano como alternativa
    private String enviarPorSmtp(String destinatario, String asunto, String cuerpo, String cuerpoHtml) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, cuerpoHtml);
            mailSender.send(mensaje);
            return ENVIADO;
        } catch (MailException | MessagingException e) {
            log.error("[Servidor de Correo] No se pudo enviar el mail a {}", destinatario, e);
            return "Error al enviar: " + e.getMessage();
        }
    }

    public List<Mail> getEnviados() {
        return List.copyOf(enviados);
    }
}
