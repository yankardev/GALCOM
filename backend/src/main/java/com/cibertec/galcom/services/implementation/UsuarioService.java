package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.UsuarioEntity;
import com.cibertec.galcom.models.LoginResponse;
import com.cibertec.galcom.models.Usuario;
import com.cibertec.galcom.repositories.IUsuarioRepository;
import com.cibertec.galcom.security.JWTAuthenticationConfig;
import com.cibertec.galcom.services.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {
    private final IUsuarioRepository iUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTAuthenticationConfig jwtAuthenticationConfig;

    @Override
    public LoginResponse login(Usuario usuario) {
        UsuarioEntity entity = iUsuarioRepository.findByUsuario(usuario.getUsuario()).orElse(null);

        if (entity == null || !Boolean.TRUE.equals(entity.getEstado()) ||
                !passwordEncoder.matches(usuario.getPassword(), entity.getPassword())) {
            return null;
        }

        return LoginResponse.builder()
                .token(jwtAuthenticationConfig.getJWTToken(entity))
                .usuario(entity.getUsuario())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .rol(entity.getRol().getNombre())
                .build();
    }
}
