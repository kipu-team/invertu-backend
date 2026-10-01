package com.upc.invertu.seguridad.filtros;

import com.upc.invertu.seguridad.servicios.CustomUserDetailsService;
import com.upc.invertu.seguridad.utilidades.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee el header "Authorization: Bearer <token>", valida el JWT y registra al estudiante
 * en el contexto de seguridad. Si el token es invalido o expiro, la peticion sigue sin
 * autenticacion y Spring Security responde 401 "Sesión no válida".
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String jwt = header.substring(7);
            try {
                String correo = jwtUtil.extraerCorreo(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

                if (jwtUtil.validarToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // Token invalido, expirado o de un estudiante que ya no existe: se ignora.
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
