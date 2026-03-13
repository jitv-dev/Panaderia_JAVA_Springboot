package com.panaderia.rodrigo.rest;

// ============================================================
//  IMPORTACIONES
//  Se importan los componentes necesarios para construir
//  un controlador REST con validación y seguridad.
// ============================================================
import com.panaderia.rodrigo.model.Cliente;           // Entidad/modelo del dominio
import com.panaderia.rodrigo.service.ClienteService;  // Capa de servicio (lógica de negocio)
import jakarta.validation.Valid;                       // Para activar validaciones del modelo (@NotNull, @Size, etc.)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;            // Códigos de estado HTTP (200, 201, 404, etc.)
import org.springframework.http.ResponseEntity;        // Respuesta HTTP completa (cuerpo + código de estado + headers)
import org.springframework.security.access.prepost.PreAuthorize; // Seguridad a nivel de método
import org.springframework.web.bind.annotation.*;      // Anotaciones REST: @GetMapping, @PostMapping, etc.
import java.util.List;
import java.util.Map;                                  // Para construir respuestas de error/éxito como JSON simple

/**
 * ============================================================
 *  CLASE: ClienteRestController
 * ============================================================
 *  Controlador REST que expone los endpoints de la API para
 *  gestionar clientes de la panadería.
 *
 *  DIFERENCIA CLAVE entre @Controller y @RestController:
 *  -------------------------------------------------------
 *  @Controller → retorna vistas (archivos .html / .jsp / Thymeleaf)
 *  @RestController → retorna datos serializados en JSON (o XML).
 *    Equivale a @Controller + @ResponseBody en cada método.
 *
 *  Es decir, cada método de esta clase devolverá JSON automáticamente,
 *  sin necesidad de un archivo de vista.
 *
 *  @RequestMapping("/api/clientes")
 *  Define la URL base para TODOS los endpoints de esta clase.
 *  Todos los métodos heredan este prefijo. Ejemplo:
 *    @GetMapping       → GET  /api/clientes
 *    @GetMapping("/{id}") → GET  /api/clientes/5
 *    @PostMapping      → POST /api/clientes
 * ============================================================
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    /**
     * INYECCIÓN DE DEPENDENCIA — ClienteService
     * ------------------------------------------
     * @Autowired le indica a Spring que inyecte automáticamente
     * una instancia de ClienteService en esta variable.
     *
     * ClienteService es la capa de servicio, que contiene la
     * lógica de negocio (validaciones, reglas, etc.).
     * El controlador NO debe contener lógica de negocio directamente;
     * su único rol es recibir la petición HTTP y delegar al servicio.
     *
     * ✅ BUENA PRÁCTICA:
     * Se recomienda inyección por constructor en lugar de @Autowired
     * en el campo, ya que facilita las pruebas unitarias (testing).
     * Ejemplo:
     *
     *   private final ClienteService clienteService;
     *
     *   public ClienteRestController(ClienteService clienteService) {
     *       this.clienteService = clienteService;
     *   }
     */
    @Autowired
    private ClienteService clienteService;

    /**
     * ============================================================
     *  ENDPOINT: GET /api/clientes
     *  (Con soporte para búsqueda opcional por nombre)
     * ============================================================
     *  Retorna la lista de todos los clientes, o filtra por nombre
     *  si se proporciona el parámetro de búsqueda.
     *
     *  Ejemplos de uso:
     *    GET /api/clientes             → devuelve todos los clientes
     *    GET /api/clientes?buscar=Juan → filtra clientes por nombre
     *
     *  @GetMapping
     *    Mapea las peticiones HTTP GET a este método.
     *
     *  @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
     *    Seguridad a nivel de MÉTODO (requiere @EnableMethodSecurity en SecurityConfig).
     *    Solo usuarios con rol ADMIN o EMPLEADO pueden ejecutar este método.
     *    Si un usuario sin rol intenta acceder, Spring Security retorna 403 Forbidden.
     *
     *  @RequestParam(required = false) String buscar
     *    Lee el parámetro "buscar" de la URL (?buscar=...).
     *    required = false → el parámetro es OPCIONAL.
     *    Si no se envía, "buscar" será null.
     *
     *  ResponseEntity<List<Cliente>>
     *    Retorna una respuesta HTTP completa:
     *      - Cuerpo: lista de objetos Cliente serializados a JSON
     *      - Código de estado: 200 OK
     * ============================================================
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<Cliente>> getAll(
            @RequestParam(required = false) String buscar) {

        // Si se proporcionó el parámetro "buscar", se filtra por nombre
        if (buscar != null) {
            return ResponseEntity.ok(clienteService.buscarPorNombre(buscar));
        }
        // Sin parámetro, retorna todos los clientes
        return ResponseEntity.ok(clienteService.findAll());
    }

    /**
     * ============================================================
     *  ENDPOINT: GET /api/clientes/{id}
     * ============================================================
     *  Retorna un cliente específico por su ID.
     *
     *  Ejemplo: GET /api/clientes/5 → busca el cliente con id = 5
     *
     *  @PathVariable Long id
     *    Extrae el valor "{id}" de la URL y lo convierte a Long.
     *    Spring realiza la conversión de tipo automáticamente.
     *
     *  Manejo de errores con try-catch:
     *    - Si el cliente existe → 200 OK + objeto Cliente en JSON
     *    - Si NO existe → el servicio lanza RuntimeException
     *                   → se captura y retorna 404 Not Found
     *
     *  ⚠️  ALERTA — MANEJO DE ERRORES GENÉRICO:
     *  -------------------------------------------------------
     *  Capturar RuntimeException (clase padre) es muy amplio.
     *  Si el servicio lanzara otras excepciones (ej: error de BD),
     *  todas retornarían 404, lo que puede confundir al cliente
     *  de la API (el error real podría ser un 500).
     *
     *  SOLUCIÓN RECOMENDADA:
     *    Crear excepciones personalizadas (ej: ClienteNotFoundException)
     *    y manejarlas con @ControllerAdvice / @ExceptionHandler para
     *    retornar el código HTTP correcto en cada caso.
     *  -------------------------------------------------------
     * ============================================================
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<Cliente> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clienteService.findById(id));
        } catch (RuntimeException e) {
            // Cliente no encontrado → 404 Not Found (sin cuerpo en la respuesta)
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: POST /api/clientes
     * ============================================================
     *  Crea un nuevo cliente en la base de datos.
     *
     *  Solo usuarios con rol ADMIN pueden crear clientes.
     *
     *  @RequestBody Cliente cliente
     *    Deserializa el JSON del cuerpo de la petición HTTP a un
     *    objeto Java de tipo Cliente.
     *    Spring usa Jackson (librería incluida en Spring Boot) para
     *    realizar esta conversión automáticamente.
     *
     *  @Valid
     *    Activa las validaciones definidas con anotaciones en la
     *    clase Cliente (ej: @NotNull, @Size, @Email, etc.).
     *    Si alguna validación falla, Spring retorna automáticamente
     *    400 Bad Request ANTES de ejecutar el método.
     *
     *  ResponseEntity<?>
     *    El "?" (wildcard) permite retornar tipos diferentes:
     *      - Éxito: objeto Cliente (201 Created)
     *      - Error: Map con mensaje de error (400 Bad Request)
     *
     *  HttpStatus.CREATED (201)
     *    Código HTTP correcto para indicar que un recurso fue creado.
     *    Es más semántico que usar 200 OK para creaciones.
     *
     *  Map.of("error", e.getMessage())
     *    Crea un mapa inmutable de una entrada, que Jackson serializa
     *    como: { "error": "mensaje del error" }
     *
     *  ⚠️  ALERTA — MISMA ADVERTENCIA QUE getById():
     *    Capturar RuntimeException es demasiado genérico.
     *    Ver solución recomendada en el método anterior.
     * ============================================================
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente) {
        try {
            // 201 Created + el cliente guardado (con su ID asignado por la BD)
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.save(cliente));
        } catch (RuntimeException e) {
            // 400 Bad Request + JSON con el mensaje de error
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: PUT /api/clientes/{id}
     * ============================================================
     *  Actualiza TODOS los campos de un cliente existente.
     *
     *  DIFERENCIA entre PUT y PATCH:
     *  -------------------------------------------------------
     *  PUT   → reemplaza el recurso COMPLETO. Si no envías un campo,
     *           se sobreescribe con null o el valor por defecto.
     *  PATCH → actualización PARCIAL. Solo actualiza los campos
     *           que se envíen en el cuerpo.
     *  -------------------------------------------------------
     *
     *  @PathVariable Long id → ID del cliente a actualizar (desde la URL)
     *  @RequestBody Cliente cliente → datos nuevos del cliente (desde el JSON)
     *
     *  Resultado:
     *    - Éxito: 200 OK + cliente actualizado en JSON
     *    - No encontrado: 404 Not Found
     *
     *  ⚠️  ALERTA — INCONSISTENCIA EN EL MANEJO DE ERRORES:
     *  -------------------------------------------------------
     *  El método create() retorna un body con { "error": "..." }
     *  cuando falla, pero este método retorna 404 sin cuerpo.
     *
     *  Si el error no es "no encontrado" sino otro tipo (ej: email
     *  duplicado), retornar 404 es incorrecto semánticamente.
     *
     *  SOLUCIÓN:
     *    Distinguir tipos de excepción y retornar el código HTTP
     *    apropiado en cada caso (400, 404, 409 Conflict, etc.).
     *  -------------------------------------------------------
     * ============================================================
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Cliente cliente) {
        try {
            return ResponseEntity.ok(clienteService.update(id, cliente));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: DELETE /api/clientes/{id}
     * ============================================================
     *  Elimina un cliente por su ID.
     *
     *  Solo el rol ADMIN puede eliminar clientes.
     *
     *  Resultado:
     *    - Éxito: 200 OK + JSON con mensaje de confirmación
     *             { "mensaje": "Cliente eliminado correctamente" }
     *    - No encontrado: 404 Not Found (sin cuerpo)
     *
     *  ✅ BUENA PRÁCTICA — Respuesta confirmatoria:
     *    Retornar un mensaje JSON tras un DELETE exitoso es una
     *    buena práctica de UX para APIs. Algunas APIs retornan
     *    204 No Content (sin cuerpo) como alternativa válida.
     *
     *  Map<String, String>
     *    El tipo de retorno es explícito aquí (no wildcard "?"),
     *    lo cual es más claro para el éxito. Sin embargo, en el
     *    caso de error se retorna sin cuerpo con .build(),
     *    lo que es consistente.
     *
     *  ⚠️  ALERTA — OPERACIÓN IRREVERSIBLE SIN CONFIRMACIÓN:
     *  -------------------------------------------------------
     *  Un DELETE es permanente. Si la entidad Cliente tiene
     *  relaciones con Pedidos u otras entidades, eliminarla
     *  podría causar errores de integridad referencial en la BD.
     *
     *  SOLUCIÓN RECOMENDADA:
     *    Considerar un "soft delete": en lugar de eliminar el
     *    registro, agregar un campo "activo = false" y filtrar
     *    los inactivos en las consultas. Esto preserva el historial.
     *  -------------------------------------------------------
     * ============================================================
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            // Eliminación exitosa → 200 OK con mensaje de confirmación
            return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado correctamente"));
        } catch (RuntimeException e) {
            // Cliente no encontrado → 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}