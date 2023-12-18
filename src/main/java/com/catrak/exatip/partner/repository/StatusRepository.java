package com.catrak.exatip.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catrak.exatip.commonlib.entity.Status;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
}