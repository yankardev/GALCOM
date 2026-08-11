package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Puesto;
import com.cibertec.galcom.services.IPuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/puestos")
@RequiredArgsConstructor
public class PuestoController {
    private final IPuestoService iPuestoService;

    @PostMapping public ResponseEntity<Puesto> create(@RequestBody @Valid Puesto puesto) { return ResponseEntity.ok(iPuestoService.create(puesto)); }
    @GetMapping("/{id}") public ResponseEntity<Puesto> get(@PathVariable Long id) { Puesto r=iPuestoService.get(id); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @GetMapping("/all") public ResponseEntity<List<Puesto>> getAll() { return ResponseEntity.ok(iPuestoService.getAll()); }
    @PutMapping("/{id}") public ResponseEntity<Puesto> update(@PathVariable Long id,@RequestBody @Valid Puesto puesto){ Puesto r=iPuestoService.update(id,puesto); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ if(iPuestoService.get(id)==null)return ResponseEntity.notFound().build(); iPuestoService.delete(id); return ResponseEntity.noContent().build(); }
}
