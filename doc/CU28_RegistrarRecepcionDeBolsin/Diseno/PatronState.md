# CU 28 Registrar recepción de bolsín — Rediseño con patrón State

## 1. Identificación del patrón

**Patrón:** State (GoF). Es un patrón de comportamiento y de ámbito de objeto.

**Clase rediseñada:** `Documentacion`, cuyo comportamiento depende de su estado según la Máquina de Estados de Documentación.

**Paso del CU que se rediseña:** paso 11 del flujo básico. Ahí el sistema actualiza la documentación asociada al estado *Recibida y Aceptada* y registra el empleado responsable.

### 1.a Problema y justificación

La documentación tiene 9 estados: Registrada, EnRemito, EnBolsinSaliente, EnBolsinEnviado, NoRecibida, RecibidaYRechazada, ParaRedirigir, RecibidaYAceptada y DeBaja. Además tiene 12 métodos de transición: `incluirEnRemito()`, `cancelarRemito()`, `agregarABolsin()`, `modificarBolsin()`, `quitarDeBolsin()`, `retirarBolsin()`, `aceptar()`, `docNoRecibida()`, `recibirYRechazar()`, `redirigir()`, `reenviarDocumentacion()` y `eliminarDocumentacion()`.

El modelo de análisis tiene dos problemas:

- **Condicionales por estado.** Cada método de `Documentacion` tendría que preguntar en qué estado está para decidir si la transición vale y a qué estado pasar. Por ejemplo, `aceptar()` solo es válido desde *EnBolsinEnviado* (CU 28) o desde *ParaRedirigir* (CU 31). Desde cualquier otro estado tiene que rechazarse. Con 9 estados y 12 operaciones, eso termina en estructuras *if/switch* grandes y repetidas en cada método.
- **El control busca el estado destino.** El controlador recorre todos los estados preguntando `esAmbitoDocumentacion()` y `esRecibidaYAceptada()` para conocer el estado al que tiene que pasar la documentación. Así, conocer las transiciones de la máquina de estados queda como responsabilidad del controlador y no de la documentación.

Además, el dominio todavía puede crecer: la ERS ya contempla los flujos alternativos de recepción (no recibida, no corresponde al destino, para redirigir) y la revisión de documentación (CU 31). Si agregamos un estado o una transición al diseño sin el patrón, hay que modificar los condicionales de todos los métodos.

**Solución:** cada estado de la documentación se encapsula en una clase propia que hereda de una clase abstracta `EstadoDocumentacion`. La documentación (contexto) le delega la operación a su estado actual. Cada estado concreto sabe qué transiciones son válidas desde él, cuál es el estado siguiente, y cómo cerrar el cambio de estado actual y registrar el nuevo.

**Consecuencias:**
- Se eliminan los condicionales por estado: la decisión se resuelve por **polimorfismo**.
- Se cumple el principio **Abierto/Cerrado** (SOLID): para agregar un estado nuevo se agrega una subclase sin modificar las existentes.
- El controlador ya no busca el estado destino de la documentación: se elimina el loop `esAmbitoDocumentacion()` / `esRecibidaYAceptada()`.
- Como contrapartida, aumenta la cantidad de clases (una por estado).

## 2. Vista estática

Ver [VistaClasesDiseno.puml](VistaClasesDiseno.puml).

## 3. Vista dinámica

Ver [diagramaSecuenciaDiseno.puml](diagramaSecuenciaDiseno.puml).

- **Método de enganche:** `confirmarRecepcion()`, de la boundary al controlador.
- **Delegación:** `Documentacion.aceptar(fechaHoraActual, responsable)` le delega a su estado actual `:EnBolsinEnviado` con `aceptar(this, fechaHoraActual, responsable, historiales)`.
- **Comportamiento delegado asociado al momento:** el estado busca el cambio de estado actual (`sosActual()`) y lo cierra (`setFechaHoraFin()`). Después crea el nuevo estado `RecibidaYAceptada` y el nuevo `CambioEstadoDocumentacion` con la fecha y hora de inicio, el estado y el empleado responsable.
- **Transición de estado:** el estado le indica a la documentación su nuevo estado (`setEstado()`) y le agrega el nuevo cambio de estado (`agregarCambioEstado()`).

## Explicación de las clases del patrón

### Documentacion — Clase concreta · Contexto
Es el contexto del patrón: su comportamiento cambia según el estado en el que está. Conoce su estado actual (`estadoActual`) y su historial de cambios de estado (`cambioEstado`). Cuando la `DetalleRemito` le indica que se acepta, con el mensaje `aceptar(fechaHoraActual: DateTime, responsable: Empleado)`, delega esa responsabilidad a su estado actual, que en tiempo de ejecución es *EnBolsinEnviado*. Al delegar se envía a sí misma (`this`) junto con la fecha y hora actual, el empleado responsable y sus historiales. También expone los modificadores `setEstado()` y `agregarCambioEstado()` para que el estado pueda hacer la transición.

### EstadoDocumentacion — Clase abstracta · Estado
Declara todos los métodos de la máquina de estados de Documentación. Su método `aceptar(this: Documentacion, fechaHoraActual: DateTime, responsable: Empleado, historiales: CambioEstadoDocumentacion[])` es **polimórfico**: cada clase hija le da su propia implementación según el comportamiento de ese estado. Recibe:
- la documentación, para indicarle el cambio de estado;
- la fecha y hora actual y el responsable, para registrar el nuevo cambio de estado;
- los historiales, para que el estado actual pueda finalizar el cambio de estado vigente.

Se usa **herencia** y no realización porque se reutiliza código: la clase abstracta guarda los atributos comunes (`nombre`, `descripcion`) y una implementación por defecto de cada transición que la rechaza. Así, cada estado concreto sobrescribe solo las transiciones válidas desde él.

Se llama `EstadoDocumentacion`, y no `Estado`, para no confundirla con la clase `Estado` del análisis. Esa clase se sigue usando, sin cambios, para los estados de Bolsín y Remito.

### EnBolsinEnviado — Clase concreta · Estado concreto
Hereda de `EstadoDocumentacion` y le da implementación a `aceptar()`:
1. Busca, entre los historiales, el cambio de estado actual (`sosActual()`) y le registra la fecha y hora de fin.
2. Crea el nuevo estado `RecibidaYAceptada`.
3. Crea el nuevo `CambioEstadoDocumentacion` con la fecha y hora de inicio, el nuevo estado y el empleado responsable.
4. Por último, le indica a la documentación que tiene un nuevo estado y que agregue a su colección el nuevo cambio de estado.

También implementa `docNoRecibida()`, `recibirYRechazar()` y `redirigir()`, que son las demás transiciones que salen de este estado según la ME y corresponden a los flujos alternativos del CU 28 y al CU 31.

### Demás estados concretos
`Registrada`, `EnRemito`, `EnBolsinSaliente`, `NoRecibida`, `RecibidaYRechazada`, `ParaRedirigir`, `RecibidaYAceptada` y `DeBaja` heredan de `EstadoDocumentacion` y sobrescriben solo las transiciones que salen de cada uno según la máquina de estados. `RecibidaYAceptada` y `DeBaja` son estados finales, así que no sobrescriben ninguna transición.
