package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.GiroEntity;
import com.cibertec.galcom.models.Giro;
import com.cibertec.galcom.repositories.IGiroRepository;
import com.cibertec.galcom.services.IGiroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GiroService implements IGiroService {
    private final IGiroRepository iGiroRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Giro create(Giro giro) {
        giro.setId(null);
        if (giro.getEstado() == null) giro.setEstado(true);
        return objectMapper.convertValue(iGiroRepository.save(objectMapper.convertValue(giro, GiroEntity.class)), Giro.class);
    }

    @Override
    public Giro get(Long id) {
        return iGiroRepository.findById(id).map(e -> objectMapper.convertValue(e, Giro.class)).orElse(null);
    }

    @Override
    public List<Giro> getAll() {
        return objectMapper.convertValue(iGiroRepository.findAll(), new TypeReference<List<Giro>>() {});
    }

    @Override
    public Giro update(Long id, Giro giro) {
        if (!iGiroRepository.existsById(id)) return null;
        giro.setId(id);
        if (giro.getEstado() == null) giro.setEstado(true);
        return objectMapper.convertValue(iGiroRepository.save(objectMapper.convertValue(giro, GiroEntity.class)), Giro.class);
    }

    @Override
    public void delete(Long id) {
        if (iGiroRepository.existsById(id)) iGiroRepository.deleteById(id);
    }
}
