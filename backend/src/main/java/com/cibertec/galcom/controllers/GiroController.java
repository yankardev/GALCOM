package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Giro;
import com.cibertec.galcom.services.IGiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/giros")
@RequiredArgsConstructor
public class GiroController {
    private final IGiroService iGiroService;

    @PostMapping public ResponseEntity<Giro> create(@RequestBody @Valid Giro giro) { return ResponseEntity.ok(iGiroService.create(giro)); }
    @GetMapping("/{id}") public ResponseEntity<Giro> get(@PathVariable Long id) { Giro r=iGiroService.get(id); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @GetMapping("/all") public ResponseEntity<List<Giro>> getAll() { return ResponseEntity.ok(iGiroService.getAll()); }
    @PutMapping("/{id}") public ResponseEntity<Giro> update(@PathVariable Long id,@RequestBody @Valid Giro giro){ Giro r=iGiroService.update(id,giro); return r==null?ResponseEntity.notFound().build():ResponseEntity.ok(r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ if(iGiroService.get(id)==null)return ResponseEntity.notFound().build(); iGiroService.delete(id); return ResponseEntity.noContent().build(); }
}
