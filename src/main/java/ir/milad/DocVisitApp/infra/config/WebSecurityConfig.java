package ir.milad.DocVisitApp.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    public static final String HTMX_REDIRECT_HEADER = "HX-Redirect";

    @Bean
    public SecurityFilterChain httpSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/patient/**", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/doctor/index", true)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getOutputStream().println("Bad Credentials: Username or password is incorrect");
                        })
                        .permitAll()
                )
                .logout(configurer -> {
                    configurer.logoutSuccessHandler((request, response, authentication) -> {
                        response.setHeader(HTMX_REDIRECT_HEADER, "/login");
                    });
                    configurer.permitAll();
                    configurer.logoutSuccessUrl("/login");
                });

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web
                .ignoring()
                .requestMatchers(
                        "/fonts/**",
                        "/metronic/**",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/img/**",
                        "/lib/**",
                        "/favicon.ico"
                );
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("contact@drpak.fr")
                .password("2606")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
