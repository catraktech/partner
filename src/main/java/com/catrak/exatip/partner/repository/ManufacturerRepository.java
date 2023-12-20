package com.catrak.exatip.partner.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.catrak.exatip.commonlib.entity.Manufacturer;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

    @Query(value = "select * from manufacturer m where lower(m.\"name\") = lower(?1)", nativeQuery = true)
    Optional<Manufacturer> findByName(String manufacturerName);

}
