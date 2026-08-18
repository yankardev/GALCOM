package com.cibertec.galcom.config;

import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> negocio(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(body("VALIDACION", e.getMessage()));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> integridad(DataIntegrityViolationException e) {
        String detalle = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "";
        String mensaje = "La operación no puede completarse porque el registro está relacionado con otros datos o ya existe un valor único.";
        String d = detalle == null ? "" : detalle.toLowerCase();
        if (d.contains("dni")) mensaje = "El DNI ingresado ya se encuentra registrado.";
        else if (d.contains("numero_cuenta")) mensaje = "El número de cuenta bancaria ya se encuentra registrado.";
        else if (d.contains("cci")) mensaje = "El CCI ingresado ya se encuentra registrado.";
        else if (d.contains("numero") && d.contains("puesto")) mensaje = "El número de puesto ya se encuentra registrado.";
        else if (d.contains("codigo")) mensaje = "El código generado ya se encuentra registrado. Intente nuevamente.";
        else mensaje += " Si tiene movimientos asociados, cámbielo a Inactivo en lugar de eliminarlo.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body("INTEGRIDAD", mensaje));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(x -> x.getDefaultMessage()).orElse("Revise los datos enviados");
        return ResponseEntity.badRequest().body(body("VALIDACION", mensaje));
    }

    private Map<String,Object> body(String codigo, String mensaje) {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("timestamp", LocalDateTime.now()); r.put("codigo", codigo); r.put("message", mensaje); return r;
    }
}
