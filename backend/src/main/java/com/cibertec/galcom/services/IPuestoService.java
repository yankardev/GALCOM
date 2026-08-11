package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Puesto;
import java.util.List;

public interface IPuestoService {
    Puesto create(Puesto puesto);
    Puesto get(Long id);
    List<Puesto> getAll();
    Puesto update(Long id, Puesto puesto);
    void delete(Long id);
}
