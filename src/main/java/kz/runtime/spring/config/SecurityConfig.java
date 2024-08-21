package kz.runtime.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //            httpSecurity.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());
        httpSecurity.csrf(AbstractHttpConfigurer::disable); // отключение блокировки post запросов

        httpSecurity.authorizeHttpRequests(authorizationConfigurer -> {
            //сначала запрещающие

//            authorizationConfigurer.requestMatchers("/products/addToCart").authenticated(); // разрешение к обращению только авторизованным польз

            authorizationConfigurer.requestMatchers("/products/create/category").hasRole("ADMIN"); // создать товар
            authorizationConfigurer.requestMatchers("/products/create").hasRole("ADMIN"); // создавть товар
            authorizationConfigurer.requestMatchers("/products/cart").hasRole("USER"); // корзина
            authorizationConfigurer.requestMatchers("/products/place_order").hasRole("USER"); // создавние заказа
            authorizationConfigurer.requestMatchers("/products/orders").hasRole("USER"); // заказы
//
//            authorizationConfigurer.requestMatchers("/products/addToCart").hasRole("USER");


            authorizationConfigurer.anyRequest().permitAll();
        });

        httpSecurity.formLogin(formLoginConfigurer -> {
            formLoginConfigurer.defaultSuccessUrl("/products"); // адрес, на который попадёт пользователь после успешного входа
        });

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); //незашифрованный пароль
    }
}
