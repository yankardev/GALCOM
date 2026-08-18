package com.cibertec.galcom.services;

import com.cibertec.galcom.models.MovimientoBancario;
import java.time.LocalDate;
import java.util.List;

public interface IMovimientoBancarioService {
    List<MovimientoBancario> buscar(LocalDate fecha);
}
