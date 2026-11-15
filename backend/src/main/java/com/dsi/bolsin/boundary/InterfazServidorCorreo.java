package com.dsi.bolsin.boundary;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * <<boundary>> con el actor secundario "Servidor de Correo" (CU 29 Notificar recepción de bolsín).
 *
 * No es una pantalla: es la interfaz con un sistema externo.
 * - Con SMTP configurado (backend/mail-local.properties), el mail se envía de verdad.
 * - Sin configurar, el envío se simula: el mail se escribe en el log.
 * En los dos casos el mail queda registrado en memoria (lo usan los tests).
 */
@Component
public class InterfazServidorCorreo {

    private static final Logger log = LoggerFactory.getLogger(InterfazServidorCorreo.class);

    public static final String ENVIADO = "Enviado";
    public static final String SIMULADO = "Simulado (sin servidor de correo configurado)";

    public record Mail(String destinatario, String asunto, String cuerpo, String resultado) {
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
     * Control -> Interfaz : enviarMail(destinatario, asunto, cuerpo)
     *
     * Devuelve el resultado del envío. Si el servidor de correo falla no se lanza excepción:
     * la recepción del bolsín ya quedó registrada y no se debe deshacer por un mail.
     */
    public String enviarMail(String destinatario, String asunto, String cuerpo) {
        String resultado = habilitado ? enviarPorSmtp(destinatario, asunto, cuerpo) : SIMULADO;
        enviados.add(new Mail(destinatario, asunto, cuerpo, resultado));
        log.info("[Servidor de Correo] {} | Para: {} | Asunto: {}\n{}", resultado, destinatario, asunto, cuerpo);
        return resultado;
    }

    private String enviarPorSmtp(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            mailSender.send(mensaje);
            return ENVIADO;
        } catch (MailException e) {
            log.error("[Servidor de Correo] No se pudo enviar el mail a {}", destinatario, e);
            return "Error al enviar: " + e.getMessage();
        }
    }

    public List<Mail> getEnviados() {
        return List.copyOf(enviados);
    }
}
