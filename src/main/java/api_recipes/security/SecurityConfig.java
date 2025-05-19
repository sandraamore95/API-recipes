package api_recipes.security;
import api_recipes.security.jwt.AuthEntryPointJwt;
import api_recipes.security.jwt.AuthTokenFilter;
import api_recipes.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
import java.util.List;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler,
                          AuthTokenFilter authTokenFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.authTokenFilter = authTokenFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ingredients/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/test/public/**").permitAll()
                        .requestMatchers("/images/**", "/images/ingredients/**").permitAll()

                        // Endpoints con restricciones de roles
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole( "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/ingredients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/ingredients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/ingredients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/ingredients/**/upload-image").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")


                        //  endpoints que requieren autenticación
                        .requestMatchers(HttpMethod.POST, "/api/recipes").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/recipes/{id}/upload-image").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/recipes").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").authenticated()

                        // Endpoints de favoritos
                        .requestMatchers(HttpMethod.GET, "/api/favorites").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/favorites/exists/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/favorites").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/recipes/{id}/upload-image").authenticated()



                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuración CORS (opcional pero recomendado)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}