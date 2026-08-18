package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.*;
import com.cibertec.galcom.models.Recibo;
import com.cibertec.galcom.models.ReciboDetalle;
import com.cibertec.galcom.repositories.IReciboRepository;
import com.cibertec.galcom.repositories.ISecuenciaReciboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReciboSupportService {
    private final IReciboRepository repository;
    private final ISecuenciaReciboRepository secuenciaRepository;

    /**
     * Genera el correlativo dentro de la misma transacción del pago/ingreso/egreso.
     * El bloqueo pesimista evita que dos operaciones simultáneas obtengan el mismo número.
     */
    public String siguienteCorrelativo() {
        SecuenciaReciboEntity secuencia = secuenciaRepository.bloquearSecuencia()
                .orElseGet(() -> secuenciaRepository.saveAndFlush(
                        SecuenciaReciboEntity.builder()
                                .id(1)
                                .ultimo(repository.findTopByOrderByIdDesc().map(ReciboEntity::getId).orElse(0L))
                                .build()
                ));

        long siguiente = secuencia.getUltimo() + 1;
        secuencia.setUltimo(siguiente);
        secuenciaRepository.save(secuencia);
        return "REC-%06d".formatted(siguiente);
    }

    public ReciboEntity nuevo(String tipo, UsuarioEntity usuario, SocioEntity socio, PuestoEntity puesto,
                              BigDecimal monto, String metodoPago) {
        return ReciboEntity.builder()
                .numeroCorrelativo(siguienteCorrelativo())
                .tipo(tipo)
                .usuario(usuario)
                .socio(socio)
                .puesto(puesto)
                .montoTotal(monto)
                .metodoPago(metodoPago)
                .estado("EMITIDO")
                .build();
    }

    public Recibo toModel(ReciboEntity entity, boolean incluirDetalles) {
        List<ReciboDetalle> detalles = incluirDetalles && entity.getDetalles() != null
                ? entity.getDetalles().stream().map(d -> ReciboDetalle.builder()
                    .cuentaId(d.getCuenta().getId())
                    .servicio(d.getCuenta().getServicio().getNombre())
                    .periodo(d.getCuenta().getPeriodo())
                    .montoAplicado(d.getMontoAplicado())
                    .build()).toList()
                : List.of();

        return Recibo.builder()
                .id(entity.getId())
                .numeroCorrelativo(entity.getNumeroCorrelativo())
                .tipo(entity.getTipo())
                .usuario(entity.getUsuario() != null ? entity.getUsuario().getUsuario() : null)
                .socioId(entity.getSocio() != null ? entity.getSocio().getId() : null)
                .socioNombre(entity.getSocio() != null ? entity.getSocio().getNombres() + " " + entity.getSocio().getApellidos() : null)
                .puestoId(entity.getPuesto() != null ? entity.getPuesto().getId() : null)
                .puestoNumero(entity.getPuesto() != null ? entity.getPuesto().getNumero() : null)
                .fecha(entity.getFecha())
                .montoTotal(entity.getMontoTotal())
                .metodoPago(entity.getMetodoPago())
                .estado(entity.getEstado())
                .detalles(detalles)
                .build();
    }
}
