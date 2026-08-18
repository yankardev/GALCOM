package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.IngresoExterno;
import com.cibertec.galcom.services.IIngresoExternoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ingresos")
@RequiredArgsConstructor
public class IngresoExternoController {
    private final IIngresoExternoService service;

    @PostMapping
    public ResponseEntity<IngresoExterno> create(@RequestBody @Valid IngresoExterno ingreso) {
        return ResponseEntity.ok(service.create(ingreso));
    }

    @GetMapping("/all")
    public ResponseEntity<List<IngresoExterno>> buscar(@RequestParam(required = false) LocalDate inicio,
                                                        @RequestParam(required = false) LocalDate fin) {
        return ResponseEntity.ok(service.buscar(inicio, fin));
    }
}
