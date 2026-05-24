package com.thriftby.config;

import com.thriftby.auth.JwtAuthFilter;
import com.thriftby.service.UserService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;      // ✅ com.thriftby.service.UserService
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ CSRF activé pour Thymeleaf (formulaires login/logout)
                // Désactivé uniquement pour l'API REST et le webhook Stripe
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/webhook/stripe")
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        // Autoriser les forwards Thymeleaf et les pages d'erreur
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // Pages publiques
                        .requestMatchers("/", "/login", "/register", "/error").permitAll()
                        .requestMatchers("/boutique", "/boutique/**").permitAll()

                        // Ressources statiques
                        .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/favicon.ico").permitAll()

                        // API publique (register + login)
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/verify").permitAll()

                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()

                        // Webhook Stripe
                        .requestMatchers("/webhook/stripe").permitAll()

                        // ── Accès par rôle ──────────────────────────────────────────
                        .requestMatchers("/superadmin/**").hasRole("SUPERADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )

                // ── Login par formulaire (Thymeleaf) ────────────────────────────
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")           // Spring intercepte POST /login
                        .usernameParameter("email")             // nom du champ email dans le form
                        .passwordParameter("password")          // nom du champ password
                        .successHandler((req, res, auth) -> {
                            // Redirection selon le rôle
                            boolean isSuperAdmin = auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
                            boolean isAdmin = auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                            if (isSuperAdmin) {
                                res.sendRedirect("/superadmin/dashboard");
                            } else if (isAdmin) {
                                res.sendRedirect("/admin/dashboard");
                            } else {
                                res.sendRedirect("/boutique");
                            }
                        })
                        .failureHandler((req, res, ex) -> {
                            String redirectUrl = "/login?error=true";
                            if (ex instanceof org.springframework.security.authentication.DisabledException) {
                                redirectUrl = "/login?disabled=true";
                            }
                            res.sendRedirect(redirectUrl);
                        })
                        .permitAll()
                )

                // ── Logout ─────────────────────────────────────────────────────
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ── Filtre JWT (pour les appels API) ────────────────────────────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8081"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        config.setExposedHeaders(List.of("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userService)       // ✅ utilise UserService.loadUserByUsername
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }
}
