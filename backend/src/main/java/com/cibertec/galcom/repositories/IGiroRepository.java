package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.GiroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGiroRepository extends JpaRepository<GiroEntity, Long> {
}
