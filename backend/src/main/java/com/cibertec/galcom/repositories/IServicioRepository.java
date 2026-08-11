package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IServicioRepository extends JpaRepository<ServicioEntity, Long> {
}
