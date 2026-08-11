package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.LoginResponse;
import com.cibertec.galcom.models.Usuario;
import com.cibertec.galcom.services.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final IUsuarioService iUsuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid Usuario usuario) {
        LoginResponse response = iUsuarioService.login(usuario);
        if (response == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(response);
    }
}
