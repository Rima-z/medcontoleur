package com.stage.backend.repository;

import com.stage.backend.entity.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin,Long> {

    Optional<Medecin> findById(Long aLong);

    Boolean existsByEmail(String email);

    @Query("select p from Medecin p where p.email=:email")
    Optional<Medecin> findByEmail(@Param("email")String email);
}


