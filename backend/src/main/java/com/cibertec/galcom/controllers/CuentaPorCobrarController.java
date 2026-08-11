package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.CuentaPorCobrar;
import com.cibertec.galcom.services.ICuentaPorCobrarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cibertec.galcom.models.GenerarCuentasPuestosRequest;
import com.cibertec.galcom.models.GenerarCuentasSociosRequest;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaPorCobrarController {
    private final ICuentaPorCobrarService iCuentaService;
    @PostMapping
    public ResponseEntity<CuentaPorCobrar> create(@RequestBody @Valid CuentaPorCobrar cuenta) {
        return ResponseEntity.ok(iCuentaService.create(cuenta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaPorCobrar> get(@PathVariable Long id) {
        CuentaPorCobrar cuenta = iCuentaService.get(id);
        return cuenta == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(cuenta);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CuentaPorCobrar>> getAll() {
        return ResponseEntity.ok(iCuentaService.getAll());
    }

    @PostMapping("/generar-puestos")
    public ResponseEntity<List<CuentaPorCobrar>> generarParaPuestos(@RequestBody @Valid GenerarCuentasPuestosRequest request) {
        return ResponseEntity.ok(iCuentaService.generarParaPuestos(request));
    }

    @PostMapping("/generar-socios")
    public ResponseEntity<List<CuentaPorCobrar>> generarParaSocios(@RequestBody @Valid GenerarCuentasSociosRequest request) {
        return ResponseEntity.ok(iCuentaService.generarParaSocios(request));
    }
}