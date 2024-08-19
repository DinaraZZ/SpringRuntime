package kz.runtime.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(authorizationConfigurer -> {
            //сначала запрещающие

//            authorizationConfigurer.requestMatchers("/products/addToCart").authenticated(); // разрешение к обращению только авторизованным польз

            authorizationConfigurer.requestMatchers("/products/create/category").hasRole("ADMIN");
            authorizationConfigurer.requestMatchers("/products/create").hasRole("ADMIN");

            authorizationConfigurer.requestMatchers("/products/addToCart").hasRole("USER");
            authorizationConfigurer.anyRequest().permitAll();
        });

        httpSecurity.formLogin(formLoginConfigurer -> {
        });

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); //незашифрованный пароль
    }
}
