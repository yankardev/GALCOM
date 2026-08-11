package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Servicio;
import java.util.List;

public interface IServicioService {
    Servicio create(Servicio servicio);
    Servicio get(Long id);
    List<Servicio> getAll();
    Servicio update(Long id, Servicio servicio);
    void delete(Long id);
}
