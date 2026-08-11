package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.BancoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBancoRepository extends JpaRepository<BancoEntity, Long> {
}
