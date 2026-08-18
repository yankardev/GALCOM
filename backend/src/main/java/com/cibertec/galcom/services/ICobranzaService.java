package com.cibertec.galcom.services;

import com.cibertec.galcom.models.*;

import java.util.List;

public interface ICobranzaService {
    List<CuentaPorCobrar> cuentasSocio(Long socioId);
    List<CuentaPorCobrar> cuentasPuesto(Long puestoId);
    PagoResponse procesarPago(PagoRequest request);
    Recibo canjear(CanjeBancarioRequest request);
    ResumenResponsable resumen(String tipo, Long id);
}
