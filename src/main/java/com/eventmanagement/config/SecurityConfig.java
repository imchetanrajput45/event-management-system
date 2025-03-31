package com.eventmanagement.config;

import com.eventmanagement.services.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for now (optional)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/register", "/home","/login", "/css/**", "/js/**").permitAll()

                        .requestMatchers("/profile", "/profile/update").authenticated() // ✅ Only logged-in users can access
                        .requestMatchers("/uploads/**").permitAll()  // ✅ Allow profile images to be accessed

                        // ✅ Everyone can view events
                        .requestMatchers(HttpMethod.GET, "/events/{id}").permitAll() // Allow viewing event details
                        .requestMatchers(HttpMethod.GET, "/events").permitAll() // Allow listing all events
                        .requestMatchers(HttpMethod.GET, "/events/search/**").permitAll() // ✅ Allow searching events
                        .requestMatchers(HttpMethod.GET, "/events/filter/**").permitAll() // ✅ Allow filtering events



                        // ✅ ORGANIZER can create events
                        .requestMatchers(HttpMethod.GET, "/events/create").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/events/create").hasAuthority("ROLE_ORGANIZER")

                        // ✅ ADMIN & ORGANIZER can edit/delete
                        .requestMatchers(HttpMethod.GET, "/events/edit/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/events/edit/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/events/delete/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORGANIZER")

                        // ✅ Registration (Only Authenticated Users)
                        .requestMatchers("/events/register/**").authenticated()
                        .requestMatchers("/dashboard/**").authenticated()

                        // ✅ Allow marking attendance (Admin & Organizer)
                        .requestMatchers(HttpMethod.POST, "/events/mark-attendance").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORGANIZER")

                        // ✅ ADMIN Panel
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

//                        .requestMatchers("/dashboard").hasAnyRole("ADMIN", "ORGANIZER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/events", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
