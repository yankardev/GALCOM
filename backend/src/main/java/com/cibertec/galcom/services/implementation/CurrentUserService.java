package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.UsuarioEntity;
import com.cibertec.galcom.repositories.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final IUsuarioRepository iUsuarioRepository;

    public UsuarioEntity get() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return iUsuarioRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalStateException("No se pudo resolver el usuario autenticado"));
    }
}
