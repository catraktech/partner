package com.catrak.exatip.partner.service;

import com.catrak.exatip.commonlib.entity.PartnerInfo;
import com.catrak.exatip.partner.exception.PartnerException;

public interface RegisterPartnerService {

    PartnerInfo registerPartner(PartnerInfo partnerInfo, String requestUUID) throws PartnerException;

    PartnerInfo renewApiKey(PartnerInfo partnerInfo, String requestUUID) throws PartnerException;

}
