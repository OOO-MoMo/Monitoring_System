package ru.momo.monitoring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractJwtFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);

                if (authentication != null) {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof UserDetails) {
                        UserDetails userDetails = (UserDetails) principal;
                        if (userDetails.isEnabled()) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.trace("User '{}' successfully authenticated and SecurityContext updated.", userDetails.getUsername());
                        } else {
                            log.warn("Authentication attempt for disabled user: {}. Token was valid.", userDetails.getUsername());
                            SecurityContextHolder.clearContext();
                        }
                    } else {
                        log.warn("Principal in Authentication is not an instance of UserDetails: {}",
                                principal != null ? principal.getClass().getName() : "null");
                        SecurityContextHolder.clearContext();
                    }
                }
            } catch (Exception e) {
                log.warn("JWT Filter: Error during setting user authentication in security context: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            if (token != null) {
                log.trace("JWT Filter: Invalid or expired token received.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}