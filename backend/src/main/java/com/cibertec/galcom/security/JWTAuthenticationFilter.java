package com.cibertec.galcom.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.cibertec.galcom.security.Constants.*;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(HEADER_AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
                String token = authHeader.substring(TOKEN_PREFIX.length());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSigningKey(SECRET_TEXT))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                List<String> authorities = claims.get("authorities", List.class);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                authorities.stream().map(SimpleGrantedAuthority::new).toList()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
        }
    }
}
