package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Egreso;
import com.cibertec.galcom.services.IEgresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/egresos")
@RequiredArgsConstructor
public class EgresoController {
    private final IEgresoService service;

    @PostMapping
    public ResponseEntity<Egreso> create(@RequestBody @Valid Egreso egreso) {
        return ResponseEntity.ok(service.create(egreso));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Egreso>> buscar(@RequestParam(required = false) LocalDate inicio,
                                                @RequestParam(required = false) LocalDate fin) {
        return ResponseEntity.ok(service.buscar(inicio, fin));
    }

    @PutMapping("/{id}/procesar")
    public ResponseEntity<Egreso> procesar(@PathVariable Long id) { return ResponseEntity.ok(service.procesar(id)); }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Egreso> anular(@PathVariable Long id) { return ResponseEntity.ok(service.anular(id)); }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Egreso>> importar(@RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(service.importarCsv(archivo));
    }
}
