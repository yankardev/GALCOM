package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Socio;
import com.cibertec.galcom.services.ISocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/socios")
@RequiredArgsConstructor
public class SocioController {
    private final ISocioService iSocioService;

    @PostMapping
    public ResponseEntity<Socio> create(@RequestBody @Valid Socio socio) {
        return ResponseEntity.ok(iSocioService.create(socio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Socio> get(@PathVariable Long id) {
        Socio socio = iSocioService.get(id);
        return socio == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(socio);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Socio>> getAll() {
        return ResponseEntity.ok(iSocioService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Socio> update(@PathVariable Long id, @RequestBody @Valid Socio socio) {
        Socio actualizado = iSocioService.update(id, socio);
        return actualizado == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (iSocioService.get(id) == null) return ResponseEntity.notFound().build();
        iSocioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
