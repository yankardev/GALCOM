package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.*;
import com.cibertec.galcom.models.*;
import com.cibertec.galcom.repositories.*;
import com.cibertec.galcom.services.ICobranzaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CobranzaService implements ICobranzaService {
    private final ICuentaPorCobrarRepository cuentaRepository;
    private final IReciboRepository reciboRepository;
    private final IDetalleReciboRepository detalleRepository;
    private final IBancoRepository bancoRepository;
    private final IMovimientoBancarioRepository movimientoRepository;
    private final ISocioRepository socioRepository;
    private final IPuestoRepository puestoRepository;
    private final CurrentUserService currentUserService;
    private final AuditoriaService auditoriaService;
    private final ReciboSupportService reciboSupport;

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrar> cuentasSocio(Long socioId) {
        if (!socioRepository.existsById(socioId)) throw new IllegalArgumentException("El socio indicado no existe");
        return cuentaRepository.findBySocioConRelaciones(socioId).stream().map(this::cuentaToModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrar> cuentasPuesto(Long puestoId) {
        if (!puestoRepository.existsById(puestoId)) throw new IllegalArgumentException("El puesto indicado no existe");
        return cuentaRepository.findByPuestoConRelaciones(puestoId).stream().map(this::cuentaToModel).toList();
    }

    @Override
    @Transactional
    public PagoResponse procesarPago(PagoRequest request) {
        List<Long> abonarIds = normalizar(request.getCuentasAbonar());
        List<Long> exonerarIds = normalizar(request.getCuentasExonerar());
        if (abonarIds.isEmpty() && exonerarIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una cuenta para abonar o exonerar");
        }
        Set<Long> repetidas = new HashSet<>(abonarIds);
        repetidas.retainAll(exonerarIds);
        if (!repetidas.isEmpty()) throw new IllegalArgumentException("Una cuenta no puede abonarse y exonerarse al mismo tiempo");

        List<CuentaPorCobrarEntity> abonar = cargarPendientes(abonarIds);
        List<CuentaPorCobrarEntity> exonerar = cargarPendientes(exonerarIds);
        validarMismoResponsable(abonar, exonerar);

        UsuarioEntity usuario = currentUserService.get();
        for (CuentaPorCobrarEntity cuenta : exonerar) {
            cuenta.setEstado("EXONERADA");
            auditoriaService.registrar(usuario, "EXONERAR", "CUENTA", cuenta.getId(), cuenta.getMonto(), "Cuenta exonerada");
        }
        cuentaRepository.saveAll(exonerar);

        BigDecimal total = abonar.stream().map(CuentaPorCobrarEntity::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        ReciboEntity recibo = null;

        if (!abonar.isEmpty()) {
            if (request.getMetodoPago() == null || request.getMetodoPago().isBlank()) {
                throw new IllegalArgumentException("El método de pago es obligatorio para las cuentas abonadas");
            }
            validarMetodoPago(request.getMetodoPago());
            CuentaPorCobrarEntity primera = abonar.getFirst();
            recibo = reciboSupport.nuevo("INGRESO", usuario, primera.getSocio(), primera.getPuesto(), total, request.getMetodoPago().toUpperCase());
            recibo = reciboRepository.save(recibo);

            for (CuentaPorCobrarEntity cuenta : abonar) {
                cuenta.setEstado("ABONADA");
                DetalleReciboEntity detalle = DetalleReciboEntity.builder()
                        .recibo(recibo)
                        .cuenta(cuenta)
                        .montoAplicado(cuenta.getMonto())
                        .build();
                detalleRepository.save(detalle);
                recibo.getDetalles().add(detalle);
                auditoriaService.registrar(usuario, "ABONAR", "CUENTA", cuenta.getId(), cuenta.getMonto(), "Cuenta abonada en " + recibo.getNumeroCorrelativo());
            }
            cuentaRepository.saveAll(abonar);

            if (request.getBancoId() != null) {
                BancoEntity banco = bancoRepository.findById(request.getBancoId())
                        .orElseThrow(() -> new IllegalArgumentException("El banco indicado no existe"));
                movimientoRepository.save(MovimientoBancarioEntity.builder()
                        .banco(banco)
                        .usuario(usuario)
                        .recibo(recibo)
                        .tipo("DEPOSITO")
                        .fechaDeposito(request.getFechaDeposito() != null ? request.getFechaDeposito() : LocalDate.now())
                        .numeroOperacion(request.getNumeroOperacion())
                        .monto(total)
                        .observaciones(request.getObservaciones())
                        .build());
            }
            auditoriaService.registrar(usuario, "PAGO", "RECIBO", recibo.getId(), total, "Pago procesado " + recibo.getNumeroCorrelativo());
        }

        return PagoResponse.builder()
                .recibo(recibo == null ? null : reciboSupport.toModel(recibo, true))
                .totalPagado(total)
                .cuentasAbonadas(abonarIds)
                .cuentasExoneradas(exonerarIds)
                .build();
    }

    @Override
    @Transactional
    public Recibo canjear(CanjeBancarioRequest request) {
        CuentaPorCobrarEntity cuenta = cuentaRepository.findByIdConRelaciones(request.getCuentaId())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta indicada no existe"));
        if (cuenta.getSocio() == null) throw new IllegalArgumentException("El canje bancario aplica a cuentas de socios");
        if (!"PENDIENTE".equals(cuenta.getEstado())) throw new IllegalArgumentException("La cuenta ya fue procesada");
        BancoEntity banco = bancoRepository.findById(request.getBancoId())
                .orElseThrow(() -> new IllegalArgumentException("El banco indicado no existe"));
        UsuarioEntity usuario = currentUserService.get();

        ReciboEntity recibo = reciboSupport.nuevo("BANCARIO", usuario, cuenta.getSocio(), null, cuenta.getMonto(), "TRANSFERENCIA");
        recibo = reciboRepository.save(recibo);
        DetalleReciboEntity detalle = DetalleReciboEntity.builder().recibo(recibo).cuenta(cuenta).montoAplicado(cuenta.getMonto()).build();
        detalleRepository.save(detalle);
        recibo.getDetalles().add(detalle);
        cuenta.setEstado("ABONADA");
        cuentaRepository.save(cuenta);
        movimientoRepository.save(MovimientoBancarioEntity.builder()
                .banco(banco).cuenta(cuenta).usuario(usuario).recibo(recibo).tipo("CANJE")
                .fechaDeposito(request.getFechaDeposito()).numeroOperacion(request.getNumeroOperacion())
                .monto(cuenta.getMonto()).observaciones(request.getObservaciones()).build());
        auditoriaService.registrar(usuario, "CANJE", "CUENTA", cuenta.getId(), cuenta.getMonto(), "Canje bancario " + recibo.getNumeroCorrelativo());
        return reciboSupport.toModel(recibo, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenResponsable resumen(String tipo, Long id) {
        String t = tipo == null ? "" : tipo.toUpperCase();
        List<CuentaPorCobrarEntity> cuentas;
        List<ReciboEntity> recibos;
        String nombre;
        if ("SOCIO".equals(t)) {
            SocioEntity socio = socioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El socio indicado no existe"));
            nombre = socio.getNombres() + " " + socio.getApellidos();
            cuentas = cuentaRepository.findBySocioConRelaciones(id);
            recibos = reciboRepository.findBySocio(id);
        } else if ("PUESTO".equals(t)) {
            PuestoEntity puesto = puestoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El puesto indicado no existe"));
            nombre = "Puesto " + puesto.getNumero();
            cuentas = cuentaRepository.findByPuestoConRelaciones(id);
            recibos = reciboRepository.findByPuesto(id);
        } else {
            throw new IllegalArgumentException("El tipo debe ser SOCIO o PUESTO");
        }
        BigDecimal pendiente = cuentas.stream().filter(c -> "PENDIENTE".equals(c.getEstado()))
                .map(CuentaPorCobrarEntity::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResumenResponsable.builder()
                .tipo(t).id(id).nombre(nombre).totalPendiente(pendiente)
                .cuentas(cuentas.stream().map(this::cuentaToModel).toList())
                .recibos(recibos.stream().map(r -> reciboSupport.toModel(r, false)).toList())
                .build();
    }

    private List<Long> normalizar(List<Long> ids) {
        if (ids == null) return List.of();
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private List<CuentaPorCobrarEntity> cargarPendientes(List<Long> ids) {
        List<CuentaPorCobrarEntity> cuentas = new ArrayList<>();
        for (Long id : ids) {
            CuentaPorCobrarEntity cuenta = cuentaRepository.findByIdConRelaciones(id)
                    .orElseThrow(() -> new IllegalArgumentException("No existe la cuenta " + id));
            if (!"PENDIENTE".equals(cuenta.getEstado())) throw new IllegalArgumentException("La cuenta " + id + " ya fue procesada");
            cuentas.add(cuenta);
        }
        return cuentas;
    }

    private void validarMismoResponsable(List<CuentaPorCobrarEntity> a, List<CuentaPorCobrarEntity> e) {
        List<CuentaPorCobrarEntity> todas = new ArrayList<>(a); todas.addAll(e);
        if (todas.isEmpty()) return;
        CuentaPorCobrarEntity primera = todas.getFirst();
        Long socio = primera.getSocio() != null ? primera.getSocio().getId() : null;
        Long puesto = primera.getPuesto() != null ? primera.getPuesto().getId() : null;
        boolean diferentes = todas.stream().anyMatch(c -> !Objects.equals(socio, c.getSocio() != null ? c.getSocio().getId() : null)
                || !Objects.equals(puesto, c.getPuesto() != null ? c.getPuesto().getId() : null));
        if (diferentes) throw new IllegalArgumentException("Procese cuentas de un solo socio o puesto por operación");
    }

    private void validarMetodoPago(String metodo) {
        if (!Set.of("EFECTIVO", "TRANSFERENCIA", "YAPE_PLIN", "TARJETA").contains(metodo.toUpperCase())) {
            throw new IllegalArgumentException("Método de pago inválido");
        }
    }

    private CuentaPorCobrar cuentaToModel(CuentaPorCobrarEntity entity) {
        return CuentaPorCobrar.builder()
                .id(entity.getId()).servicioId(entity.getServicio().getId()).servicioNombre(entity.getServicio().getNombre())
                .socioId(entity.getSocio() != null ? entity.getSocio().getId() : null)
                .socioNombre(entity.getSocio() != null ? entity.getSocio().getNombres() + " " + entity.getSocio().getApellidos() : null)
                .puestoId(entity.getPuesto() != null ? entity.getPuesto().getId() : null)
                .puestoNumero(entity.getPuesto() != null ? entity.getPuesto().getNumero() : null)
                .periodo(entity.getPeriodo()).lecturaInicial(entity.getLecturaInicial()).lecturaFinal(entity.getLecturaFinal())
                .costoUnitario(entity.getCostoUnitario()).monto(entity.getMonto()).fechaEmision(entity.getFechaEmision())
                .fechaVencimiento(entity.getFechaVencimiento()).estado(entity.getEstado()).build();
    }
}
