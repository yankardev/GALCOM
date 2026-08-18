package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.SecuenciaReciboEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISecuenciaReciboRepository extends JpaRepository<SecuenciaReciboEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SecuenciaReciboEntity s WHERE s.id = 1")
    Optional<SecuenciaReciboEntity> bloquearSecuencia();
}
