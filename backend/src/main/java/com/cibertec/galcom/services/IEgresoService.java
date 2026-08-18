package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Egreso;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface IEgresoService {
    Egreso create(Egreso egreso);
    List<Egreso> buscar(LocalDate inicio, LocalDate fin);
    Egreso procesar(Long id);
    Egreso anular(Long id);
    List<Egreso> importarCsv(MultipartFile archivo);
}
