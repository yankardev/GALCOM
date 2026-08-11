package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Servicio;
import com.cibertec.galcom.services.IServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {
    private final IServicioService iServicioService;

    @PostMapping public ResponseEntity<Servicio> create(@RequestBody @Valid Servicio servicio) { return ResponseEntity.ok(iServicioService.create(servicio)); }
    @GetMapping("/{id}") public ResponseEntity<Servicio> get(@PathVariable Long id) { Servicio r=iServicioService.get(id); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @GetMapping("/all") public ResponseEntity<List<Servicio>> getAll() { return ResponseEntity.ok(iServicioService.getAll()); }
    @PutMapping("/{id}") public ResponseEntity<Servicio> update(@PathVariable Long id,@RequestBody @Valid Servicio servicio){ Servicio r=iServicioService.update(id,servicio); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ if(iServicioService.get(id)==null)return ResponseEntity.notFound().build(); iServicioService.delete(id); return ResponseEntity.noContent().build(); }
}
