package com.panaderia.rodrigo.config;

// ============================================================
//  IMPORTACIONES DE SPRING SECURITY
//  Spring Security es el módulo de seguridad de Spring que
//  gestiona autenticación (¿quién eres?) y autorización
//  (¿qué puedes hacer?).
// ============================================================
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================
 *  CLASE: SecurityConfig
 * ============================================================
 *  Centraliza TODA la configuración de seguridad de la aplicación.
 *  Aquí definimos:
 *    - Cómo se encriptan las contraseñas.
 *    - Qué usuarios existen (en memoria, para este ejemplo).
 *    - Qué URLs son públicas y cuáles requieren autenticación/rol.
 *    - Cómo funciona el formulario de login y el logout.
 * ============================================================
 */

/**
 * @Configuration
 *   Le indica a Spring que esta clase contiene definiciones de Beans
 *   (objetos administrados por el contenedor de Spring).
 *
 * @EnableWebSecurity
 *   Activa la seguridad web de Spring Security.
 *   Sin esta anotación, las reglas de acceso NO tendrían efecto.
 *
 * @EnableMethodSecurity
 *   Habilita la seguridad a nivel de método, es decir, permite usar
 *   anotaciones como @PreAuthorize("hasRole('ADMIN')") directamente
 *   sobre métodos de los controladores o servicios para un control
 *   de acceso más granular.
 *
 *   ⚠️  NOTA EDUCATIVA:
 *   En este archivo no se usan anotaciones como @PreAuthorize, por lo
 *   que @EnableMethodSecurity está declarada pero sin uso visible aquí.
 *   Es útil tenerla si en otros controladores se usará seguridad por método.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * ============================================================
     *  BEAN: PasswordEncoder
     * ============================================================
     *  Define el algoritmo de encriptación para las contraseñas.
     *
     *  BCryptPasswordEncoder es el estándar recomendado porque:
     *    - Aplica "salting" automático (agrega datos aleatorios antes
     *      de encriptar, evitando ataques de diccionario).
     *    - Es un algoritmo lento por diseño, lo que dificulta ataques
     *      de fuerza bruta.
     *
     *  ✅ BUENA PRÁCTICA: NUNCA guardar contraseñas en texto plano.
     *     Siempre encriptarlas con BCrypt u otro algoritmo seguro.
     *
     *  Al declararlo como @Bean, Spring lo inyectará automáticamente
     *  donde sea necesario (por ejemplo, en userDetailsService).
     * ============================================================
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ============================================================
     *  BEAN: UserDetailsService
     * ============================================================
     *  Define los usuarios de la aplicación y sus roles.
     *
     *  UserDetailsService es la interfaz que Spring Security usa para
     *  cargar la información del usuario durante el proceso de login.
     *
     *  En este caso se usa InMemoryUserDetailsManager, que almacena
     *  los usuarios en la memoria RAM del servidor (no en base de datos).
     *
     *  ⚠️  ALERTA — USO SOLO PARA DESARROLLO/APRENDIZAJE:
     *  -------------------------------------------------------
     *  InMemoryUserDetailsManager guarda los usuarios en memoria,
     *  lo que significa que:
     *    - Los usuarios se PIERDEN al reiniciar el servidor.
     *    - Las credenciales están "hardcodeadas" en el código fuente.
     *    - NO es apto para producción.
     *
     *  SOLUCIÓN PARA PRODUCCIÓN:
     *    Implementar un UserDetailsService personalizado que cargue
     *    los usuarios desde una base de datos (tabla "usuarios" o similar),
     *    usando JPA/Hibernate y un repositorio Spring Data.
     *  -------------------------------------------------------
     *
     *  Recibe por inyección el PasswordEncoder definido arriba.
     * ============================================================
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {

        /**
         * Usuario ADMIN
         * -------------
         * - username: "admin"     → nombre de login
         * - password: encriptada con BCrypt
         * - roles("ADMIN")        → Spring agrega el prefijo "ROLE_" internamente,
         *                           por lo que el rol queda como "ROLE_ADMIN"
         *
         * Este usuario tiene acceso total a todas las rutas protegidas,
         * incluyendo crear, editar y eliminar productos.
         *
         * ⚠️  ALERTA — CREDENCIALES INSEGURAS EN CÓDIGO FUENTE:
         * -------------------------------------------------------
         * Tener "admin123" directamente en el código es una mala práctica.
         * En producción, las credenciales deben provenir de variables de
         * entorno o de un sistema externo de gestión de secretos
         * (ej: application.properties cifrado, AWS Secrets Manager, Vault).
         * -------------------------------------------------------
         */
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        /**
         * Usuario EMPLEADO
         * ----------------
         * Acceso limitado: puede ver y gestionar clientes y pedidos,
         * pero NO puede crear, editar ni eliminar productos.
         *
         * ⚠️  ALERTA — CREDENCIALES INSEGURAS EN CÓDIGO FUENTE:
         *   Misma advertencia que el usuario ADMIN. Ver arriba.
         */
        UserDetails empleado = User.builder()
                .username("empleado")
                .password(encoder.encode("emp123"))
                .roles("EMPLEADO")
                .build();

        // Devuelve el gestor en memoria con ambos usuarios registrados
        return new InMemoryUserDetailsManager(admin, empleado);
    }

    /**
     * ============================================================
     *  BEAN: SecurityFilterChain
     * ============================================================
     *  Es el corazón de la configuración de seguridad.
     *  Define la "cadena de filtros" que cada petición HTTP debe
     *  atravesar antes de llegar a los controladores.
     *
     *  Aquí se configuran 4 secciones principales:
     *    1. Reglas de autorización por URL (authorizeHttpRequests)
     *    2. Formulario de login (formLogin)
     *    3. Logout (logout)
     *    4. Configuración CSRF y headers (csrf / headers)
     *
     *  Recibe HttpSecurity como parámetro, que es el objeto builder
     *  que Spring Security provee para construir estas reglas.
     * ============================================================
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /**
                 * ----------------------------------------------------
                 *  SECCIÓN 1: REGLAS DE AUTORIZACIÓN POR URL
                 * ----------------------------------------------------
                 *  Las reglas se evalúan EN ORDEN, de arriba hacia abajo.
                 *  La PRIMERA regla que coincide con la URL gana.
                 *  Por eso las rutas más específicas deben ir PRIMERO.
                 * ----------------------------------------------------
                 */
                .authorizeHttpRequests(auth -> auth

                        /**
                         * RUTAS PÚBLICAS — Accesibles por cualquier visitante
                         * sin necesidad de estar autenticado.
                         *
                         * "/css/**", "/img/**", "/js/**" → recursos estáticos
                         *   (hojas de estilo, imágenes, scripts). Siempre deben
                         *   ser públicos para que el login se vea correctamente.
                         *
                         * "/login" → la propia página de login debe ser pública,
                         *   de lo contrario habría un bucle de redirección infinita.
                         */
                        .requestMatchers("/", "/index", "/login", "/css/**", "/img/**", "/js/**").permitAll()

                        /**
                         * CONSOLA H2 — Solo para desarrollo
                         * ----------------------------------
                         * H2 es una base de datos en memoria muy útil para pruebas.
                         * Su consola web está en "/h2-console/**".
                         *
                         * ⚠️  ALERTA — SEGURIDAD EN PRODUCCIÓN:
                         * -------------------------------------------------------
                         * Dejar la consola H2 pública en producción es un riesgo
                         * de seguridad CRÍTICO: cualquiera podría ejecutar SQL
                         * sobre tu base de datos.
                         *
                         * SOLUCIÓN:
                         *   Deshabilitar H2 y esta regla en producción usando
                         *   perfiles de Spring (@Profile("dev")) o
                         *   properties condicionales (spring.h2.console.enabled=false).
                         * -------------------------------------------------------
                         */
                        .requestMatchers("/h2-console/**").permitAll()

                        /**
                         * PRODUCTOS — Lectura pública, escritura solo ADMIN
                         * -------------------------------------------------
                         * Listar productos (/productos, /api/productos) es público
                         * para que cualquier visitante pueda ver el catálogo.
                         *
                         * Crear, editar y eliminar requiere rol ADMIN.
                         * hasRole("ADMIN") verifica internamente "ROLE_ADMIN".
                         */
                        .requestMatchers("/productos", "/api/productos").permitAll()
                        .requestMatchers("/productos/nuevo", "/productos/guardar",
                                "/productos/editar/**", "/productos/eliminar/**").hasRole("ADMIN")

                        /**
                         * CLIENTES y PEDIDOS — Acceso para ADMIN y EMPLEADO
                         * -------------------------------------------------
                         * hasAnyRole() acepta múltiples roles separados por coma.
                         * Cualquier usuario con rol ADMIN o EMPLEADO puede acceder.
                         *
                         * El patrón "/**" cubre todas las sub-rutas:
                         *   /clientes, /clientes/nuevo, /clientes/editar/5, etc.
                         */
                        .requestMatchers("/clientes/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers("/pedidos/**").hasAnyRole("ADMIN", "EMPLEADO")

                        /**
                         * API REST — Requiere autenticación para todas las operaciones
                         * ------------------------------------------------------------
                         * ⚠️  ALERTA — INCONSISTENCIA ENTRE COMENTARIO Y CÓDIGO:
                         * -------------------------------------------------------
                         * El comentario original dice "GET público, el resto requiere
                         * autenticación", pero el código aplica .authenticated() a
                         * TODOS los métodos HTTP bajo /api/** sin distinción.
                         *
                         * Si la intención es que los GET sean públicos, la solución
                         * correcta es usar requestMatchers con método HTTP:
                         *
                         *   .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                         *   .requestMatchers("/api/**").authenticated()
                         *
                         * Recuerda importar:
                         *   import org.springframework.http.HttpMethod;
                         * -------------------------------------------------------
                         *
                         * NOTA: La ruta "/api/productos" ya fue declarada como
                         * pública más arriba y tiene prioridad por orden de evaluación.
                         */
                        .requestMatchers("/api/**").authenticated()

                        /**
                         * REGLA CATCH-ALL — Cualquier URL no listada arriba
                         * requiere que el usuario esté autenticado.
                         * Funciona como una "red de seguridad" para rutas no declaradas.
                         */
                        .anyRequest().authenticated()
                )

                /**
                 * ----------------------------------------------------
                 *  SECCIÓN 2: FORMULARIO DE LOGIN
                 * ----------------------------------------------------
                 *  Configura la página y el procesamiento del login.
                 * ----------------------------------------------------
                 */
                .formLogin(form -> form
                        // URL de la página de login personalizada (GET)
                        .loginPage("/login")

                        // URL que procesa el formulario (POST con usuario y contraseña)
                        .loginProcessingUrl("/login")

                        // Redirige aquí tras un login exitoso. "true" fuerza siempre
                        // esta URL, ignorando la página original que el usuario intentaba ver.
                        // ⚠️  NOTA: Cambiar a "false" si se prefiere redirigir al usuario
                        //   a la página que intentaba acceder antes de ser redirigido al login.
                        .defaultSuccessUrl("/", true)

                        // Redirige aquí si las credenciales son incorrectas.
                        // El parámetro "?error=true" permite mostrar un mensaje de error en la vista.
                        .failureUrl("/login?error=true")

                        // Permite que todos accedan a las URLs de login (evita bucles de redirección)
                        .permitAll()
                )

                /**
                 * ----------------------------------------------------
                 *  SECCIÓN 3: LOGOUT (Cierre de sesión)
                 * ----------------------------------------------------
                 *  Define cómo se cierra la sesión del usuario.
                 * ----------------------------------------------------
                 */
                .logout(logout -> logout
                        // URL que procesa el logout (generalmente un botón o enlace en la vista)
                        .logoutUrl("/logout")

                        // Redirige aquí tras cerrar sesión exitosamente.
                        // El parámetro "?logout=true" permite mostrar un mensaje de confirmación.
                        .logoutSuccessUrl("/login?logout=true")

                        // Invalida la sesión HTTP (limpia todos los datos de sesión del servidor)
                        .invalidateHttpSession(true)

                        // Borra el objeto de autenticación del SecurityContext
                        .clearAuthentication(true)

                        .permitAll()
                )

                /**
                 * ----------------------------------------------------
                 *  SECCIÓN 4A: CSRF (Cross-Site Request Forgery)
                 * ----------------------------------------------------
                 *  CSRF es un tipo de ataque donde un sitio malicioso
                 *  engaña al navegador del usuario para que haga
                 *  peticiones a TU aplicación sin que el usuario lo sepa.
                 *
                 *  Spring Security activa la protección CSRF por defecto
                 *  para todos los formularios HTML.
                 *
                 *  Se desactiva selectivamente para:
                 *    - /h2-console/**  → la consola H2 no envía token CSRF
                 *    - /api/**         → las APIs REST generalmente usan otros
                 *                       mecanismos (tokens JWT, etc.) y los clientes
                 *                       externos no manejan tokens CSRF de sesión.
                 *
                 *  ⚠️  NOTA: En producción, si la API usa JWT, considera usar
                 *    csrf.disable() solo para /api/** y mantenerlo activo para
                 *    las vistas HTML tradicionales.
                 * ----------------------------------------------------
                 */
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )

                /**
                 * ----------------------------------------------------
                 *  SECCIÓN 4B: HEADERS DE SEGURIDAD
                 * ----------------------------------------------------
                 *  Configura cabeceras HTTP de seguridad.
                 *
                 *  frameOptions: controla si la app puede ser mostrada
                 *  dentro de un <iframe>.
                 *
                 *  .sameOrigin() → solo permite iframes del mismo dominio.
                 *  Esto es necesario porque la consola H2 usa iframes
                 *  internamente y sin esta configuración no se vería.
                 *
                 *  ✅ BUENA PRÁCTICA:
                 *  En producción, si no necesitas iframes en absoluto,
                 *  usa .deny() en lugar de .sameOrigin() para mayor seguridad.
                 *  Esto previene ataques de "clickjacking".
                 * ----------------------------------------------------
                 */
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        // Construye y retorna la cadena de filtros de seguridad configurada
        return http.build();
    }
}