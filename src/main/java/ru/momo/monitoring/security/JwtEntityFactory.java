package ru.momo.monitoring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;

import java.util.List;

public class JwtEntityFactory {

    public static JwtEntity create(User user) {
        return JwtEntity.builder()
                .id(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(mapToGrantedAuthorities(user.getRole()))
                .build();
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(RoleName role) {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

}
