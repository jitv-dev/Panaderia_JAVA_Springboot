package com.panaderia.rodrigo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails empleado = User.builder()
                .username("empleado")
                .password(passwordEncoder().encode("emp123"))
                .roles("EMPLEADO")
                .build();

        return new InMemoryUserDetailsManager(admin, empleado);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos públicos
                        .requestMatchers("/", "/index", "/login", "/css/**", "/img/**", "/js/**").permitAll()
                        // H2 console (solo desarrollo)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Productos: ver es público, crear/editar/eliminar solo ADMIN
                        .requestMatchers("/productos", "/api/productos").permitAll()
                        .requestMatchers("/productos/nuevo", "/productos/guardar",
                                "/productos/editar/**", "/productos/eliminar/**").hasRole("ADMIN")
                        // Clientes: ADMIN y EMPLEADO
                        .requestMatchers("/clientes/**").hasAnyRole("ADMIN", "EMPLEADO")
                        // Pedidos: ADMIN y EMPLEADO
                        .requestMatchers("/pedidos/**").hasAnyRole("ADMIN", "EMPLEADO")
                        // API REST: GET público, el resto requiere autenticación
                        .requestMatchers("/api/**").authenticated()
                        // Lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                // Necesario para acceder a la consola H2 en desarrollo
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}