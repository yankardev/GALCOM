package com.cibertec.galcom.services;

import java.time.LocalDate;

public interface IReporteService {
    byte[] generar(String tipo, LocalDate fecha, String mes);
}
