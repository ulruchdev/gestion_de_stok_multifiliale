package com.stockmaster.auth.config;

import com.stockmaster.shared.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, Long entrepriseId, Long groupId,
                                       String role, String scope) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration() * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USER_ID, userId)
                .claim("entrepriseId", entrepriseId)
                .claim("groupId", groupId)
                .claim("role", role)
                .claim("scope", scope)
                .claim("jti", UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Génère un refresh token portant un {@code familyId} (chaîne de rotation, US-083) et un
     * {@code jti} propre à ce token précis — permet de détecter le rejeu d'un token déjà tourné.
     *
     * @param familyId identifiant de la famille de tokens (constant sur toute la durée de la session,
     *                  généré une seule fois au login)
     * @param jti       identifiant unique de ce token précis (change à chaque rotation)
     */
    public String generateRefreshToken(Long userId, String familyId, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration() * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USER_ID, userId)
                .claim("familyId", familyId)
                .claim("jti", jti)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    public Long getEntrepriseIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("entrepriseId", Long.class);
    }

    public Long getGroupIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("groupId", Long.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }
}
