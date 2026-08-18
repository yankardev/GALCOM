package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Recibo;

import java.time.LocalDate;
import java.util.List;

public interface IReciboService {
    Recibo get(Long id);
    List<Recibo> buscar(LocalDate fecha, String tipo);
}
