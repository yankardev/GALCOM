package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Socio;
import java.util.List;

public interface ISocioService {
    Socio create(Socio socio);
    Socio get(Long id);
    List<Socio> getAll();
    Socio update(Long id, Socio socio);
    void delete(Long id);
}
