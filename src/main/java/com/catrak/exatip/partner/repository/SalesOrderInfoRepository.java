package com.catrak.exatip.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.catrak.exatip.commonlib.entity.SalesOrderInfo;

@Repository
public interface SalesOrderInfoRepository extends JpaRepository<SalesOrderInfo, Long> {

    @Query("select count(soi) > 0 from SalesOrderInfo soi where soi.salesOrderNumber = ?1")
    boolean existsBySalesOrderNumber(Long salesOrderNumber);

    @Query("select count(soi) > 0 from SalesOrderInfo soi where soi.can = ?1")
    boolean existsByCan(Integer can);

}