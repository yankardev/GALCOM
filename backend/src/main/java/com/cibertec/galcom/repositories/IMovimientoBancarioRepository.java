package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.MovimientoBancarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IMovimientoBancarioRepository extends JpaRepository<MovimientoBancarioEntity, Long> {
    @Query("""
        SELECT m FROM MovimientoBancarioEntity m
        JOIN FETCH m.banco
        LEFT JOIN FETCH m.cuenta
        LEFT JOIN FETCH m.recibo
        WHERE (:fecha IS NULL OR m.fechaDeposito = :fecha)
        ORDER BY m.fechaDeposito DESC, m.id DESC
    """)
    List<MovimientoBancarioEntity> buscar(@Param("fecha") LocalDate fecha);
}
