package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.*;
import com.cibertec.galcom.models.IngresoExterno;
import com.cibertec.galcom.repositories.*;
import com.cibertec.galcom.services.IIngresoExternoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngresoExternoService implements IIngresoExternoService {
    private final IIngresoExternoRepository repository;
    private final IBancoRepository bancoRepository;
    private final IReciboRepository reciboRepository;
    private final IMovimientoBancarioRepository movimientoRepository;
    private final CurrentUserService currentUserService;
    private final AuditoriaService auditoriaService;
    private final ReciboSupportService reciboSupport;

    @Override
    @Transactional
    public IngresoExterno create(IngresoExterno ingreso) {
        UsuarioEntity usuario = currentUserService.get();
        BancoEntity banco = ingreso.getBancoId() == null ? null : bancoRepository.findById(ingreso.getBancoId())
                .orElseThrow(() -> new IllegalArgumentException("El banco indicado no existe"));

        ReciboEntity recibo = reciboSupport.nuevo("INGRESO", usuario, null, null, ingreso.getMonto(), banco == null ? "EFECTIVO" : "TRANSFERENCIA");
        recibo = reciboRepository.save(recibo);

        IngresoExternoEntity entity = repository.save(IngresoExternoEntity.builder()
                .depositante(ingreso.getDepositante()).categoria(ingreso.getCategoria()).concepto(ingreso.getConcepto())
                .monto(ingreso.getMonto()).fecha(ingreso.getFecha()).observaciones(ingreso.getObservaciones())
                .banco(banco).usuario(usuario).recibo(recibo).build());

        if (banco != null) {
            movimientoRepository.save(MovimientoBancarioEntity.builder()
                    .banco(banco).usuario(usuario).recibo(recibo).tipo("DEPOSITO").fechaDeposito(ingreso.getFecha())
                    .monto(ingreso.getMonto()).observaciones("Ingreso externo: " + ingreso.getConcepto()).build());
        }
        auditoriaService.registrar(usuario, "REGISTRAR", "INGRESO_EXTERNO", entity.getId(), entity.getMonto(), entity.getConcepto());
        return toModel(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngresoExterno> buscar(LocalDate inicio, LocalDate fin) {
        return repository.buscar(inicio, fin).stream().map(this::toModel).toList();
    }

    private IngresoExterno toModel(IngresoExternoEntity e) {
        return IngresoExterno.builder().id(e.getId()).depositante(e.getDepositante()).categoria(e.getCategoria())
                .concepto(e.getConcepto()).monto(e.getMonto()).fecha(e.getFecha()).observaciones(e.getObservaciones())
                .bancoId(e.getBanco() != null ? e.getBanco().getId() : null)
                .bancoNombre(e.getBanco() != null ? e.getBanco().getNombre() : null)
                .reciboId(e.getRecibo() != null ? e.getRecibo().getId() : null)
                .numeroRecibo(e.getRecibo() != null ? e.getRecibo().getNumeroCorrelativo() : null).build();
    }
}
