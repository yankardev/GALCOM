package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.*;
import com.cibertec.galcom.services.ICobranzaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cobranza")
@RequiredArgsConstructor
public class CobranzaController {
    private final ICobranzaService service;

    @GetMapping("/socio/{id}")
    public ResponseEntity<List<CuentaPorCobrar>> cuentasSocio(@PathVariable Long id) {
        return ResponseEntity.ok(service.cuentasSocio(id));
    }

    @GetMapping("/puesto/{id}")
    public ResponseEntity<List<CuentaPorCobrar>> cuentasPuesto(@PathVariable Long id) {
        return ResponseEntity.ok(service.cuentasPuesto(id));
    }

    @PostMapping("/pagar")
    public ResponseEntity<PagoResponse> pagar(@RequestBody @Valid PagoRequest request) {
        return ResponseEntity.ok(service.procesarPago(request));
    }

    @PostMapping("/canje")
    public ResponseEntity<Recibo> canjear(@RequestBody @Valid CanjeBancarioRequest request) {
        return ResponseEntity.ok(service.canjear(request));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenResponsable> resumen(@RequestParam String tipo, @RequestParam Long id) {
        return ResponseEntity.ok(service.resumen(tipo, id));
    }
}
