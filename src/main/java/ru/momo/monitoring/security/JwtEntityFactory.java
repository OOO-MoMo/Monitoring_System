package ru.momo.monitoring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.momo.monitoring.store.entities.Role;
import ru.momo.monitoring.store.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JwtEntityFactory {

    public static JwtEntity create(User user) {
        return JwtEntity
                .builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(mapToGrantedAuthorities(new ArrayList<>(user.getRoles())))
                .build();
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Role> roles) {
        return roles.stream()
                .map(Role::getRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
