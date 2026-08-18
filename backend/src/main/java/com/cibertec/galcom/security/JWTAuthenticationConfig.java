package com.cibertec.galcom.security;

import com.cibertec.galcom.entities.UsuarioEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.List;

import static com.cibertec.galcom.security.Constants.*;

@Configuration
public class JWTAuthenticationConfig {
    @Value("${security.jwt.secret}")
    private String secretText;

    @Value("${security.jwt.expiration-ms:3600000}")
    private long expirationTime;

    public String getJWTToken(UsuarioEntity usuario) {
        String rol = "ROLE_" + usuario.getRol().getNombre();

        String token = Jwts.builder()
                .setSubject(usuario.getUsuario())
                .claim("usuario", usuario.getUsuario())
                .claim("nombres", usuario.getNombres())
                .claim("apellidos", usuario.getApellidos())
                .claim("authorities", List.of(rol))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(secretText), SignatureAlgorithm.HS256)
                .compact();

        return TOKEN_PREFIX + token;
    }
}
