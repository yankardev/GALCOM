package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.MovimientoBancarioEntity;
import com.cibertec.galcom.models.MovimientoBancario;
import com.cibertec.galcom.repositories.IMovimientoBancarioRepository;
import com.cibertec.galcom.services.IMovimientoBancarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoBancarioService implements IMovimientoBancarioService {
    private final IMovimientoBancarioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoBancario> buscar(LocalDate fecha) {
        return repository.buscar(fecha).stream().map(this::toModel).toList();
    }

    private MovimientoBancario toModel(MovimientoBancarioEntity e) {
        return MovimientoBancario.builder()
                .id(e.getId()).bancoId(e.getBanco().getId()).bancoNombre(e.getBanco().getNombre())
                .cuentaId(e.getCuenta()!=null?e.getCuenta().getId():null)
                .reciboId(e.getRecibo()!=null?e.getRecibo().getId():null)
                .tipo(e.getTipo()).fechaDeposito(e.getFechaDeposito()).numeroOperacion(e.getNumeroOperacion())
                .monto(e.getMonto()).observaciones(e.getObservaciones()).build();
    }
}
