package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.BancoEntity;
import com.cibertec.galcom.models.Banco;
import com.cibertec.galcom.repositories.IBancoRepository;
import com.cibertec.galcom.services.IBancoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BancoService implements IBancoService {
    private final IBancoRepository iBancoRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Banco create(Banco banco) {
        banco.setId(null);
        if (banco.getSaldoInicial() == null) banco.setSaldoInicial(BigDecimal.ZERO);
        if (banco.getEstado() == null) banco.setEstado(true);
        return objectMapper.convertValue(iBancoRepository.save(objectMapper.convertValue(banco, BancoEntity.class)), Banco.class);
    }

    @Override
    public Banco get(Long id) {
        return iBancoRepository.findById(id).map(e -> objectMapper.convertValue(e, Banco.class)).orElse(null);
    }

    @Override
    public List<Banco> getAll() {
        return objectMapper.convertValue(iBancoRepository.findAll(), new TypeReference<List<Banco>>() {});
    }

    @Override
    public Banco update(Long id, Banco banco) {
        if (!iBancoRepository.existsById(id)) return null;
        banco.setId(id);
        if (banco.getSaldoInicial() == null) banco.setSaldoInicial(BigDecimal.ZERO);
        if (banco.getEstado() == null) banco.setEstado(true);
        return objectMapper.convertValue(iBancoRepository.save(objectMapper.convertValue(banco, BancoEntity.class)), Banco.class);
    }

    @Override
    public void delete(Long id) {
        if (iBancoRepository.existsById(id)) iBancoRepository.deleteById(id);
    }
}
