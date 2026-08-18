package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.AuditoriaMovimientoEntity;
import com.cibertec.galcom.entities.UsuarioEntity;
import com.cibertec.galcom.repositories.IAuditoriaMovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final IAuditoriaMovimientoRepository repository;

    public void registrar(UsuarioEntity usuario, String accion, String entidad, Long entidadId, BigDecimal importe, String detalle) {
        repository.save(AuditoriaMovimientoEntity.builder()
                .usuario(usuario)
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId)
                .importe(importe)
                .detalle(detalle)
                .build());
    }
}
