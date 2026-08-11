package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.GiroEntity;
import com.cibertec.galcom.entities.PuestoEntity;
import com.cibertec.galcom.entities.SocioEntity;
import com.cibertec.galcom.models.Puesto;
import com.cibertec.galcom.repositories.IGiroRepository;
import com.cibertec.galcom.repositories.IPuestoRepository;
import com.cibertec.galcom.repositories.ISocioRepository;
import com.cibertec.galcom.services.IPuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PuestoService implements IPuestoService {
    private final IPuestoRepository iPuestoRepository;
    private final ISocioRepository iSocioRepository;
    private final IGiroRepository iGiroRepository;

    @Override
    @Transactional
    public Puesto create(Puesto puesto) {
        PuestoEntity entity = new PuestoEntity();
        mapearDatos(entity, puesto);
        PuestoEntity guardado = iPuestoRepository.save(entity);
        return get(guardado.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Puesto get(Long id) {
        return iPuestoRepository.findByIdConRelaciones(id).map(this::convertirAModelo).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Puesto> getAll() {
        return iPuestoRepository.findAllConRelaciones().stream().map(this::convertirAModelo).toList();
    }

    @Override
    @Transactional
    public Puesto update(Long id, Puesto puesto) {
        PuestoEntity entity = iPuestoRepository.findById(id).orElse(null);
        if (entity == null) return null;
        mapearDatos(entity, puesto);
        iPuestoRepository.save(entity);
        return get(id);
    }

    @Override
    public void delete(Long id) {
        if (iPuestoRepository.existsById(id)) iPuestoRepository.deleteById(id);
    }

    private void mapearDatos(PuestoEntity entity, Puesto puesto) {
        entity.setNumero(puesto.getNumero());
        entity.setUbicacion(puesto.getUbicacion());
        entity.setInquilinoNombre(puesto.getInquilinoNombre());
        entity.setInquilinoDocumento(puesto.getInquilinoDocumento());
        entity.setInquilinoTelefono(puesto.getInquilinoTelefono());
        entity.setVigenciaInicio(puesto.getVigenciaInicio());
        entity.setVigenciaFin(puesto.getVigenciaFin());
        entity.setEstado(puesto.getEstado());

        GiroEntity giro = iGiroRepository.findById(puesto.getGiroId())
                .orElseThrow(() -> new IllegalArgumentException("El giro indicado no existe"));
        entity.setGiro(giro);

        if (puesto.getSocioId() != null) {
            SocioEntity socio = iSocioRepository.findById(puesto.getSocioId())
                    .orElseThrow(() -> new IllegalArgumentException("El socio indicado no existe"));
            entity.setSocio(socio);
        } else {
            entity.setSocio(null);
        }
    }

    private Puesto convertirAModelo(PuestoEntity entity) {
        return Puesto.builder()
                .id(entity.getId())
                .numero(entity.getNumero())
                .ubicacion(entity.getUbicacion())
                .inquilinoNombre(entity.getInquilinoNombre())
                .inquilinoDocumento(entity.getInquilinoDocumento())
                .inquilinoTelefono(entity.getInquilinoTelefono())
                .vigenciaInicio(entity.getVigenciaInicio())
                .vigenciaFin(entity.getVigenciaFin())
                .estado(entity.getEstado())
                .socioId(entity.getSocio() != null ? entity.getSocio().getId() : null)
                .socioNombre(entity.getSocio() != null ? entity.getSocio().getNombres() + " " + entity.getSocio().getApellidos() : null)
                .giroId(entity.getGiro().getId())
                .giroNombre(entity.getGiro().getNombre())
                .build();
    }
}
