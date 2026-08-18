package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.SocioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ISocioRepository extends JpaRepository<SocioEntity, Long> {

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    @Query("SELECT s FROM SocioEntity s WHERE s.etapa IN :etapas")
    List<SocioEntity> findByEtapas(@Param("etapas") List<Integer> etapas);
}
