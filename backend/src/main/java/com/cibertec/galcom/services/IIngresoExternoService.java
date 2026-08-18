package com.cibertec.galcom.services;

import com.cibertec.galcom.models.IngresoExterno;

import java.time.LocalDate;
import java.util.List;

public interface IIngresoExternoService {
    IngresoExterno create(IngresoExterno ingreso);
    List<IngresoExterno> buscar(LocalDate inicio, LocalDate fin);
}
