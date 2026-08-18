package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.models.Recibo;
import com.cibertec.galcom.repositories.IReciboRepository;
import com.cibertec.galcom.services.IReciboService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReciboService implements IReciboService {
    private final IReciboRepository repository;
    private final ReciboSupportService support;

    @Override
    @Transactional(readOnly = true)
    public Recibo get(Long id) {
        return repository.findByIdConRelaciones(id).map(r -> support.toModel(r, true)).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recibo> buscar(LocalDate fecha, String tipo) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        String tipoNormalizado = tipo == null || tipo.isBlank() || "TODOS".equalsIgnoreCase(tipo) ? null : tipo.toUpperCase();
        return repository.findByFechaTipo(f.atStartOfDay(), f.plusDays(1).atStartOfDay(), tipoNormalizado)
                .stream().map(r -> support.toModel(r, false)).toList();
    }
}
