package com.catrak.exatip.partner.service;

import com.catrak.exatip.partner.dto.SalesOrderInfoDTO;
import com.catrak.exatip.partner.exception.PartnerException;

public interface SalesOrderService {

    SalesOrderInfoDTO save(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) throws PartnerException;

}
