package com.catrak.exatip.partner.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catrak.exatip.commonlib.entity.PartnerInfo;

@Repository
public interface PartnerInfoRepository extends JpaRepository<PartnerInfo, Long> {

    Optional<PartnerInfo> findByPartner(String partner);

    Optional<PartnerInfo> findByContactNumber(String phoneNumber);

    Optional<PartnerInfo> findByEmail(String email);

    Optional<PartnerInfo> findByUserName(String userName);
}