package com.cibertec.galcom.controllers;

import com.cibertec.galcom.services.IReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final IReporteService service;

    @GetMapping("/xlsx")
    public ResponseEntity<byte[]> xlsx(@RequestParam String tipo,
                                       @RequestParam(required = false) LocalDate fecha,
                                       @RequestParam(required = false) String mes) {
        byte[] data = service.generar(tipo, fecha, mes);
        String nombre = "GALCOM_" + tipo.toUpperCase() + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .body(data);
    }
}
