package ru.momo.monitoring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.momo.monitoring.security.JwtTokenProvider;
import ru.momo.monitoring.security.JwtUserDetails;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUserDetails userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader(AUTHORIZATION_HEADER);
            log.debug("STOMP Connect Headers - Authorization: {}", authorization);

            String jwtToken = null;
            if (authorization != null && !authorization.isEmpty()) {
                String authHeaderValue = authorization.get(0);
                if (authHeaderValue != null && authHeaderValue.startsWith("Bearer ")) {
                    jwtToken = authHeaderValue.substring(7);
                }
            }

            if (jwtToken != null && jwtTokenProvider.validateToken(jwtToken)) {
                String username = jwtTokenProvider.getUsername(jwtToken);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService.loadUserByUsername(username);

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        accessor.setUser(authentication);
                        log.info("STOMP User '{}' authenticated and set for WebSocket session.", username);
                    } else {
                        log.warn("User details not found for username '{}' from token.", username);
                    }
                }
            } else {
                log.warn("STOMP Connect: JWT Token is missing, invalid, or expired.");
            }
        }
        return message;
    }
}