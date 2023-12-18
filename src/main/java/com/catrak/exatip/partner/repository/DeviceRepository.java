package com.catrak.exatip.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catrak.exatip.commonlib.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

}