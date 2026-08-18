package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.AuditoriaMovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuditoriaMovimientoRepository extends JpaRepository<AuditoriaMovimientoEntity, Long> {
}
