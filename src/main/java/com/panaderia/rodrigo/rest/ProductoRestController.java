package com.panaderia.rodrigo.rest;

// ============================================================
//  IMPORTACIONES
// ============================================================
import com.panaderia.rodrigo.model.Producto;              // Entidad del dominio: representa un producto de la panadería
import com.panaderia.rodrigo.service.ProductoService;      // Capa de servicio con la lógica de negocio
import jakarta.validation.Valid;                            // Activa validaciones del modelo (@NotNull, @Min, etc.)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;                 // Constantes para códigos de estado HTTP (200, 201, 400, 404...)
import org.springframework.http.ResponseEntity;             // Respuesta HTTP completa: cuerpo + código de estado + headers
import org.springframework.security.access.prepost.PreAuthorize; // Seguridad declarativa a nivel de método
import org.springframework.web.bind.annotation.*;           // Anotaciones REST (@GetMapping, @PostMapping, etc.)
import java.util.List;
import java.util.Map;                                       // Para construir respuestas JSON simples { "clave": "valor" }

/**
 * ============================================================
 *  CLASE: ProductoRestController
 * ============================================================
 *  Controlador REST que expone los endpoints de la API para
 *  gestionar el catálogo de productos de la panadería.
 *
 *  DIFERENCIAS CLAVE respecto a ClienteRestController y PedidoRestController:
 *  ---------------------------------------------------------------------------
 *  1. Los endpoints GET son PÚBLICOS (sin @PreAuthorize ni autenticación).
 *     Cualquier visitante puede consultar productos sin estar logueado.
 *     Esto tiene sentido para un catálogo visible en la web.
 *
 *  2. Incluye un endpoint especializado: GET /stock-bajo
 *     que es un ejemplo de consulta de dominio específico del negocio
 *     expuesta como recurso REST.
 *
 *  3. El método create() captura Exception (clase raíz) en lugar de
 *     RuntimeException, lo que cambia levemente el comportamiento
 *     de manejo de errores.
 *
 *  @RestController
 *    Combina @Controller + @ResponseBody.
 *    Todos los métodos serializan su retorno a JSON automáticamente,
 *    sin necesidad de archivos de vista (.html, .jsp, etc.).
 *
 *  @RequestMapping("/api/productos")
 *    Prefijo de URL compartido por todos los endpoints de esta clase.
 * ============================================================
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    /**
     * INYECCIÓN DE DEPENDENCIA — ProductoService
     * -------------------------------------------
     * @Autowired instruye a Spring para inyectar automáticamente
     * una instancia de ProductoService gestionada por el contenedor IoC.
     *
     * Separación de responsabilidades:
     *   Controller → maneja HTTP (entrada/salida)
     *   Service    → contiene la lógica de negocio
     *   Repository → accede a la base de datos
     *
     * ✅ BUENA PRÁCTICA — Inyección por constructor:
     *   Es preferible a @Autowired en campo porque:
     *     - La dependencia queda marcada como final (inmutable)
     *     - Facilita las pruebas unitarias con mocks
     *     - Deja claro que la dependencia es obligatoria
     *
     *   private final ProductoService productoService;
     *
     *   public ProductoRestController(ProductoService productoService) {
     *       this.productoService = productoService;
     *   }
     */
    @Autowired
    private ProductoService productoService;

    /**
     * ============================================================
     *  ENDPOINT: GET /api/productos
     *  (Con filtro opcional por categoría)
     * ============================================================
     *  Retorna todos los productos, o filtra por categoría si se
     *  proporciona el parámetro en la URL.
     *
     *  Ejemplos de uso:
     *    GET /api/productos                       → todos los productos
     *    GET /api/productos?categoria=Tortas      → solo tortas
     *    GET /api/productos?categoria=Pan         → solo panes
     *
     *  ✅ ENDPOINT PÚBLICO — Sin @PreAuthorize
     *  -------------------------------------------------------
     *  Este endpoint NO tiene @PreAuthorize ni restricción de rol.
     *  Cualquier visitante (autenticado o no) puede consultar el catálogo.
     *
     *  Esto es coherente con la configuración de SecurityConfig, donde:
     *    .requestMatchers("/productos", "/api/productos").permitAll()
     *
     *  Es una decisión de diseño válida para tiendas o catálogos públicos.
     *  -------------------------------------------------------
     *
     *  @RequestParam(required = false) String categoria
     *    Lee el parámetro "categoria" de la URL.
     *    Al ser optional (required = false), si no se proporciona
     *    la variable será null y se retornan todos los productos.
     *
     *  ResponseEntity<List<Producto>>
     *    Retorna una lista serializada como array JSON:
     *    [ { "id": 1, "nombre": "Hallulla", ... }, { ... } ]
     * ============================================================
     */
    @GetMapping
    public ResponseEntity<List<Producto>> getAll(
            @RequestParam(required = false) String categoria) {

        // Si se proporcionó categoría, filtra por ella
        if (categoria != null) {
            return ResponseEntity.ok(productoService.findByCategoria(categoria));
        }
        // Sin filtro → retorna todo el catálogo
        return ResponseEntity.ok(productoService.findAll());
    }

    /**
     * ============================================================
     *  ENDPOINT: GET /api/productos/{id}
     * ============================================================
     *  Retorna un producto específico por su ID.
     *
     *  Ejemplo: GET /api/productos/3 → producto con id = 3
     *
     *  ✅ TAMBIÉN PÚBLICO — Sin restricción de autenticación
     *    Cualquier visitante puede consultar el detalle de un producto.
     *
     *  @PathVariable Long id
     *    Extrae el segmento "{id}" de la URL y lo convierte a Long.
     *    Si el valor no es numérico (ej: /api/productos/abc),
     *    Spring retornará automáticamente 400 Bad Request.
     *
     *  Manejo de errores:
     *    - Producto encontrado → 200 OK + objeto Producto en JSON
     *    - No encontrado       → RuntimeException → 404 Not Found
     *
     *  ⚠️  ALERTA — CAPTURA GENÉRICA DE RuntimeException:
     *  -------------------------------------------------------
     *  Atrapar RuntimeException (clase padre) es demasiado amplio.
     *  Un error de conexión a base de datos también sería atrapado
     *  aquí y el cliente recibiría un 404 engañoso en lugar de 500.
     *
     *  SOLUCIÓN RECOMENDADA:
     *    Crear ProductoNotFoundException extends RuntimeException
     *    y capturarla específicamente en este método.
     *    Los errores inesperados deberían propagarse como 500.
     *  -------------------------------------------------------
     * ============================================================
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.findById(id));
        } catch (RuntimeException e) {
            // Producto no encontrado → 404 Not Found (sin cuerpo en la respuesta)
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: POST /api/productos
     * ============================================================
     *  Crea un nuevo producto en el catálogo.
     *  Solo accesible para usuarios con rol ADMIN.
     *
     *  @Valid @RequestBody Producto producto
     *  -------------------------------------------------------
     *  @RequestBody: deserializa el JSON del cuerpo de la petición
     *    a un objeto Java de tipo Producto (usa Jackson internamente).
     *
     *  @Valid: activa las validaciones definidas en la clase Producto
     *    con anotaciones de Jakarta Validation:
     *      @NotBlank, @NotNull, @Min, @Max, @Size, @DecimalMin, etc.
     *
     *    Si alguna validación falla, Spring intercepta ANTES de
     *    ejecutar el método y retorna 400 Bad Request automáticamente,
     *    sin llegar al try-catch.
     *  -------------------------------------------------------
     *
     *  Producto guardado = productoService.save(producto)
     *    Se captura el producto retornado por el servicio, que incluye
     *    el ID asignado por la base de datos. Es importante retornar
     *    este objeto (con ID) y no el que llegó en el request (sin ID).
     *
     *  HttpStatus.CREATED (201)
     *    Semánticamente correcto para indicar que se creó un recurso.
     *    Más descriptivo que usar 200 OK para una creación.
     *
     *  ⚠️  ALERTA — CAPTURA DE Exception (clase raíz):
     *  -------------------------------------------------------
     *  A diferencia de los otros métodos que capturan RuntimeException,
     *  aquí se captura Exception, que es la clase padre de TODAS
     *  las excepciones en Java (checked y unchecked).
     *
     *  Esto significa que incluso errores de I/O, problemas de red,
     *  o errores de JVM serían atrapados y retornados como 400 Bad Request.
     *
     *  SOLUCIÓN:
     *    Capturar excepciones específicas del dominio:
     *    catch (ProductoDuplicadoException e) → 409 Conflict
     *    catch (ValidacionException e)        → 400 Bad Request
     *    Y dejar el resto propagarse como 500 Internal Server Error.
     *  -------------------------------------------------------
     * ============================================================
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody Producto producto) {
        try {
            // Guarda el producto y captura el objeto retornado (ya tiene ID asignado por la BD)
            Producto guardado = productoService.save(producto);
            // 201 Created + producto creado en el cuerpo de la respuesta
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            // 400 Bad Request + JSON con el mensaje de error
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: PUT /api/productos/{id}
     * ============================================================
     *  Actualiza COMPLETAMENTE un producto existente.
     *  Solo accesible para usuarios con rol ADMIN.
     *
     *  PUT vs PATCH:
     *  -------------------------------------------------------
     *  PUT reemplaza el recurso COMPLETO. El cliente debe enviar
     *  todos los campos del producto, incluso los que no cambian.
     *  Si se omite un campo, su valor queda como null o default.
     *
     *  Si solo se necesita actualizar un campo (ej: precio),
     *  sería más eficiente usar PATCH.
     *  -------------------------------------------------------
     *
     *  @PathVariable Long id  → ID del producto a actualizar (viene de la URL)
     *  @RequestBody Producto  → nuevos datos del producto (viene del JSON)
     *
     *  ⚠️  ALERTA — INCONSISTENCIA EN MANEJO DE ERRORES:
     *  -------------------------------------------------------
     *  El método create() retorna un cuerpo JSON con el mensaje de error,
     *  pero este método retorna 404 sin cuerpo ante cualquier fallo.
     *
     *  Si el error fuera por validación o datos duplicados (no por
     *  "no encontrado"), retornar 404 es semánticamente incorrecto.
     *  El cliente no sabría si el producto no existe o si hay un
     *  error de validación.
     *
     *  SOLUCIÓN:
     *    Usar excepciones específicas y retornar el código HTTP
     *    correcto en cada caso (400, 404, 409, etc.),
     *    preferiblemente con un cuerpo JSON descriptivo.
     *  -------------------------------------------------------
     * ============================================================
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Producto producto) {
        try {
            return ResponseEntity.ok(productoService.update(id, producto));
        } catch (RuntimeException e) {
            // Producto no encontrado (o cualquier RuntimeException) → 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: DELETE /api/productos/{id}
     * ============================================================
     *  Elimina un producto del catálogo por su ID.
     *  Solo accesible para usuarios con rol ADMIN.
     *
     *  Resultado:
     *    - Éxito:     200 OK + { "mensaje": "Producto eliminado correctamente" }
     *    - No existe: 404 Not Found (sin cuerpo)
     *
     *  ✅ RESTRICCIÓN CORRECTA:
     *    Solo ADMIN puede eliminar, coherente con SecurityConfig donde:
     *    .requestMatchers("/productos/eliminar/**").hasRole("ADMIN")
     *    Hay consistencia entre las reglas de URL y las de método.
     *
     *  ⚠️  ALERTA — PRODUCTOS CON PEDIDOS ASOCIADOS:
     *  -------------------------------------------------------
     *  Si un producto está referenciado en pedidos existentes,
     *  eliminarlo físicamente puede romper la integridad referencial
     *  de la base de datos y lanzar una excepción no controlada.
     *
     *  SOLUCIÓN 1 — Soft Delete:
     *    Agregar un campo "activo = false" y filtrar productos
     *    inactivos en las consultas. El historial se preserva.
     *
     *  SOLUCIÓN 2 — Validación previa:
     *    Antes de eliminar, verificar si el producto tiene pedidos
     *    asociados y retornar 409 Conflict con un mensaje claro.
     *  -------------------------------------------------------
     * ============================================================
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        try {
            productoService.delete(id);
            // Eliminación exitosa → 200 OK con mensaje confirmatorio en JSON
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
        } catch (RuntimeException e) {
            // Producto no encontrado → 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: GET /api/productos/stock-bajo
     * ============================================================
     *  Retorna la lista de productos cuyo stock está por debajo
     *  del umbral mínimo definido en la lógica del servicio.
     *
     *  Ejemplo: GET /api/productos/stock-bajo
     *
     *  ✅ BUEN DISEÑO DE API — Endpoint de consulta de dominio:
     *  -------------------------------------------------------
     *  Este endpoint representa una consulta específica del negocio
     *  ("¿qué productos necesito reponer?") expuesta como recurso REST.
     *  En lugar de filtrar desde el cliente con ?stock=5, se encapsula
     *  la lógica en el servidor, lo cual es más mantenible.
     *  -------------------------------------------------------
     *
     *  @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
     *    Ambos roles pueden consultar el stock bajo, lo que tiene
     *    sentido ya que un empleado necesita saber qué productos
     *    están por agotarse para notificarlo o reabastecerlos.
     *
     *  ⚠️  ALERTA — POSIBLE CONFLICTO DE RUTAS:
     *  -------------------------------------------------------
     *  Spring evalúa las rutas en orden de especificidad.
     *  Existe un riesgo potencial de conflicto entre:
     *    GET /api/productos/{id}       → @PathVariable espera un Long
     *    GET /api/productos/stock-bajo → ruta literal
     *
     *  En versiones modernas de Spring MVC, las rutas literales
     *  tienen MAYOR PRIORIDAD que las variables de plantilla ({id}),
     *  por lo que "stock-bajo" NO será interpretado como un {id}.
     *
     *  Sin embargo, si por alguna razón el orden de declaración
     *  de métodos importara en tu versión de Spring, colocar
     *  este método ANTES que getById() es una buena práctica
     *  defensiva para evitar ambigüedades.
     *  -------------------------------------------------------
     *
     *  ✅ NOTA: El umbral de "stock bajo" debería estar definido
     *    como constante o configuración en el servicio/dominio,
     *    no como un número mágico en el código.
     * ============================================================
     */
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<Producto>> getStockBajo() {
        // Delega completamente al servicio la lógica de qué es "stock bajo"
        return ResponseEntity.ok(productoService.findStockBajo());
    }
}