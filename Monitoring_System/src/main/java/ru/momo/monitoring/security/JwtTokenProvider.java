package ru.momo.monitoring.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.services.RedisService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.response.JwtResponse;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtTokenProvider {

    final JwtProperties jwtProperties;
    final UserDetailsService userDetailsService;
    final UserService userService;
    final RedisService redisService;
    Key key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String createAccessToken(UUID userId, String username, RoleName role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("id", userId.toString());
        claims.put("role", role.name());
        claims.put("typ", "access");
        claims.setId(UUID.randomUUID().toString());
        Instant validity = Instant.now().plus(jwtProperties.getAccess(), ChronoUnit.HOURS);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(validity))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UUID userId, String username) {
        String tokenId = UUID.randomUUID().toString();
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("id", userId.toString());
        claims.put("typ", "refresh");
        claims.setId(tokenId);
        Instant validity = Instant.now().plus(jwtProperties.getRefresh(), ChronoUnit.DAYS);

        String refreshTokenString = Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(validity))
                .signWith(key)
                .compact();

        redisService.storeRefreshToken(userId, tokenId, refreshTokenString, Duration.ofDays(jwtProperties.getRefresh()));

        return refreshTokenString;
    }

    public boolean validateToken(String token){
        try {
            Jws<Claims> claims = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            log.trace("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token){
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public JwtResponse refreshUserTokens(String refreshToken){
        if(!validateToken(refreshToken)){
            throw new AccessDeniedException("Refresh token is invalid or expired (basic validation).");
        }

        Claims claims;

        try {
            claims = getClaimsFromToken(refreshToken);
        } catch (Exception e) {
            throw new AccessDeniedException("Failed to parse refresh token claims.");
        }

        String jti = claims.getId();
        if (jti == null) {
            throw new AccessDeniedException("Refresh token must contain JTI (JWT ID).");
        }

        UUID userId = UUID.fromString(claims.get("id", String.class));

        if (!redisService.isRefreshTokenValid(jti)) {
            redisService.invalidateRefreshToken(userId, jti);
            throw new AccessDeniedException("Refresh token has been revoked or is not found in store.");
        }

        redisService.invalidateRefreshToken(userId, jti);
        log.info("Refresh token {} for user {} has been used and invalidated.", jti, userId);

        User user = userService.getByIdEntity(userId);

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AccessDeniedException("User account is not active.");
        }

        return JwtResponse.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(createAccessToken(userId, user.getEmail(), user.getRole()))
                .refreshToken(createRefreshToken(userId, user.getEmail()))
                .build();
    }

    public String getUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public UUID getUserIdFromToken(String token) {
        String idClaim = getClaimsFromToken(token).get("id", String.class);
        if (idClaim == null) {
            throw new AccessDeniedException("Token does not contain user ID claim.");
        }
        return UUID.fromString(idClaim);
    }

    public String getJtiFromToken(String token) {
        return getClaimsFromToken(token).getId();
    }

    private Claims getClaimsFromToken(String token) throws ExpiredJwtException /*, другие исключения JWT */ {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String getId(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id")
                .toString();
    }

}
