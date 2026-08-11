package com.cibertec.galcom.services;

import com.cibertec.galcom.models.CuentaPorCobrar;
import com.cibertec.galcom.models.GenerarCuentasPuestosRequest;
import com.cibertec.galcom.models.GenerarCuentasSociosRequest;

import java.util.List;

public interface ICuentaPorCobrarService {
    CuentaPorCobrar create(CuentaPorCobrar cuenta);
    CuentaPorCobrar get(Long id);
    List<CuentaPorCobrar> getAll();
    List<CuentaPorCobrar> generarParaPuestos(GenerarCuentasPuestosRequest request);
    List<CuentaPorCobrar> generarParaSocios(GenerarCuentasSociosRequest request);
}