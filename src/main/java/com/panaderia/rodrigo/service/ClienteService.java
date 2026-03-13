package com.panaderia.rodrigo.service;

// ============================================================
//  IMPORTACIONES
// ============================================================
import com.panaderia.rodrigo.model.Cliente;                      // Entidad del dominio
import com.panaderia.rodrigo.repository.ClienteRepository;        // Acceso a la base de datos (capa Repository)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;                    // Marca esta clase como componente de servicio
import org.springframework.transaction.annotation.Transactional;  // Gestión de transacciones de base de datos
import java.util.List;

/**
 * ============================================================
 *  CLASE: ClienteService
 * ============================================================
 *  Esta clase representa la CAPA DE SERVICIO (Service Layer)
 *  dentro de la arquitectura en capas de Spring:
 *
 *  ┌──────────────────────────────────────────┐
 *  │  Controller  →  Service  →  Repository   │
 *  │  (HTTP)         (Negocio)   (Base Datos)  │
 *  └──────────────────────────────────────────┘
 *
 *  La capa de servicio es responsable de:
 *    - Contener la LÓGICA DE NEGOCIO de la aplicación
 *      (validaciones, reglas, cálculos, etc.)
 *    - Coordinar operaciones entre repositorios
 *    - Gestionar las transacciones de base de datos
 *    - Aislar al controlador de los detalles de persistencia
 *
 *  El controlador NO debe contener lógica de negocio.
 *  El repositorio NO debe contener reglas del dominio.
 *  Todo eso vive aquí.
 *
 * ============================================================
 *  @Service
 * ============================================================
 *  Anotación de Spring que:
 *    1. Registra esta clase como un Bean en el contenedor IoC
 *       (Spring la instancia y gestiona su ciclo de vida).
 *    2. Semánticamente indica que es una clase de lógica de negocio.
 *       Funciona igual que @Component, pero con más significado
 *       para quien lee el código.
 *
 * ============================================================
 *  @Transactional
 * ============================================================
 *  Aplica gestión de transacciones a TODOS los métodos de la clase.
 *
 *  ¿Qué es una transacción?
 *  Una transacción es una unidad de trabajo que se ejecuta de forma
 *  ATÓMICA: o todo se completa exitosamente, o nada se persiste.
 *
 *  Ejemplo sin transacción:
 *    1. Se actualiza el nombre del cliente ✅
 *    2. Se actualiza el teléfono → ERROR 💥
 *    Resultado: datos parcialmente actualizados → INCONSISTENCIA
 *
 *  Ejemplo con transacción:
 *    1. Se actualiza el nombre ✅
 *    2. Se actualiza el teléfono → ERROR 💥
 *    Spring hace ROLLBACK automático → base de datos sin cambios ✅
 *
 *  Comportamiento por defecto de @Transactional:
 *    - Hace ROLLBACK ante RuntimeException (y sus subclases)
 *    - Hace COMMIT si el método termina sin excepción
 *    - Para Checked Exceptions NO hace rollback por defecto
 *      (se puede cambiar con rollbackFor = Exception.class)
 *
 *  ✅ BUENA PRÁCTICA:
 *    Para métodos de solo lectura (findAll, findById, etc.),
 *    usar @Transactional(readOnly = true) mejora el rendimiento:
 *    - Hibernate no hace seguimiento de cambios (no dirty checking)
 *    - Algunas BD optimizan queries en modo lectura
 * ============================================================
 */
@Service
@Transactional
public class ClienteService {

    /**
     * INYECCIÓN DE DEPENDENCIA — ClienteRepository
     * ---------------------------------------------
     * ClienteRepository es la capa de acceso a datos.
     * Extiende JpaRepository (o CrudRepository) y hereda
     * automáticamente métodos CRUD: save(), findById(),
     * findAll(), delete(), count(), etc.
     *
     * @Autowired le dice a Spring que inyecte la implementación
     * de ClienteRepository generada automáticamente por Spring Data JPA.
     * (Spring Data JPA crea la implementación en tiempo de ejecución,
     * no necesitas escribir SQL para las operaciones básicas.)
     *
     * ✅ BUENA PRÁCTICA — Inyección por constructor:
     *   private final ClienteRepository clienteRepository;
     *
     *   public ClienteService(ClienteRepository clienteRepository) {
     *       this.clienteRepository = clienteRepository;
     *   }
     *
     *   Ventajas: la dependencia es final (inmutable) y
     *   es más fácil escribir tests unitarios con mocks.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * ============================================================
     *  MÉTODO: findAll()
     * ============================================================
     *  Retorna la lista completa de clientes registrados en la BD.
     *
     *  clienteRepository.findAll()
     *    Método heredado de JpaRepository. Ejecuta internamente:
     *    SELECT * FROM clientes
     *
     *  ⚠️  ALERTA — CARGA MASIVA SIN PAGINACIÓN:
     *  -------------------------------------------------------
     *  Si la tabla de clientes tiene miles de registros, cargar
     *  todos en memoria puede degradar el rendimiento y saturar
     *  el heap de la JVM.
     *
     *  SOLUCIÓN RECOMENDADA para sistemas con muchos datos:
     *    Usar paginación con Pageable:
     *
     *    public Page<Cliente> findAll(Pageable pageable) {
     *        return clienteRepository.findAll(pageable);
     *    }
     *
     *    El controlador pasaría: PageRequest.of(0, 20, Sort.by("nombre"))
     *  -------------------------------------------------------
     *
     *  ✅ MEJORA: Agregar @Transactional(readOnly = true) a este
     *    método ya que solo hace lectura, sin modificar datos.
     * ============================================================
     */
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    /**
     * ============================================================
     *  MÉTODO: findById(Long id)
     * ============================================================
     *  Busca un cliente por su ID. Si no existe, lanza excepción.
     *
     *  clienteRepository.findById(id)
     *    Retorna un Optional<Cliente> (puede estar vacío o tener valor).
     *    Optional es una clase de Java 8+ que evita el NullPointerException:
     *    en lugar de retornar null cuando no hay resultado, retorna
     *    Optional.empty(), lo que obliga al código a manejar ese caso.
     *
     *  .orElseThrow(...)
     *    Si el Optional está vacío (cliente no existe), lanza la
     *    excepción indicada. Si tiene valor, lo retorna directamente.
     *    Es equivalente a:
     *      if (optional.isEmpty()) throw new RuntimeException(...);
     *      return optional.get();
     *
     *  ⚠️  ALERTA — RuntimeException GENÉRICA:
     *  -------------------------------------------------------
     *  Lanzar RuntimeException directamente es funcional pero
     *  no semántico. No distingue "cliente no encontrado" de
     *  otros errores posibles.
     *
     *  SOLUCIÓN:
     *    Crear una excepción personalizada:
     *
     *    public class ClienteNotFoundException extends RuntimeException {
     *        public ClienteNotFoundException(Long id) {
     *            super("Cliente no encontrado con ID: " + id);
     *        }
     *    }
     *
     *    Esto permite al controlador (o @ControllerAdvice) capturar
     *    esta excepción específica y retornar exactamente 404.
     *  -------------------------------------------------------
     *
     *  ✅ MEJORA: @Transactional(readOnly = true)
     * ============================================================
     */
    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    /**
     * ============================================================
     *  MÉTODO: findByEmail(String email)
     * ============================================================
     *  Busca un cliente por su email. Si no existe, lanza excepción.
     *
     *  clienteRepository.findByEmail(email)
     *    Este método NO es de JpaRepository — es un método personalizado
     *    definido en ClienteRepository usando la convención de nombres
     *    de Spring Data JPA (Query Methods).
     *
     *    Spring Data JPA lee el nombre del método y genera el SQL:
     *      findByEmail → SELECT * FROM clientes WHERE email = ?
     *
     *    Retorna Optional<Cliente>, igual que findById().
     *
     *  ⚠️  NOTA — Este método existe en el servicio pero ningún
     *    controlador REST lo usa directamente en el código visto.
     *    Podría estar usado internamente o estar preparado para
     *    funcionalidades futuras (ej: login por email, recuperación
     *    de contraseña).
     *
     *  ✅ MEJORA: @Transactional(readOnly = true)
     * ============================================================
     */
    public Cliente findByEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + email));
    }

    /**
     * ============================================================
     *  MÉTODO: buscarPorNombre(String nombre)
     * ============================================================
     *  Busca clientes cuyo nombre CONTENGA la cadena indicada,
     *  sin distinción de mayúsculas y minúsculas.
     *
     *  clienteRepository.findByNombreContainingIgnoreCase(nombre)
     *    Otro Query Method de Spring Data JPA. El nombre del método
     *    se descompone así:
     *      findBy          → SELECT ... WHERE
     *      Nombre          → columna "nombre"
     *      Containing      → LIKE '%valor%'
     *      IgnoreCase      → LOWER(nombre) = LOWER(?)
     *
     *    SQL generado aproximado:
     *      SELECT * FROM clientes
     *      WHERE LOWER(nombre) LIKE LOWER(CONCAT('%', ?, '%'))
     *
     *  Esto permite búsquedas como:
     *    buscarPorNombre("juan") → encuentra "Juan", "JUAN", "Juanita"
     *
     *  ✅ BUENA PRÁCTICA:
     *    Retorna List<Cliente> (puede estar vacía), no lanza excepción
     *    si no hay resultados. Es el comportamiento correcto para
     *    búsquedas: "no encontré nada" ≠ "ocurrió un error".
     *
     *  ⚠️  ALERTA — RENDIMIENTO CON LIKE '%valor%':
     *  -------------------------------------------------------
     *  Las búsquedas con LIKE que comienzan con % (wildcard al inicio)
     *  no pueden usar índices de la BD, lo que puede ser lento
     *  con tablas grandes.
     *
     *  SOLUCIÓN para sistemas con muchos datos:
     *    Considerar motores de búsqueda full-text como
     *    Hibernate Search + Lucene/Elasticsearch.
     *  -------------------------------------------------------
     *
     *  ✅ MEJORA: @Transactional(readOnly = true)
     * ============================================================
     */
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * ============================================================
     *  MÉTODO: save(Cliente cliente)
     * ============================================================
     *  Persiste un NUEVO cliente en la base de datos.
     *  Incluye validación de negocio: email único.
     *
     *  FLUJO DEL MÉTODO:
     *    1. Verificar si ya existe un cliente con ese email
     *    2. Si existe → lanzar excepción (no se guarda)
     *    3. Si no existe → guardar y retornar el cliente con ID asignado
     *
     *  clienteRepository.existsByEmail(cliente.getEmail())
     *    Query Method que genera:
     *      SELECT COUNT(*) > 0 FROM clientes WHERE email = ?
     *    Retorna boolean: true si ya existe ese email.
     *
     *  ✅ BUENA PRÁCTICA — Validación de unicidad en el servicio:
     *    La validación del email duplicado está en la capa correcta
     *    (servicio = lógica de negocio). No en el controlador ni en
     *    el repositorio.
     *
     *  clienteRepository.save(cliente)
     *    Como el cliente es nuevo (id = null), JPA ejecuta un INSERT.
     *    El objeto retornado tiene el ID asignado por la base de datos.
     *    Por eso se retorna el objeto guardado y no el que llegó como parámetro.
     *
     *  ⚠️  ALERTA — RACE CONDITION (Condición de carrera):
     *  -------------------------------------------------------
     *  El patrón "verificar existencia → guardar" tiene un problema
     *  en entornos con múltiples usuarios concurrentes:
     *
     *    Hilo A: existsByEmail("x@x.com") → false
     *    Hilo B: existsByEmail("x@x.com") → false (¡ambos ven que no existe!)
     *    Hilo A: save() → INSERT exitoso ✅
     *    Hilo B: save() → INSERT duplicado 💥 (viola constraint UNIQUE en BD)
     *
     *  SOLUCIÓN:
     *    Agregar una restricción UNIQUE en la columna email a nivel
     *    de base de datos (en la entidad con @Column(unique = true))
     *    y capturar la DataIntegrityViolationException en el servicio
     *    para retornar un mensaje amigable.
     *  -------------------------------------------------------
     * ============================================================
     */
    public Cliente save(Cliente cliente) {
        // Regla de negocio: el email debe ser único en el sistema
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con el email: " + cliente.getEmail());
        }
        // Guarda en BD y retorna el cliente con el ID auto-generado
        return clienteRepository.save(cliente);
    }

    /**
     * ============================================================
     *  MÉTODO: update(Long id, Cliente clienteActualizado)
     * ============================================================
     *  Actualiza los datos de un cliente existente.
     *
     *  FLUJO DEL MÉTODO:
     *    1. Buscar el cliente existente en la BD (findById)
     *       → Si no existe, findById() lanza RuntimeException
     *    2. Actualizar solo los campos permitidos
     *    3. Guardar los cambios
     *
     *  ✅ PATRÓN CORRECTO DE ACTUALIZACIÓN:
     *  -------------------------------------------------------
     *  Se carga el objeto existente desde la BD y se modifican
     *  solo los campos necesarios (nombre, teléfono, dirección).
     *  NO se reemplaza el objeto completo.
     *
     *  Esto evita sobrescribir accidentalmente campos que no
     *  deben cambiar (como el email o el ID), incluso si el
     *  cliente enviara esos campos en el JSON del request.
     *  -------------------------------------------------------
     *
     *  ⚠️  NOTA — Email no se puede actualizar:
     *    El email no está incluido en el update.
     *    Esto puede ser una decisión de negocio (el email es el
     *    identificador único del cliente) o una omisión.
     *    Si se necesita cambiar el email, debería hacerse en un
     *    método separado con su propia validación de unicidad.
     *
     *  clienteRepository.save(existente)
     *    Como el objeto ya tiene un ID asignado (fue cargado desde BD),
     *    JPA detecta que existe y ejecuta un UPDATE, no un INSERT.
     *    Comportamiento de JPA:
     *      id = null  → INSERT (nuevo registro)
     *      id = valor → UPDATE (actualiza el existente)
     *
     *  ✅ OPTIMIZACIÓN con @Transactional:
     *    Con @Transactional activo, en realidad no sería necesario
     *    llamar a clienteRepository.save(existente) explícitamente.
     *    Hibernate tiene "dirty checking": detecta que el objeto
     *    "existente" fue modificado dentro de la transacción y
     *    genera el UPDATE automáticamente al finalizar el método.
     *    Llamar a save() igual es válido y no genera doble UPDATE.
     * ============================================================
     */
    public Cliente update(Long id, Cliente clienteActualizado) {
        // Carga el cliente actual desde la BD (lanza excepción si no existe)
        Cliente existente = findById(id);

        // Solo se actualizan los campos permitidos, no el email ni el ID
        existente.setNombre(clienteActualizado.getNombre());
        existente.setTelefono(clienteActualizado.getTelefono());
        existente.setDireccion(clienteActualizado.getDireccion());

        // Persiste los cambios (JPA ejecutará un UPDATE)
        return clienteRepository.save(existente);
    }

    /**
     * ============================================================
     *  MÉTODO: delete(Long id)
     * ============================================================
     *  Elimina un cliente de la base de datos por su ID.
     *
     *  FLUJO DEL MÉTODO:
     *    1. Buscar el cliente (findById) → si no existe, lanza excepción
     *    2. Eliminar el objeto encontrado
     *
     *  clienteRepository.delete(cliente)
     *    Recibe el objeto entero (no solo el ID).
     *    Equivale a: DELETE FROM clientes WHERE id = ?
     *
     *  ✅ VENTAJA DE ESTE PATRÓN:
     *    Al llamar primero a findById(), si el cliente no existe,
     *    se lanza RuntimeException ANTES de intentar el DELETE.
     *    Esto garantiza un mensaje de error descriptivo ("no encontrado")
     *    en lugar de que la BD simplemente no elimine nada sin avisar.
     *
     *    Alternativa directa: clienteRepository.deleteById(id)
     *    Lanza EmptyResultDataAccessException si no existe, que
     *    Spring Data maneja automáticamente.
     *
     *  ⚠️  ALERTA — INTEGRIDAD REFERENCIAL:
     *  -------------------------------------------------------
     *  Si Cliente tiene relación con Pedido (un cliente puede
     *  tener muchos pedidos), eliminar el cliente puede:
     *    a) Fallar con error de FK si la BD tiene constraint.
     *    b) Eliminar en cascada todos sus pedidos si se configuró
     *       CascadeType.ALL o CASCADE DELETE en la BD.
     *
     *  Ninguna de estas situaciones se maneja aquí explícitamente.
     *
     *  SOLUCIÓN RECOMENDADA:
     *    Verificar si el cliente tiene pedidos activos antes de
     *    eliminarlo, y retornar un error descriptivo si los tiene.
     *    O bien, implementar soft delete (campo "activo = false").
     *  -------------------------------------------------------
     * ============================================================
     */
    public void delete(Long id) {
        // Primero verifica que el cliente existe (lanza excepción si no)
        Cliente cliente = findById(id);
        // Luego lo elimina físicamente de la base de datos
        clienteRepository.delete(cliente);
    }

    /**
     * ============================================================
     *  MÉTODO: count()
     * ============================================================
     *  Retorna el total de clientes registrados en la base de datos.
     *
     *  clienteRepository.count()
     *    Método heredado de JpaRepository. Ejecuta:
     *      SELECT COUNT(*) FROM clientes
     *
     *  Retorna long (primitivo), no Long (wrapper), lo que es
     *  correcto ya que COUNT siempre retorna un número (nunca null).
     *
     *  USO TÍPICO:
     *    - Mostrar estadísticas en el dashboard del ADMIN
     *      ("Total de clientes registrados: 247")
     *    - Validaciones de negocio ("¿Hay al menos un cliente?")
     *    - Paginación ("¿Cuántas páginas necesito?")
     *
     *  ✅ MEJORA: @Transactional(readOnly = true)
     *    Este método solo lee datos, no los modifica.
     *    Marcarlo como readOnly mejora el rendimiento.
     * ============================================================
     */
    public long count() {
        return clienteRepository.count();
    }
}