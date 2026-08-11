package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Giro;
import java.util.List;

public interface IGiroService {
    Giro create(Giro giro);
    Giro get(Long id);
    List<Giro> getAll();
    Giro update(Long id, Giro giro);
    void delete(Long id);
}
