package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.PuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPuestoRepository extends JpaRepository<PuestoEntity, Long> {
    @Query("SELECT p FROM PuestoEntity p LEFT JOIN FETCH p.socio JOIN FETCH p.giro")
    List<PuestoEntity> findAllConRelaciones();

    @Query("SELECT p FROM PuestoEntity p LEFT JOIN FETCH p.socio JOIN FETCH p.giro WHERE p.id = :id")
    Optional<PuestoEntity> findByIdConRelaciones(@Param("id") Long id);

    @Query("SELECT p FROM PuestoEntity p WHERE p.estado = :estado")
    List<PuestoEntity> findByEstado(@Param("estado") String estado);

    long countByEstado(String estado);
}
