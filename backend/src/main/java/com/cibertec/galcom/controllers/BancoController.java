package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Banco;
import com.cibertec.galcom.services.IBancoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bancos")
@RequiredArgsConstructor
public class BancoController {
    private final IBancoService iBancoService;

    @PostMapping public ResponseEntity<Banco> create(@RequestBody @Valid Banco banco) { return ResponseEntity.ok(iBancoService.create(banco)); }
    @GetMapping("/{id}") public ResponseEntity<Banco> get(@PathVariable Long id) { Banco r=iBancoService.get(id); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @GetMapping("/all") public ResponseEntity<List<Banco>> getAll() { return ResponseEntity.ok(iBancoService.getAll()); }
    @PutMapping("/{id}") public ResponseEntity<Banco> update(@PathVariable Long id,@RequestBody @Valid Banco banco){ Banco r=iBancoService.update(id,banco); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ if(iBancoService.get(id)==null)return ResponseEntity.notFound().build(); iBancoService.delete(id); return ResponseEntity.noContent().build(); }
}
