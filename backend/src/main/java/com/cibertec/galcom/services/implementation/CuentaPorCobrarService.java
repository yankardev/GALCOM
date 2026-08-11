package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.*;
import com.cibertec.galcom.models.CuentaPorCobrar;
import com.cibertec.galcom.repositories.*;
import com.cibertec.galcom.services.ICuentaPorCobrarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.galcom.models.GenerarCuentasPuestosRequest;
import java.util.ArrayList;
import com.cibertec.galcom.models.GenerarCuentasSociosRequest;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaPorCobrarService implements ICuentaPorCobrarService {
    private final ICuentaPorCobrarRepository iCuentaRepository;
    private final IServicioRepository iServicioRepository;
    private final ISocioRepository iSocioRepository;
    private final IPuestoRepository iPuestoRepository;

    @Override
    @Transactional
    public CuentaPorCobrar create(CuentaPorCobrar cuenta) {
        ServicioEntity servicio = iServicioRepository
                .findById(cuenta.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("El servicio indicado no existe"));
        validarDestino(cuenta, servicio);
        CuentaPorCobrarEntity entity = new CuentaPorCobrarEntity();
        entity.setServicio(servicio);
        entity.setPeriodo(cuenta.getPeriodo());
        entity.setFechaEmision(cuenta.getFechaEmision());
        entity.setFechaVencimiento(cuenta.getFechaVencimiento());

        if (cuenta.getEstado() == null) {
            entity.setEstado("PENDIENTE");
        } else {
            entity.setEstado(cuenta.getEstado());
        }
        if (cuenta.getPuestoId() != null) {
            PuestoEntity puesto = iPuestoRepository
                    .findById(cuenta.getPuestoId())
                    .orElseThrow(() -> new IllegalArgumentException("El puesto indicado no existe"));
            entity.setPuesto(puesto);
            entity.setSocio(null);
        }
        if (cuenta.getSocioId() != null) {
            SocioEntity socio = iSocioRepository
                    .findById(cuenta.getSocioId())
                    .orElseThrow(() -> new IllegalArgumentException("El socio indicado no existe"));
            entity.setSocio(socio);
            entity.setPuesto(null);
        }
        calcularMonto(entity, cuenta, servicio);
        CuentaPorCobrarEntity guardado = iCuentaRepository.save(entity);
        return get(guardado.getId());
    }
    @Override
    @Transactional(readOnly = true)
    public CuentaPorCobrar get(Long id) {
        return iCuentaRepository
                .findByIdConRelaciones(id)
                .map(this::convertirAModelo)
                .orElse(null);
    }
    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrar> getAll() {
        return iCuentaRepository
                .findAllConRelaciones()
                .stream()
                .map(this::convertirAModelo)
                .toList();
    }

    private void validarDestino(CuentaPorCobrar cuenta, ServicioEntity servicio) {
        boolean tieneSocio = cuenta.getSocioId() != null;
        boolean tienePuesto = cuenta.getPuestoId() != null;
        if (tieneSocio == tienePuesto) {
            throw new IllegalArgumentException("La cuenta debe pertenecer a un socio o a un puesto, pero no a ambos");
        }
        if ("PUESTO".equals(servicio.getCargoA())
                && !tienePuesto) {
            throw new IllegalArgumentException("Este servicio debe cargarse a un puesto");
        }

        if ("SOCIO".equals(servicio.getCargoA())
                && !tieneSocio) {

            throw new IllegalArgumentException(
                    "Este servicio debe cargarse a un socio"
            );
        }

        if (cuenta.getFechaVencimiento()
                .isBefore(cuenta.getFechaEmision())) {
            throw new IllegalArgumentException(
                    "La fecha de vencimiento no puede ser anterior a la fecha de emisión"
            );
        }
    }

    private void calcularMonto(CuentaPorCobrarEntity entity, CuentaPorCobrar cuenta, ServicioEntity servicio) {
        if ("CONSUMO".equals(servicio.getTipoCalculo())) {
            if (cuenta.getLecturaInicial() == null
                    || cuenta.getLecturaFinal() == null) {
                throw new IllegalArgumentException("Debe ingresar lectura inicial y final");
            }
            BigDecimal diferencia =
                    cuenta.getLecturaFinal()
                            .subtract(cuenta.getLecturaInicial());

            if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
                diferencia = BigDecimal.ZERO;
            }

            entity.setLecturaInicial(cuenta.getLecturaInicial());
            entity.setLecturaFinal(cuenta.getLecturaFinal());
            entity.setCostoUnitario(servicio.getCosto());
            entity.setMonto(diferencia.multiply(servicio.getCosto()));
        } else {
            entity.setLecturaInicial(null);
            entity.setLecturaFinal(null);
            entity.setCostoUnitario(servicio.getCosto());

            entity.setMonto(
                    cuenta.getMonto() != null
                            ? cuenta.getMonto()
                            : servicio.getCosto()
            );
        }
    }

    private CuentaPorCobrar convertirAModelo(CuentaPorCobrarEntity entity) {
        return CuentaPorCobrar.builder()
                .id(entity.getId())
                .servicioId(entity.getServicio().getId())
                .servicioNombre(entity.getServicio().getNombre())
                .socioId(entity.getSocio() != null
                                ? entity.getSocio().getId()
                                : null
                )
                .socioNombre(entity.getSocio() != null
                                ? entity.getSocio().getNombres()
                                  + " "
                                  + entity.getSocio().getApellidos()
                                : null)
                .puestoId(entity.getPuesto() != null
                                ? entity.getPuesto().getId()
                                : null)

                .puestoNumero(entity.getPuesto() != null
                                ? entity.getPuesto().getNumero()
                                : null)
                .periodo(entity.getPeriodo())
                .lecturaInicial(entity.getLecturaInicial())
                .lecturaFinal(entity.getLecturaFinal())
                .costoUnitario(entity.getCostoUnitario())
                .monto(entity.getMonto())
                .fechaEmision(entity.getFechaEmision())
                .fechaVencimiento(entity.getFechaVencimiento())
                .estado(entity.getEstado())
                .build();
    }

    @Override
    @Transactional
    public List<CuentaPorCobrar> generarParaPuestos(GenerarCuentasPuestosRequest request) {
        ServicioEntity servicio = iServicioRepository
                .findById(request.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("El servicio indicado no existe"));
        if (!Boolean.TRUE.equals(servicio.getEstado())) {
            throw new IllegalArgumentException("El servicio se encuentra inactivo");
        }

        if (!"PUESTO".equals(servicio.getCargoA())) {
            throw new IllegalArgumentException("El servicio seleccionado no corresponde a puestos");
        }

        if (!"FIJO".equals(servicio.getTipoCalculo())) {
            throw new IllegalArgumentException("Los servicios por consumo requieren lecturas por cada puesto");
        }

        if (request.getFechaVencimiento()
                .isBefore(request.getFechaEmision())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de emisión");
        }

        List<PuestoEntity> puestos = iPuestoRepository.findByEstado("OCUPADO");
        List<CuentaPorCobrar> cuentasGeneradas = new ArrayList<>();
        BigDecimal monto = request.getMonto() != null
                ? request.getMonto()
                : servicio.getCosto();
        for (PuestoEntity puesto : puestos) {
            boolean existe =
                    iCuentaRepository.existeCuentaPuestoPeriodo(
                            servicio.getId(),
                            puesto.getId(),
                            request.getPeriodo()
                    );
            if (existe) {
                continue;
            }
            CuentaPorCobrarEntity cuenta =
                    CuentaPorCobrarEntity.builder()
                            .servicio(servicio)
                            .puesto(puesto)
                            .socio(null)
                            .periodo(request.getPeriodo())
                            .costoUnitario(servicio.getCosto())
                            .monto(monto)
                            .fechaEmision(request.getFechaEmision())
                            .fechaVencimiento(request.getFechaVencimiento())
                            .estado("PENDIENTE")
                            .build();
            CuentaPorCobrarEntity guardada = iCuentaRepository.save(cuenta);
            cuentasGeneradas.add(convertirAModelo(guardada));
        }
        return cuentasGeneradas;
    }

    @Override
    @Transactional
    public List<CuentaPorCobrar> generarParaSocios(GenerarCuentasSociosRequest request) {
        ServicioEntity servicio = iServicioRepository
                .findById(request.getServicioId())
                .orElseThrow(() ->
                        new IllegalArgumentException("El servicio indicado no existe"));
        if (!Boolean.TRUE.equals(servicio.getEstado())) {
            throw new IllegalArgumentException("El servicio se encuentra inactivo");
        }
        if (!"SOCIO".equals(servicio.getCargoA())) {
            throw new IllegalArgumentException("El servicio seleccionado no corresponde a socios");
        }
        /*
         * Este endpoint genera cuentas masivamente.
         * Un servicio por consumo necesitaría lecturas
         * individuales para cada destinatario.
         */
        if ("CONSUMO".equals(servicio.getTipoCalculo())) {
            throw new IllegalArgumentException("Los servicios por consumo requieren datos individuales");
        }

        if (request.getFechaVencimiento()
                .isBefore(request.getFechaEmision())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de emisión");
        }
        List<SocioEntity> socios = iSocioRepository.findByEtapas(request.getEtapas());
        /*
         * RN-06:
         * Si sociosUnicos = true,
         * evitamos repetidos por nombres + apellidos.
         */
        if (Boolean.TRUE.equals(request.getSociosUnicos())) {
            Map<String, SocioEntity> sociosSinDuplicar = new LinkedHashMap<>();
            for (SocioEntity socio : socios) {
                String clave =
                        (socio.getNombres()
                                + "|"
                                + socio.getApellidos())
                                .trim()
                                .toLowerCase(Locale.ROOT);

                sociosSinDuplicar.putIfAbsent(
                        clave,
                        socio
                );
            }

            socios = new ArrayList<>(sociosSinDuplicar.values());
        }

        BigDecimal monto = request.getMonto() != null
                ? request.getMonto()
                : servicio.getCosto();
        List<CuentaPorCobrar> cuentasGeneradas = new ArrayList<>();
        for (SocioEntity socio : socios) {
            boolean existe = iCuentaRepository
                            .existeCuentaSocioPeriodo(servicio.getId(),
                                    socio.getId(), request.getPeriodo());
            if (existe) {
                continue;
            }
            CuentaPorCobrarEntity cuenta = CuentaPorCobrarEntity.builder()
                            .servicio(servicio)
                            .socio(socio)
                            .puesto(null)
                            .periodo(request.getPeriodo())
                            .lecturaInicial(null)
                            .lecturaFinal(null)
                            .costoUnitario(servicio.getCosto())
                            .monto(monto)
                            .fechaEmision(request.getFechaEmision())
                            .fechaVencimiento(request.getFechaVencimiento())
                            .estado("PENDIENTE")
                            .build();

            CuentaPorCobrarEntity guardada = iCuentaRepository.save(cuenta);
            cuentasGeneradas.add(convertirAModelo(guardada));
        }
        return cuentasGeneradas;
    }
}