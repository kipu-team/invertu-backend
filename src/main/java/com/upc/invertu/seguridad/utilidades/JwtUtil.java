package com.upc.invertu.seguridad.utilidades;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Genera y valida los tokens JWT.
 * Payload: subject = correo, claim "rol" = ROLE_FREE / ROLE_PREMIUM, y la fecha de expiracion.
 */
@Component
public class JwtUtil {

    private final Key clave;
    private final long expiracionSegundos;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiracion-segundos}") long expiracionSegundos) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiracionSegundos = expiracionSegundos;
    }

    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String rol = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        claims.put("rol", rol);

        Date ahora = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expiracionSegundos * 1000))
                .signWith(clave, SignatureAlgorithm.HS512)
                .compact();
    }

    /** Valor de "expiraEn" en la respuesta del login (END-AUTH-02). */
    public long getExpiracionSegundos() {
        return expiracionSegundos;
    }

    public String extraerCorreo(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean validarToken(String token, UserDetails userDetails) {
        String correo = extraerCorreo(token);
        Date expiracion = extraerClaim(token, Claims::getExpiration);
        return correo.equals(userDetails.getUsername()) && expiracion.after(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(clave)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }
}
