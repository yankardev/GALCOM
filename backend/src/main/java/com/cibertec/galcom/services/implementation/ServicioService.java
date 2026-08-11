package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.ServicioEntity;
import com.cibertec.galcom.models.Servicio;
import com.cibertec.galcom.repositories.IServicioRepository;
import com.cibertec.galcom.services.IServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService implements IServicioService {
    private final IServicioRepository iServicioRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Servicio create(Servicio servicio) {
        servicio.setId(null);
        if (servicio.getEstado() == null) servicio.setEstado(true);
        return objectMapper.convertValue(iServicioRepository.save(objectMapper.convertValue(servicio, ServicioEntity.class)), Servicio.class);
    }

    @Override
    public Servicio get(Long id) {
        return iServicioRepository.findById(id).map(e -> objectMapper.convertValue(e, Servicio.class)).orElse(null);
    }

    @Override
    public List<Servicio> getAll() {
        return objectMapper.convertValue(iServicioRepository.findAll(), new TypeReference<List<Servicio>>() {});
    }

    @Override
    public Servicio update(Long id, Servicio servicio) {
        if (!iServicioRepository.existsById(id)) return null;
        servicio.setId(id);
        if (servicio.getEstado() == null) servicio.setEstado(true);
        return objectMapper.convertValue(iServicioRepository.save(objectMapper.convertValue(servicio, ServicioEntity.class)), Servicio.class);
    }

    @Override
    public void delete(Long id) {
        if (iServicioRepository.existsById(id)) iServicioRepository.deleteById(id);
    }
}
