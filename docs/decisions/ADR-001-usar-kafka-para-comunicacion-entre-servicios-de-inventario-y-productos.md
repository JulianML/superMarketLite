# ADR-001: Usar Kafka para comunicación entre servicios de inventario y productos

- **Estado:** Aceptada
- **Fecha:** 2026-05-11
- **Autor:** julian

## Contexto

El proyecto **market** tiene dos dominios bien diferenciados que necesitan coordinarse:
- **Productos**: gestiona el catálogo (creación, actualización, eliminación, importación CSV).
- **Inventario**: gestiona el stock por negocio y producto, con movimientos auditados y bloqueo optimista.

Ambos dominios conviven en el mismo monolito Spring Boot (`com.example.demo`), pero están separados en paquetes independientes. Cuando un producto cambia (se crea, actualiza o elimina), el módulo de inventario debe poder reaccionar a ese cambio de forma desacoplada y sin crear dependencias directas entre servicios.

Además, el campo `sourceSystem` en `ProductEvent` (valor `CSV_IMPORT`) indica que hay al menos un origen externo de datos, lo que refuerza la necesidad de un bus de eventos que permita múltiples productores y consumidores sin acoplamiento.

## Decisión

Usar **Apache Kafka** como bus de eventos asíncronos entre el módulo de productos y el de inventario. El módulo de productos publica eventos `ProductEvent` en el topic `product-events` ante cualquier cambio (CREATE, UPDATE, DELETE). El módulo de inventario consume ese topic para reaccionar en consecuencia.

La instancia de Kafka corre como contenedor Docker junto a Zookeeper, usando la imagen `confluentinc/cp-kafka:7.5.0`. El topic usa replication factor 1 (entorno de desarrollo).

## Alternativas consideradas

- **Llamadas directas entre servicios (acoplamiento fuerte):** `ProductService` llamaría directamente a `InventoryService`. Se descartó porque crea una dependencia en tiempo de compilación entre módulos, dificulta la futura separación en microservicios y bloquea la operación de productos si inventario falla.
- **Base de datos compartida como mecanismo de coordinación (polling):** Inventario consultaría periódicamente cambios en la tabla de productos. Se descartó por latencia, carga innecesaria en la BD y ausencia de historial de eventos.
- **Spring ApplicationEvents (eventos en memoria):** Apropiado para un monolito pequeño, pero no sobrevive un reinicio y no escala si los módulos se separan en el futuro.

## Consecuencias

### Positivas
- Los módulos de productos e inventario están desacoplados: pueden evolucionar de forma independiente.
- El historial de eventos `ProductEvent` queda en Kafka, lo que facilita auditoría y replay.
- Si en el futuro se extraen microservicios, la infraestructura de mensajería ya está en su sitio.
- El campo `sourceSystem` permite identificar el origen de cada cambio (API REST, importación CSV, etc.).

### Negativas / Riesgos
- **Complejidad operativa:** Kafka + Zookeeper añaden dos contenedores más al entorno de desarrollo. El setup es más pesado que un monolito sin mensajería.
- **Consistencia eventual:** El inventario no se actualiza de forma síncrona. Si el consumidor falla, puede quedar desfasado temporalmente.
- **Consumer actual solo loguea:** `KafkaCostumerService` aún no implementa la lógica real de sincronización de inventario — esto es deuda técnica pendiente.
- **Single broker / replication factor 1:** La configuración actual no es tolerante a fallos. Para producción se necesitaría un clúster multi-broker.

## Referencias
- `back/src/main/java/com/example/demo/common/kafka/KafkaProducerService.java`
- `back/src/main/java/com/example/demo/common/kafka/KafkaCostumerService.java`
- `back/src/main/java/com/example/demo/product/dto/ProductEvent.java`
- `back/docker-compose.yaml` — configuración de la infraestructura Kafka/Zookeeper
