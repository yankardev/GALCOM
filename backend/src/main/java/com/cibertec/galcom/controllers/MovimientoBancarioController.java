package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.MovimientoBancario;
import com.cibertec.galcom.services.IMovimientoBancarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movimientos-bancarios")
@RequiredArgsConstructor
public class MovimientoBancarioController {
    private final IMovimientoBancarioService service;

    @GetMapping("/all")
    public ResponseEntity<List<MovimientoBancario>> buscar(@RequestParam(required = false) LocalDate fecha) {
        return ResponseEntity.ok(service.buscar(fecha));
    }
}
