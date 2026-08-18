package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.SocioEntity;
import com.cibertec.galcom.models.Socio;
import com.cibertec.galcom.repositories.ISocioRepository;
import com.cibertec.galcom.services.ISocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocioService implements ISocioService {
    private final ISocioRepository iSocioRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Socio create(Socio socio) {
        socio.setId(null);
        socio.setCodigo(null);
        normalizar(socio);
        validarDniNuevo(socio.getDni());
        if (socio.getEstado() == null) socio.setEstado(true);

        SocioEntity entity = objectMapper.convertValue(socio, SocioEntity.class);
        entity.setCodigo("TMP-" + UUID.randomUUID().toString().substring(0, 12));
        SocioEntity guardado = iSocioRepository.saveAndFlush(entity);
        guardado.setCodigo(String.format("SOC-%03d", guardado.getId()));
        guardado = iSocioRepository.save(guardado);
        return objectMapper.convertValue(guardado, Socio.class);
    }

    @Override
    public Socio get(Long id) {
        return iSocioRepository.findById(id).map(entity -> objectMapper.convertValue(entity, Socio.class)).orElse(null);
    }

    @Override
    public List<Socio> getAll() {
        return objectMapper.convertValue(iSocioRepository.findAll(), new TypeReference<List<Socio>>() {});
    }

    @Override
    @Transactional
    public Socio update(Long id, Socio socio) {
        SocioEntity actual = iSocioRepository.findById(id).orElse(null);
        if (actual == null) return null;
        socio.setId(id);
        socio.setCodigo(actual.getCodigo());
        normalizar(socio);
        validarDniActualizacion(id, socio.getDni());
        if (socio.getEstado() == null) socio.setEstado(actual.getEstado());
        SocioEntity entity = objectMapper.convertValue(socio, SocioEntity.class);
        return objectMapper.convertValue(iSocioRepository.save(entity), Socio.class);
    }

    private void normalizar(Socio socio) {
        if (socio.getDni() != null) socio.setDni(socio.getDni().trim());
        if (socio.getNombres() != null) socio.setNombres(socio.getNombres().trim());
        if (socio.getApellidos() != null) socio.setApellidos(socio.getApellidos().trim());
        if (socio.getTelefono() != null) socio.setTelefono(socio.getTelefono().trim());
        if (socio.getCorreo() != null) socio.setCorreo(socio.getCorreo().trim().toLowerCase());
        if (socio.getAccion() != null) socio.setAccion(socio.getAccion().trim());
    }

    private void validarDniNuevo(String dni) {
        if (dni != null && !dni.isBlank() && iSocioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("El DNI " + dni + " ya se encuentra registrado.");
        }
    }

    private void validarDniActualizacion(Long id, String dni) {
        if (dni != null && !dni.isBlank() && iSocioRepository.existsByDniAndIdNot(dni, id)) {
            throw new IllegalArgumentException("El DNI " + dni + " ya se encuentra registrado en otro socio.");
        }
    }

    @Override
    public void delete(Long id) {
        if (iSocioRepository.existsById(id)) iSocioRepository.deleteById(id);
    }
}
