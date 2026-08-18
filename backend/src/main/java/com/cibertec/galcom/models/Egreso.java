package com.cibertec.galcom.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Egreso {
    private Long id;
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private BigDecimal subtotal;

    @PositiveOrZero(message = "El impuesto no puede ser negativo")
    private BigDecimal impuesto;

    @PositiveOrZero(message = "El monto total no puede ser negativo")
    private BigDecimal montoTotal;

    private String documentoAsociado;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String archivoOrigen;
    private String estado;
    private Long bancoId;
    private String bancoNombre;
    private Long reciboId;
    private String numeroRecibo;
}
