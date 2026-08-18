package com.cibertec.galcom.controllers;

import com.cibertec.galcom.models.DashboardResumen;
import com.cibertec.galcom.services.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final IDashboardService service;

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumen> resumen() { return ResponseEntity.ok(service.resumen()); }
}
