# PPAI 2026 - CU 28 Registrar recepción de bolsín

- `doc/` — análisis (entrega 1) y diseño con patrón State (entrega 2), en PlantUML.
- `backend/` — implementación en Java: Spring Boot + JPA + H2 en memoria (entrega 3).
- `front/` — pantallas en React + Vite + Material UI.

Se tomó como base el ejemplo `java-persistencia` + `front` del repo de la cátedra.

## Cómo correrlo

Requisitos: Java 17+, Maven 3.9+, Node 18+.

```bash
# Terminal 1: backend en http://localhost:8080
cd backend
mvn spring-boot:run

# Terminal 2: front en http://localhost:3000
cd front
npm install
npm run dev
```

- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bolsinesdb`, usuario `sa`, sin contraseña).
- Tests de integración del CU: `cd backend && mvn test`.
- La base se crea de cero en cada arranque con `backend/src/main/resources/data.sql`.

## Datos de prueba

Usuario logueado: Juan Pérez, Encargado de Bolsines de la Comisión Médica Córdoba.
Los nombres de estados, actores y tipos de documento siguen la ERS.

| Precinto | Origen → Destino | Estado | Qué pasa |
|---|---|---|---|
| 50001 | Rosario → Córdoba | Enviado | Flujo básico: 2 remitos, 3 documentaciones |
| 50002 | Mendoza → Córdoba | Enviado | Flujo básico: 1 remito, 1 documentación |
| 50003 | Rosario → Córdoba | Enviado | Su documentación quedó en `EnBolsinSaliente`: el patrón State rechaza `aceptar()` y se hace rollback |
| 50004 | Córdoba → Rosario | Enviado | No aparece (otra CM destino) |
| 50005 | Mendoza → Córdoba | Cerrado | No aparece (todavía no fue enviado) |
| 50006 | Rosario → Córdoba | Enviado | Para A3: 2 remitos, 3 documentaciones |
| 50007 | Mendoza → Córdoba | Enviado | Para A4: 1 remito, 2 documentaciones |
| 50008 | Rosario → Central | Enviado | Para A5: solo lo ve un EB de la CMC (usuario `alopez`) |

Alcance: se implementan el flujo básico y todos los flujos alternativos del CU 28, más cancelar el CU (Obs. 1):

| Flujo | Qué hace el EB | Resultado |
|---|---|---|
| Básico (opción 1) | Indica que el contenido es igual al registrado | Toda la documentación queda `RecibidaYAceptada`; los remitos, *Recibido y Aceptado* |
| A1, A2 | Filtra por precinto o CM origen sin resultados | La pantalla informa que no se encontró un bolsín con ese dato |
| A3 (opción 2) | Marca la documentación que no llegó | La marcada queda `NoRecibida`; su remito, *Recibido y aceptado parcial* |
| A4 (opción 3) | Marca la documentación que no corresponde al destino | La marcada queda `RecibidaYRechazada`; su remito, *Recibido y aceptado parcial* |
| A5 (opción 4) | Marca la documentación a redirigir a otra área | La marcada queda `ParaRedirigir`; el remito, *Recibido y Aceptado*. Solo la CMC puede elegir esta opción |
| A6 | No confirma | No se registra nada |

En A3, A4 y A5 la documentación que no se marca queda recibida y aceptada, y el bolsín pasa a *Recibido en CM
destino* igual que en el flujo básico. El mail del CU 29 informa el estado en que quedó cada documentación.

Para probar A5 desde la pantalla hay que loguearse como el EB de la CMC: en la consola H2
(`/h2-console`, JDBC URL `jdbc:h2:mem:bolsinesdb`) ejecutar
`UPDATE sesiones SET fecha_fin = CURRENT_TIMESTAMP WHERE id = 2; UPDATE sesiones SET fecha_fin = NULL WHERE id = 3;`
y volver a entrar al CU.

El `<<include>>` al CU 29 *Notificar recepción de bolsín* va de control a control: el controller del CU 28 llama a
`ControllerNotificarRecepcionBolsin`, que manda un mail a cada CM de origen de la documentación con la situación
de cada una. El mail sale por `InterfazServidorCorreo` (`<<boundary>>` con el actor Servidor de Correo). Se notifica
recién después de guardar la recepción: si hay rollback no sale ningún mail, y si falla el envío la recepción no se
deshace (el paso 3 muestra el error).

### Envío real de mails (opcional)

Sin configurar nada, los mails se simulan y se escriben en el log del backend. Para enviarlos de verdad por Gmail:

1. Copiar `backend/mail-local.properties.example` como `backend/mail-local.properties`.
2. Reemplazar `CONTRASEÑA_DE_APLICACION` por la contraseña de aplicación de la cuenta `ppaibolsines@gmail.com`
   (Gmail no acepta la contraseña normal por SMTP).
3. Reiniciar el backend. En el log aparece `[Servidor de Correo] envío real por SMTP`.

`mail-local.properties` está en `.gitignore`: **la contraseña no se sube al repo**. Las CM de `data.sql` usan
direcciones `ppaibolsines+<cm>@gmail.com`, así que todos los mails llegan a la bandeja de esa cuenta.
Los tests nunca envían mails reales.

## Cómo se ven los diagramas en el código

| Diagrama | Código |
|---|---|
| `<<boundary>>` BoundaryRegistrarRecepcionBolsin | `boundary/BoundaryRegistrarRecepcionBolsin.java` (un endpoint por mensaje del actor) + `front/` |
| `<<control>>` ControllerRegistrarRecepcionBolsin | `control/ControllerRegistrarRecepcionBolsin.java` |
| `<<include>>` CU 29 Notificar recepción de bolsín | `control/ControllerNotificarRecepcionBolsin.java` + `boundary/InterfazServidorCorreo.java` |
| `<<entity>>` | `model/*.java` (entidades JPA) |
| Patrón State: contexto | `model/Documentacion.java` |
| Patrón State: estado abstracto y concretos | `model/estado/*.java` |
| `mostrarX()`, `pedirConfirmacion()`, `informarEjecucionExitosa()` | La respuesta JSON de cada endpoint, dibujada por el front |

Endpoints: `POST /bolsines/iniciar`, `/seleccionar-bolsin`, `/seleccionar-opcion`, `/confirmar`.
