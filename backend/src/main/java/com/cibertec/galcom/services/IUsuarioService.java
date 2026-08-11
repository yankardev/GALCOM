package com.cibertec.galcom.services;

import com.cibertec.galcom.models.LoginResponse;
import com.cibertec.galcom.models.Usuario;

public interface IUsuarioService {
    LoginResponse login(Usuario usuario);
}
