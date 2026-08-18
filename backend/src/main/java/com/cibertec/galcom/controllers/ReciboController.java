package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.Recibo;
import com.cibertec.galcom.services.IReciboService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/recibos")
@RequiredArgsConstructor
public class ReciboController {
    private final IReciboService service;

    @GetMapping("/{id}")
    public ResponseEntity<Recibo> get(@PathVariable Long id) {
        Recibo recibo = service.get(id);
        return recibo == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(recibo);
    }

    @GetMapping
    public ResponseEntity<List<Recibo>> buscar(@RequestParam(required = false) LocalDate fecha,
                                                @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(service.buscar(fecha, tipo));
    }
}
