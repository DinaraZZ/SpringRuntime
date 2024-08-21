package kz.runtime.spring.service;

import kz.runtime.spring.entity.User;
import kz.runtime.spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service // классы, в которых описана бизнес-логика (методы), чтобы Спринг мог управлять репозиторием
public class UserService {
    private static final long CURRENT_USER_ID = 1;

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() { // возвращает авторизрванного пользователя
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String email = authentication.getName();

        return userRepository.findByLogin(email).orElse(null);

//        return userRepository.findById(CURRENT_USER_ID).orElse(null);
    }
}
