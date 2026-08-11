package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.SocioEntity;
import com.cibertec.galcom.models.Socio;
import com.cibertec.galcom.repositories.ISocioRepository;
import com.cibertec.galcom.services.ISocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocioService implements ISocioService {
    private final ISocioRepository iSocioRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Socio create(Socio socio) {
        socio.setId(null);
        if (socio.getEstado() == null) socio.setEstado(true);
        SocioEntity entity = objectMapper.convertValue(socio, SocioEntity.class);
        return objectMapper.convertValue(iSocioRepository.save(entity), Socio.class);
    }

    @Override
    public Socio get(Long id) {
        return iSocioRepository.findById(id)
                .map(entity -> objectMapper.convertValue(entity, Socio.class))
                .orElse(null);
    }

    @Override
    public List<Socio> getAll() {
        return objectMapper.convertValue(iSocioRepository.findAll(), new TypeReference<List<Socio>>() {});
    }

    @Override
    public Socio update(Long id, Socio socio) {
        if (!iSocioRepository.existsById(id)) return null;
        socio.setId(id);
        if (socio.getEstado() == null) socio.setEstado(true);
        SocioEntity entity = objectMapper.convertValue(socio, SocioEntity.class);
        return objectMapper.convertValue(iSocioRepository.save(entity), Socio.class);
    }

    @Override
    public void delete(Long id) {
        if (iSocioRepository.existsById(id)) iSocioRepository.deleteById(id);
    }
}
