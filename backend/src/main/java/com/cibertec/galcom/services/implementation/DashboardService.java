package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.models.DashboardResumen;
import com.cibertec.galcom.repositories.*;
import com.cibertec.galcom.services.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {
    private final ISocioRepository socioRepository;
    private final IPuestoRepository puestoRepository;
    private final ICuentaPorCobrarRepository cuentaRepository;
    private final IReciboRepository reciboRepository;
    private final IEgresoRepository egresoRepository;
    private final ReciboSupportService reciboSupport;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumen resumen() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.withDayOfMonth(1);
        LocalDate fin = inicio.plusMonths(1);
        BigDecimal recaudado = reciboRepository.findByFechaTipo(inicio.atStartOfDay(), fin.atStartOfDay(), null)
                .stream().filter(r -> "EMITIDO".equals(r.getEstado()) && ("INGRESO".equals(r.getTipo()) || "BANCARIO".equals(r.getTipo())))
                .map(r -> r.getMontoTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal egresos = egresoRepository.buscar(inicio, fin.minusDays(1)).stream()
                .filter(e -> "PROCESADO".equals(e.getEstado())).map(e -> e.getMontoTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return DashboardResumen.builder()
                .socios(socioRepository.count())
                .puestos(puestoRepository.count())
                .puestosOcupados(puestoRepository.countByEstado("OCUPADO"))
                .cuentasPendientes(cuentaRepository.countByEstado("PENDIENTE"))
                .porCobrar(cuentaRepository.totalPendiente())
                .recaudadoMes(recaudado)
                .egresosMes(egresos)
                .ultimosRecibos(reciboRepository.findTop5ByEstadoOrderByFechaDesc("EMITIDO").stream()
                        .map(r -> reciboSupport.toModel(r, false)).toList())
                .build();
    }
}
