package com.cibertec.galcom.services;

import com.cibertec.galcom.models.Banco;
import java.util.List;

public interface IBancoService {
    Banco create(Banco banco);
    Banco get(Long id);
    List<Banco> getAll();
    Banco update(Long id, Banco banco);
    void delete(Long id);
}
