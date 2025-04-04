package ru.momo.monitoring.annotations;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.entities.User;

@Aspect
@Component
public class UserActiveAspect {

    private final UserService userService;

    public UserActiveAspect(UserService userService) {
        this.userService = userService;
    }

    @Before("@annotation(CheckUserActive)")
    public void checkUserActive() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        User user = userService.getByEmail(authentication.getName());

        if (!user.getIsActive()) {
            throw new IllegalStateException("User is not active");
        }
    }
}