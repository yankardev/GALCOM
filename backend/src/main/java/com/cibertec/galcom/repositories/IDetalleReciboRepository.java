package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.DetalleReciboEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDetalleReciboRepository extends JpaRepository<DetalleReciboEntity, Long> {
}
