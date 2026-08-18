package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.*;
import com.cibertec.galcom.models.Egreso;
import com.cibertec.galcom.repositories.*;
import com.cibertec.galcom.services.IEgresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EgresoService implements IEgresoService {
    private final IEgresoRepository repository;
    private final IBancoRepository bancoRepository;
    private final IReciboRepository reciboRepository;
    private final IMovimientoBancarioRepository movimientoRepository;
    private final CurrentUserService currentUserService;
    private final AuditoriaService auditoriaService;
    private final ReciboSupportService reciboSupport;

    @Override
    @Transactional
    public Egreso create(Egreso egreso) {
        UsuarioEntity usuario = currentUserService.get();
        // Normalizamos los datos principales del comprobante
        String tipoDocumento = egreso.getTipoDocumento() == null
                ? ""
                : egreso.getTipoDocumento().trim();
        String numeroDocumento = egreso.getNumeroDocumento() == null
                ? ""
                : egreso.getNumeroDocumento().trim();
        String proveedor = egreso.getProveedor() == null
                ? ""
                : egreso.getProveedor().trim();
        if (tipoDocumento.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de documento es obligatorio"
            );
        }
        if (numeroDocumento.isBlank()) {
            throw new IllegalArgumentException(
                    "El número de documento es obligatorio"
            );
        }
        if (proveedor.isBlank()) {
            throw new IllegalArgumentException(
                    "El proveedor es obligatorio"
            );
        }
        // Evitar registrar dos veces el mismo comprobante
        boolean existe = repository
                .existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCaseAndProveedorIgnoreCase(
                        tipoDocumento,
                        numeroDocumento,
                        proveedor
                );
        if (existe) {
            throw new IllegalArgumentException(
                    "Ya existe el " +
                            tipoDocumento.toLowerCase() +
                            " " +
                            numeroDocumento +
                            " registrado para el proveedor " +
                            proveedor +
                            "."
            );
        }
        BancoEntity banco = egreso.getBancoId() == null
                ? null
                : bancoRepository.findById(egreso.getBancoId())
                .orElseThrow(() ->
                             new IllegalArgumentException(
                                     "El banco indicado no existe"
                             )
                );
        BigDecimal subtotal = egreso.getSubtotal() == null
                ? BigDecimal.ZERO
                : egreso.getSubtotal();
        BigDecimal impuesto = egreso.getImpuesto() == null
                ? BigDecimal.ZERO
                : egreso.getImpuesto();
        // El backend vuelve a calcular el total
        BigDecimal montoTotal = subtotal.add(impuesto);

        if (montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto total del egreso debe ser mayor que cero"
            );
        }
        EgresoEntity entity = repository.save(
                EgresoEntity.builder()
                        .tipoDocumento(tipoDocumento)
                        .numeroDocumento(numeroDocumento)
                        .proveedor(proveedor)
                        .fecha(egreso.getFecha())
                        .subtotal(subtotal)
                        .impuesto(impuesto)
                        .montoTotal(montoTotal)
                        .documentoAsociado(
                                egreso.getDocumentoAsociado()
                        )
                        .motivo(egreso.getMotivo())

                        .archivoOrigen(
                                egreso.getArchivoOrigen()
                        )

                        .estado("REGISTRADO")

                        .banco(banco)
                        .usuario(usuario)

                        .build()
        );

        auditoriaService.registrar(
                usuario,
                "REGISTRAR",
                "EGRESO",
                entity.getId(),
                entity.getMontoTotal(),
                entity.getMotivo()
        );

        return toModel(entity);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Egreso> buscar(LocalDate inicio, LocalDate fin) {
        return repository.buscar(inicio, fin).stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public Egreso procesar(Long id) {
        EgresoEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("El egreso indicado no existe"));
        if ("ANULADO".equals(entity.getEstado())) throw new IllegalArgumentException("El egreso se encuentra anulado");
        if ("PROCESADO".equals(entity.getEstado())) return toModel(entity);
        UsuarioEntity usuario = currentUserService.get();
        ReciboEntity recibo = reciboSupport.nuevo("EGRESO", usuario, null, null, entity.getMontoTotal(), entity.getBanco() == null ? "EFECTIVO" : "TRANSFERENCIA");
        recibo = reciboRepository.save(recibo);
        entity.setRecibo(recibo);
        entity.setEstado("PROCESADO");
        repository.save(entity);
        if (entity.getBanco() != null) {
            movimientoRepository.save(MovimientoBancarioEntity.builder().banco(entity.getBanco()).usuario(usuario).recibo(recibo)
                    .tipo("RETIRO").fechaDeposito(entity.getFecha()).monto(entity.getMontoTotal())
                    .observaciones("Egreso: " + entity.getMotivo()).build());
        }
        auditoriaService.registrar(usuario, "PROCESAR", "EGRESO", entity.getId(), entity.getMontoTotal(), recibo.getNumeroCorrelativo());
        return toModel(entity);
    }

    @Override
    @Transactional
    public Egreso anular(Long id) {
        EgresoEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("El egreso indicado no existe"));
        if ("ANULADO".equals(entity.getEstado())) return toModel(entity);
        entity.setEstado("ANULADO");
        if (entity.getRecibo() != null) entity.getRecibo().setEstado("ANULADO");
        UsuarioEntity usuario = currentUserService.get();
        auditoriaService.registrar(usuario, "ANULAR", "EGRESO", entity.getId(), entity.getMontoTotal(), entity.getMotivo());
        return toModel(repository.save(entity));
    }

    @Override
    @Transactional
    public List<Egreso> importarCsv(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("Debe seleccionar un archivo CSV");
        List<Egreso> creados = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean primera = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (primera && line.toLowerCase().contains("proveedor")) { primera = false; continue; }
                primera = false;
                String[] p = line.split(",", -1);
                if (p.length < 9) throw new IllegalArgumentException("Formato CSV inválido. Se esperan al menos 9 columnas");
                Egreso e = Egreso.builder()
                        .tipoDocumento(v(p,0)).numeroDocumento(v(p,1)).proveedor(v(p,2)).fecha(LocalDate.parse(v(p,3)))
                        .subtotal(decimal(v(p,4))).impuesto(decimal(v(p,5))).montoTotal(decimal(v(p,6)))
                        .documentoAsociado(v(p,7)).motivo(v(p,8)).archivoOrigen(archivo.getOriginalFilename())
                        .bancoId(p.length > 9 && !v(p,9).isBlank() ? Long.valueOf(v(p,9)) : null).build();
                creados.add(create(e));
            }
        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Importación cancelada. No se registró ningún egreso. Motivo: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "No se pudo procesar el archivo: " + e.getMessage()
            );
        }
        return creados;
    }

    private String v(String[] p, int i) { return p[i].trim().replace("\"", ""); }
    private BigDecimal decimal(String s) { return s == null || s.isBlank() ? BigDecimal.ZERO : new BigDecimal(s); }

    private Egreso toModel(EgresoEntity e) {
        return Egreso.builder().id(e.getId()).tipoDocumento(e.getTipoDocumento()).numeroDocumento(e.getNumeroDocumento())
                .proveedor(e.getProveedor()).fecha(e.getFecha()).subtotal(e.getSubtotal()).impuesto(e.getImpuesto())
                .montoTotal(e.getMontoTotal()).documentoAsociado(e.getDocumentoAsociado()).motivo(e.getMotivo())
                .archivoOrigen(e.getArchivoOrigen()).estado(e.getEstado())
                .bancoId(e.getBanco() != null ? e.getBanco().getId() : null).bancoNombre(e.getBanco() != null ? e.getBanco().getNombre() : null)
                .reciboId(e.getRecibo() != null ? e.getRecibo().getId() : null).numeroRecibo(e.getRecibo() != null ? e.getRecibo().getNumeroCorrelativo() : null).build();
    }
}
