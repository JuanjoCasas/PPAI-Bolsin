package com.dsi.bolsin.control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.dsi.bolsin.dto.BolsinResponse;
import com.dsi.bolsin.dto.DetalleBolsinResponse;
import com.dsi.bolsin.dto.DocumentacionResponse;
import com.dsi.bolsin.dto.InicioResponse;
import com.dsi.bolsin.dto.NotificacionResponse;
import com.dsi.bolsin.dto.RemitoResponse;
import com.dsi.bolsin.model.Bolsin;
import com.dsi.bolsin.model.ComisionMedica;
import com.dsi.bolsin.model.Empleado;
import com.dsi.bolsin.model.Estado;
import com.dsi.bolsin.model.Sesion;
import com.dsi.bolsin.model.SituacionRecepcion;
import com.dsi.bolsin.repository.IBolsinRepository;
import com.dsi.bolsin.repository.IEstadoRepository;
import com.dsi.bolsin.repository.ISesionRepository;

/**
 * <<control>> del CU 28 Registrar recepción de bolsín.
 *
 * Coordina el flujo del caso de uso y mantiene el estado de la conversación
 * (bolsín seleccionado, opción elegida, etc.) entre un pedido y el siguiente.
 */
@Service
public class ControllerRegistrarRecepcionBolsin {

    // Opciones de recepción del paso 7 del CU
    public static final String OPCION_CONTENIDO_IGUAL = "El contenido del bolsín es igual al registrado";
    public static final String OPCION_NO_SE_RECIBE_TODA =
            "No se recibe toda la documentación asociada a los remitos que contiene el bolsín";
    public static final String OPCION_NO_CORRESPONDE_DESTINO =
            "Existe documentación que no corresponde al destino (CM del usuario logueado)";
    public static final String OPCION_REDIRIGIR = "La documentación se debe redirigir a otra área";

    private final ISesionRepository sesionRepository;
    private final IBolsinRepository bolsinRepository;
    private final IEstadoRepository estadoRepository;
    private final ControllerNotificarRecepcionBolsin controllerNotificarRecepcionBolsin;
    private final TransactionTemplate transactionTemplate;

    // ---- Atributos del control ----
    private Sesion sesionActiva;
    private Empleado empleadoLogueado;
    private ComisionMedica cmUsuarioLogueado;
    private List<Bolsin> bolsinesEntrantes;
    private Bolsin bolsinSeleccionado;
    private List<RemitoResponse> remitosDelBolsinSeleccionado;
    private List<String> opcionesRecepcionBolsin;
    private String opcionRecepcionBolsinSeleccionada;
    private SituacionRecepcion situacionDocumentacion;     // A3, A4, A5 (null en el flujo básico)
    private List<Integer> documentacionSeleccionada;       // documentación marcada por el EB en A3, A4, A5
    private List<Estado> estados;
    private Estado estadoBolsinRecibidoENCMDestino;
    private Estado estadoRemitoRecibidoYAceptado;
    private Estado estadoRemitoRecibidoYAceptadoParcial;
    private LocalDateTime fechaHoraActual;

    public ControllerRegistrarRecepcionBolsin(ISesionRepository sesionRepository,
            IBolsinRepository bolsinRepository,
            IEstadoRepository estadoRepository,
            ControllerNotificarRecepcionBolsin controllerNotificarRecepcionBolsin,
            PlatformTransactionManager transactionManager) {
        this.sesionRepository = sesionRepository;
        this.bolsinRepository = bolsinRepository;
        this.estadoRepository = estadoRepository;
        this.controllerNotificarRecepcionBolsin = controllerNotificarRecepcionBolsin;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    
    // Boundary -> Control : opcionRegistrarBolsin()
    public InicioResponse opcionRegistrarBolsin() {
        this.sesionActiva = sesionRepository.findAll().stream()
                .filter(Sesion::sosActiva)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay una sesión activa"));
        this.empleadoLogueado = sesionActiva.getEmpleadoLogueado();

        this.cmUsuarioLogueado = getCMUsuarioLogueado();
        String nombreCM = cmUsuarioLogueado.getNombre();

        List<BolsinResponse> bolsines = obtenerBolsinesCMUsuario(); // -> mostrarBolsines()

        return new InicioResponse(nombreCM, bolsines);
    }

    // Sesion -> Usuario -> Empleado -> ComisionMedica
    private ComisionMedica getCMUsuarioLogueado() {
        return sesionActiva.getCMUsuarioLogueado();
    }

    // Loop: para todos los bolsines con destino a la CM del usuario
    private List<BolsinResponse> obtenerBolsinesCMUsuario() {
        this.bolsinesEntrantes = new ArrayList<>();
        List<BolsinResponse> datosBolsines = new ArrayList<>();

        for (Bolsin bolsin : bolsinRepository.findAll()) {
            if (bolsin.esTuCMDestino(cmUsuarioLogueado) && bolsin.sosEnviado()) {
                bolsinesEntrantes.add(bolsin);
                datosBolsines.add(new BolsinResponse(bolsin.getNroPrecinto(), bolsin.getCMOrigen()));
            }
        }
        return datosBolsines;
    }

    // =====================================================================
    // Boundary -> Control : tomarSeleccionBolsin()
    // =====================================================================
    public DetalleBolsinResponse tomarSeleccionBolsin(Integer numeroPrecinto) {
        if (bolsinesEntrantes == null) {
            return null;
        }
        this.bolsinSeleccionado = bolsinesEntrantes.stream()
                .filter(b -> b.getNroPrecinto().equals(numeroPrecinto))
                .findFirst()
                .orElse(null);
        if (bolsinSeleccionado == null) {
            return null;
        }
        limpiarOpcionRecepcion();

        obtenerInformacionRemito(); // -> mostrarDetalleBolsin()

        this.opcionesRecepcionBolsin = List.of(
                OPCION_CONTENIDO_IGUAL,
                OPCION_NO_SE_RECIBE_TODA,
                OPCION_NO_CORRESPONDE_DESTINO,
                OPCION_REDIRIGIR);
        // -> mostrarOpcionesRecepcionBolsin()

        return new DetalleBolsinResponse(
                bolsinSeleccionado.getNroPrecinto(),
                bolsinSeleccionado.getCMOrigen(),
                remitosDelBolsinSeleccionado,
                opcionesRecepcionBolsin);
    }

    /**
     * seleccion:Bolsin.obtenerInformacionRemito() devuelve una fila por documentación:
     * [numeroRemito, numeroDocumentacion, asunto, tipo]. Acá se agrupan por remito para
     * que la pantalla las muestre en la grilla de remitos.
     */
    private void obtenerInformacionRemito() {
        Map<Integer, List<DocumentacionResponse>> docsPorRemito = new LinkedHashMap<>();
        for (String[] fila : bolsinSeleccionado.obtenerInformacionRemito()) {
            Integer numeroRemito = Integer.valueOf(fila[0]);
            docsPorRemito.computeIfAbsent(numeroRemito, k -> new ArrayList<>())
                    .add(new DocumentacionResponse(Integer.valueOf(fila[1]), fila[2], fila[3]));
        }

        this.remitosDelBolsinSeleccionado = new ArrayList<>();
        docsPorRemito.forEach((numero, docs) -> remitosDelBolsinSeleccionado.add(new RemitoResponse(numero, docs)));
    }

    // =====================================================================
    // Boundary -> Control : tomarOpcionRecepcion()
    // =====================================================================
    /**
     * Devuelve true si la opción es un flujo alternativo (A3, A4, A5): en ese caso el EB
     * tiene que indicar a qué documentación del bolsín le corresponde la situación.
     */
    public boolean tomarOpcionRecepcion(String opcion) {
        if (bolsinSeleccionado == null) {
            throw new IllegalStateException("Primero hay que seleccionar un bolsín");
        }
        limpiarOpcionRecepcion();
        if (!opcionesRecepcionBolsin.contains(opcion)) {
            throw new IllegalArgumentException("La opción de recepción seleccionada no es válida");
        }
        // A5: la ME solo permite redirigir cuando la CM destino es la CMC
        if (OPCION_REDIRIGIR.equals(opcion) && !cmUsuarioLogueado.sosCMC()) {
            throw new IllegalArgumentException("Solo la Comisión Médica Central puede redirigir documentación "
                    + "a otra área; " + cmUsuarioLogueado.getNombre() + " no es la CMC");
        }
        this.opcionRecepcionBolsinSeleccionada = opcion;
        this.situacionDocumentacion = getSituacion(opcion);
        return situacionDocumentacion != null;
    }

    // Opción 1 (flujo básico) no tiene situación: toda la documentación se acepta
    private SituacionRecepcion getSituacion(String opcion) {
        return switch (opcion) {
            case OPCION_NO_SE_RECIBE_TODA -> SituacionRecepcion.NO_RECIBIDA;
            case OPCION_NO_CORRESPONDE_DESTINO -> SituacionRecepcion.NO_CORRESPONDE_DESTINO;
            case OPCION_REDIRIGIR -> SituacionRecepcion.PARA_REDIRIGIR;
            default -> null;
        };
    }

    // =====================================================================
    // Boundary -> Control : tomarSeleccionDocumentacion()   (A3, A4, A5)
    // =====================================================================
    public void tomarSeleccionDocumentacion(List<Integer> numerosDocumentacion) {
        if (situacionDocumentacion == null) {
            throw new IllegalStateException("La opción seleccionada no requiere indicar documentación");
        }
        if (numerosDocumentacion == null || numerosDocumentacion.isEmpty()) {
            throw new IllegalArgumentException("Hay que indicar al menos una documentación");
        }
        for (Integer numero : numerosDocumentacion) {
            if (!bolsinSeleccionado.contieneDocumentacion(numero)) {
                throw new IllegalArgumentException("La documentación N° " + numero + " no está en el bolsín");
            }
        }
        this.documentacionSeleccionada = List.copyOf(numerosDocumentacion);
    }

    // Control -> Boundary : pedirConfirmacion()
    public String pedirConfirmacion() {
        if (!validarOpcion()) {
            throw new IllegalStateException("Primero hay que seleccionar un bolsín y la opción de recepción");
        }
        String mensaje = "¿Confirma la recepción del bolsín precinto N° " + bolsinSeleccionado.getNroPrecinto() + "? ";
        if (situacionDocumentacion == null) {
            return mensaje + "Toda su documentación quedará recibida y aceptada.";
        }
        String marcada = "la documentación N° " + documentacionSeleccionada.stream()
                .map(String::valueOf).collect(Collectors.joining(", "));
        String resto = " y el resto, recibida y aceptada.";
        return mensaje + switch (situacionDocumentacion) {
            case NO_RECIBIDA -> marcada + " quedará como no recibida" + resto
                    + " Sus remitos quedarán recibidos y aceptados en forma parcial.";
            case NO_CORRESPONDE_DESTINO -> marcada + " quedará recibida y rechazada" + resto
                    + " Sus remitos quedarán recibidos y aceptados en forma parcial.";
            case PARA_REDIRIGIR -> marcada + " quedará para redirigir a otra área" + resto;
        };
    }

    // Flujo básico: alcanza con la opción. Flujos alternativos: además hace falta la documentación marcada.
    private boolean validarOpcion() {
        return bolsinSeleccionado != null && opcionRecepcionBolsinSeleccionada != null
                && (situacionDocumentacion == null || !documentacionSeleccionada.isEmpty());
    }

    // =====================================================================
    // Boundary -> Control : confirmarRecepcion()   <-- MÉTODO DE ENGANCHE
    // =====================================================================
    public List<NotificacionResponse> confirmarRecepcion() {
        if (!validarOpcion()) {
            throw new IllegalStateException("Primero hay que seleccionar un bolsín y la opción de recepción");
        }
        // La recepción se guarda en una transacción: si algo falla (por ejemplo, una transición
        // inválida del patrón State) se hace rollback y la excepción llega a la boundary.
        transactionTemplate.executeWithoutResult(status -> registrarRecepcion());

        // Recién con la recepción guardada se notifica: si hubo rollback no sale ningún mail.
        List<NotificacionResponse> notificaciones = llamarCu29NotificarRecepcionBolsin();
        // -> informarEjecucionExitosa() (lo hace la boundary con la respuesta HTTP)
        finCU();
        return notificaciones;
    }

    private void registrarRecepcion() {
        this.estados = estadoRepository.findAll();

        // Loop: para todos los estados -> esAmbitoBolsin() / esRecibidoENCMDestino()
        for (Estado estado : estados) {
            if (estado.esAmbitoBolsin() && estado.esRecibidoENCMDestino()) {
                this.estadoBolsinRecibidoENCMDestino = estado;
            }
        }
        // Loop: para todos los estados -> esAmbitoRemito() / esRecibidoYAceptado()
        for (Estado estado : estados) {
            if (estado.esAmbitoRemito() && estado.esRecibidoYAceptado()) {
                this.estadoRemitoRecibidoYAceptado = estado;
            }
        }
        // El loop esAmbitoDocumentacion() / esRecibidaYAceptada() se ELIMINA con el patrón State:
        // el nuevo estado de la documentación lo crea el estado concreto (EnBolsinEnviado).

        this.fechaHoraActual = getFecha();

        // Persistencia: se vuelve a leer el bolsín dentro de la transacción para trabajar sobre
        // datos actualizados. Si algo falla (por ejemplo, una transición inválida), se hace
        // rollback y no queda nada a medio guardar.
        this.bolsinSeleccionado = bolsinRepository.findById(bolsinSeleccionado.getNumeroBolsin())
                .orElseThrow(() -> new IllegalStateException("El bolsín seleccionado ya no existe"));

        if (situacionDocumentacion == null) {
            // Flujo básico
            bolsinSeleccionado.actualizarEstado(estadoBolsinRecibidoENCMDestino, estadoRemitoRecibidoYAceptado,
                    fechaHoraActual, empleadoLogueado);
        } else {
            registrarRecepcionConSituacion();
        }

        bolsinRepository.save(bolsinSeleccionado);
    }

    // Flujos alternativos A3, A4 y A5
    private void registrarRecepcionConSituacion() {
        // Loop: para todos los estados -> esAmbitoRemito() / esRecibidoYAceptadoParcial()
        for (Estado estado : estados) {
            if (estado.esAmbitoRemito() && estado.esRecibidoYAceptadoParcial()) {
                this.estadoRemitoRecibidoYAceptadoParcial = estado;
            }
        }
        bolsinSeleccionado.actualizarEstadoConSituacion(estadoBolsinRecibidoENCMDestino, estadoRemitoRecibidoYAceptado,
                estadoRemitoRecibidoYAceptadoParcial, documentacionSeleccionada, situacionDocumentacion,
                fechaHoraActual, empleadoLogueado);
    }

    private LocalDateTime getFecha() {
        return LocalDateTime.now();
    }

    // <<include>> CU 29 Notificar recepción de bolsín: control a control, sin pasar por una pantalla.
    private List<NotificacionResponse> llamarCu29NotificarRecepcionBolsin() {
        return controllerNotificarRecepcionBolsin.notificarRecepcionBolsin(bolsinSeleccionado);
    }

    private void finCU() {
        this.bolsinSeleccionado = null;
        this.remitosDelBolsinSeleccionado = null;
        limpiarOpcionRecepcion();
    }

    private void limpiarOpcionRecepcion() {
        this.opcionRecepcionBolsinSeleccionada = null;
        this.situacionDocumentacion = null;
        this.documentacionSeleccionada = List.of();
    }
}
