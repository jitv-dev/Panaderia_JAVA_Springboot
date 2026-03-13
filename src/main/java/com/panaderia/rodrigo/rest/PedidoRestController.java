package com.panaderia.rodrigo.rest;

// ============================================================
//  IMPORTACIONES
// ============================================================
import com.panaderia.rodrigo.model.Pedido;                  // Entidad principal del dominio
import com.panaderia.rodrigo.model.Pedido.EstadoPedido;      // Enum anidado dentro de Pedido
// (PENDIENTE, EN_PROCESO, LISTO, etc.)
import com.panaderia.rodrigo.service.PedidoService;          // Capa de servicio con la lógica de negocio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;               // Respuesta HTTP con código de estado + cuerpo
import org.springframework.security.access.prepost.PreAuthorize; // Seguridad a nivel de método
import org.springframework.web.bind.annotation.*;             // Anotaciones REST
import java.util.List;
import java.util.Map;                                         // Para respuestas JSON simples { "clave": "valor" }

/**
 * ============================================================
 *  CLASE: PedidoRestController
 * ============================================================
 *  Controlador REST que expone los endpoints de la API para
 *  gestionar los pedidos de la panadería.
 *
 *  A diferencia de ClienteRestController, este controlador
 *  presenta algunas diferencias de diseño importantes:
 *
 *    1. No tiene endpoint POST (crear pedido) → probablemente
 *       los pedidos se crean desde una vista HTML tradicional,
 *       no desde la API REST.
 *
 *    2. Tiene un endpoint PATCH para cambiar solo el estado
 *       del pedido, lo cual es un buen ejemplo del uso correcto
 *       de PATCH (actualización parcial).
 *
 *    3. Usa un Enum (EstadoPedido) como parámetro de entrada,
 *       lo que aporta tipado fuerte y validación automática.
 *
 *  @RestController
 *    Indica que esta clase es un controlador REST.
 *    Cada método retorna datos en JSON directamente,
 *    sin necesidad de una vista (Thymeleaf, JSP, etc.).
 *    Equivale a @Controller + @ResponseBody en cada método.
 *
 *  @RequestMapping("/api/pedidos")
 *    URL base compartida por todos los endpoints del controlador.
 *    Todos los métodos heredan este prefijo automáticamente.
 * ============================================================
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoRestController {

    /**
     * INYECCIÓN DE DEPENDENCIA — PedidoService
     * -----------------------------------------
     * @Autowired delega en Spring la creación e inyección de
     * la instancia de PedidoService.
     *
     * El controlador NUNCA debe implementar lógica de negocio.
     * Su responsabilidad es exclusivamente:
     *   1. Recibir la petición HTTP
     *   2. Extraer y validar los parámetros
     *   3. Llamar al servicio correspondiente
     *   4. Construir y retornar la respuesta HTTP
     *
     * ✅ BUENA PRÁCTICA — Inyección por constructor:
     *   Se recomienda inyectar por constructor en lugar de campo,
     *   ya que facilita las pruebas unitarias (se puede pasar
     *   un mock sin necesidad de levantar el contexto de Spring).
     *
     *   private final PedidoService pedidoService;
     *
     *   public PedidoRestController(PedidoService pedidoService) {
     *       this.pedidoService = pedidoService;
     *   }
     */
    @Autowired
    private PedidoService pedidoService;

    /**
     * ============================================================
     *  ENDPOINT: GET /api/pedidos
     *  (Con filtro opcional por estado)
     * ============================================================
     *  Retorna todos los pedidos, o solo los que coincidan con
     *  un estado específico si se proporciona el parámetro.
     *
     *  Ejemplos de uso:
     *    GET /api/pedidos                    → todos los pedidos
     *    GET /api/pedidos?estado=PENDIENTE   → solo los pendientes
     *    GET /api/pedidos?estado=EN_PROCESO  → solo los en proceso
     *
     *  @RequestParam(required = false) EstadoPedido estado
     *  -------------------------------------------------------
     *  Novedad importante respecto a ClienteRestController:
     *  Aquí el parámetro NO es un String sino un ENUM (EstadoPedido).
     *
     *  Spring convierte automáticamente el texto de la URL
     *  al valor del Enum correspondiente.
     *
     *  ⚠️  ALERTA — MANEJO DE ENUM INVÁLIDO:
     *  -------------------------------------------------------
     *  Si el cliente envía un valor que NO existe en el Enum:
     *    GET /api/pedidos?estado=INEXISTENTE
     *
     *  Spring lanzará una MethodArgumentTypeMismatchException
     *  y retornará un 400 Bad Request automáticamente. Sin embargo,
     *  el mensaje de error por defecto de Spring puede ser técnico
     *  y poco amigable para el consumidor de la API.
     *
     *  SOLUCIÓN RECOMENDADA:
     *    Manejar esta excepción con @ControllerAdvice para devolver
     *    un mensaje claro como:
     *    { "error": "Estado inválido. Valores aceptados: PENDIENTE, EN_PROCESO, LISTO" }
     *  -------------------------------------------------------
     *
     *  @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
     *    Ambos roles pueden listar pedidos. Esta protección a nivel
     *    de método funciona gracias a @EnableMethodSecurity en SecurityConfig.
     * ============================================================
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<Pedido>> getAll(
            @RequestParam(required = false) EstadoPedido estado) {

        // Si se proporcionó un estado válido, filtra por ese estado
        if (estado != null) {
            return ResponseEntity.ok(pedidoService.findByEstado(estado));
        }
        // Sin filtro → retorna todos los pedidos
        return ResponseEntity.ok(pedidoService.findAll());
    }

    /**
     * ============================================================
     *  ENDPOINT: GET /api/pedidos/{id}
     * ============================================================
     *  Retorna un pedido específico por su ID.
     *
     *  Ejemplo: GET /api/pedidos/10 → busca el pedido con id = 10
     *
     *  @PathVariable Long id
     *    Extrae el segmento "{id}" de la URL y lo convierte a Long.
     *
     *  Manejo de errores:
     *    - Pedido encontrado  → 200 OK + objeto Pedido en JSON
     *    - No encontrado      → RuntimeException → 404 Not Found
     *
     *  ⚠️  ALERTA — MANEJO DE ERRORES GENÉRICO:
     *  -------------------------------------------------------
     *  Igual que en ClienteRestController: capturar RuntimeException
     *  es demasiado amplio. Un error de conexión a BD también sería
     *  atrapado aquí y retornaría un engañoso 404.
     *
     *  SOLUCIÓN:
     *    Crear PedidoNotFoundException (extends RuntimeException)
     *    y atraparla específicamente. Los errores inesperados
     *    deberían propagarse como 500 Internal Server Error.
     *  -------------------------------------------------------
     * ============================================================
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<Pedido> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pedidoService.findById(id));
        } catch (RuntimeException e) {
            // Pedido no encontrado → 404 Not Found (sin cuerpo)
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: PATCH /api/pedidos/{id}/estado
     * ============================================================
     *  Actualiza ÚNICAMENTE el estado de un pedido existente.
     *
     *  Ejemplo: PATCH /api/pedidos/10/estado?estado=LISTO
     *
     *  ✅ USO CORRECTO DE PATCH:
     *  -------------------------------------------------------
     *  PATCH es el verbo HTTP para actualizaciones PARCIALES.
     *  Aquí solo se modifica el campo "estado", sin tocar el
     *  resto de los datos del pedido (cliente, productos, fecha, etc.).
     *  Esto es mucho más eficiente y semántico que usar PUT,
     *  que reemplazaría el recurso completo.
     *
     *  Comparativa de verbos HTTP para actualización:
     *    PUT   → reemplaza TODO el recurso (requiere enviar todos los campos)
     *    PATCH → modifica SOLO los campos indicados
     *  -------------------------------------------------------
     *
     *  @PatchMapping("/{id}/estado")
     *    La URL incluye "/estado" como sub-recurso, lo que deja
     *    muy claro en la API qué propiedad se está modificando.
     *    Es una convención REST bien adoptada.
     *
     *  @RequestParam EstadoPedido estado
     *    El nuevo estado viene como parámetro de URL (?estado=LISTO).
     *    Nótese que aquí required = true por defecto (no se indicó
     *    required = false), por lo que si se omite el parámetro,
     *    Spring retornará automáticamente 400 Bad Request.
     *
     *  ResponseEntity<?>
     *    Wildcard "?" porque puede retornar:
     *      - Pedido actualizado (200 OK)
     *      - Sin cuerpo (404 Not Found)
     *
     *  ⚠️  ALERTA — ACCESO PERMISIVO AL CAMBIO DE ESTADO:
     *  -------------------------------------------------------
     *  Tanto ADMIN como EMPLEADO pueden cambiar cualquier estado.
     *  En un sistema real, puede ser necesario restringir
     *  transiciones de estado inválidas (ej: no permitir pasar
     *  de LISTO de vuelta a PENDIENTE).
     *
     *  SOLUCIÓN:
     *    Implementar una máquina de estados en la capa de servicio
     *    que valide las transiciones permitidas y lance una excepción
     *    descriptiva si la transición no es válida.
     *  -------------------------------------------------------
     * ============================================================
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestParam EstadoPedido estado) {
        try {
            // Delega al servicio el cambio de estado y retorna el pedido actualizado
            return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
        } catch (RuntimeException e) {
            // Pedido no encontrado → 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ============================================================
     *  ENDPOINT: DELETE /api/pedidos/{id}
     * ============================================================
     *  Elimina un pedido por su ID.
     *
     *  Solo el rol ADMIN puede eliminar pedidos.
     *  Los empleados pueden ver y actualizar estados, pero NO eliminar.
     *  Esto es una decisión de diseño correcta desde el punto de
     *  vista de la seguridad (principio de mínimo privilegio).
     *
     *  Resultado:
     *    - Éxito:       200 OK + { "mensaje": "Pedido eliminado correctamente" }
     *    - No existe:   404 Not Found (sin cuerpo)
     *
     *  Map.of("mensaje", "...")
     *    Crea un Map inmutable con una sola entrada.
     *    Jackson lo serializa automáticamente como:
     *      { "mensaje": "Pedido eliminado correctamente" }
     *    Esto es más informativo que un 200 vacío.
     *
     *  ⚠️  ALERTA — IMPLICANCIAS DE ELIMINAR UN PEDIDO:
     *  -------------------------------------------------------
     *  Los pedidos suelen tener información histórica importante
     *  (ventas, facturación, inventario). Eliminarlos físicamente
     *  puede romper reportes y auditorías.
     *
     *  SOLUCIÓN RECOMENDADA — Soft Delete:
     *    En lugar de DELETE físico, considera agregar un campo
     *    "eliminado = true" o "estado = CANCELADO" en la entidad
     *    Pedido y filtrar estos registros en las consultas.
     *    Esto preserva el historial sin exponer los registros
     *    en las vistas normales.
     *  -------------------------------------------------------
     *
     *  ⚠️  ALERTA — INTEGRIDAD REFERENCIAL:
     *  -------------------------------------------------------
     *  Si Pedido tiene relaciones @OneToMany o @ManyToMany con
     *  DetallePedido u otras entidades, eliminar el pedido sin
     *  gestionar estas relaciones podría lanzar una excepción
     *  de integridad referencial en la base de datos.
     *
     *  SOLUCIÓN:
     *    Usar cascade = CascadeType.ALL en la relación JPA, o
     *    eliminar manualmente las entidades relacionadas antes
     *    de eliminar el pedido padre.
     *  -------------------------------------------------------
     * ============================================================
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        try {
            pedidoService.delete(id);
            // Eliminación exitosa → 200 OK con mensaje confirmatorio
            return ResponseEntity.ok(Map.of("mensaje", "Pedido eliminado correctamente"));
        } catch (RuntimeException e) {
            // Pedido no encontrado → 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}