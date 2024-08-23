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

            /*authorizationConfigurer.requestMatchers("/products/create/**").hasRole("ADMIN"); // создать товар
            authorizationConfigurer.requestMatchers("/products/cart/**").authenticated(); // корзина
            authorizationConfigurer.requestMatchers("/products/place_order").authenticated(); // создание заказа
            authorizationConfigurer.requestMatchers("/products/orders").authenticated(); // заказы
            authorizationConfigurer.requestMatchers("/products/moderate_reviews/**").hasRole("ADMIN"); // модерация отзывов
            authorizationConfigurer.requestMatchers("/products/moderate_orders/**").hasRole("ADMIN"); // модерация заказов
            authorizationConfigurer.requestMatchers("/products/change/**").hasRole("ADMIN"); // редактирование заказов
            authorizationConfigurer.requestMatchers("/products/addToCart/**").authenticated(); // добавление в корзину*/

            authorizationConfigurer.requestMatchers("/products/create/**").hasRole("ADMIN"); // создать товар
            authorizationConfigurer.requestMatchers("/products/change").hasRole("ADMIN"); // изменить товар
            authorizationConfigurer.requestMatchers("/products/change_characteristics").hasRole("ADMIN"); // изменить товар

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
