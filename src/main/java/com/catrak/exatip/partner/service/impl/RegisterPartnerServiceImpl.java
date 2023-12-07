package com.catrak.exatip.partner.service.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catrak.exatip.commonlib.entity.PartnerInfo;
import com.catrak.exatip.partner.exception.PartnerException;
import com.catrak.exatip.partner.repository.PartnerInfoRepository;
import com.catrak.exatip.partner.service.RegisterPartnerService;

@Service
public class RegisterPartnerServiceImpl implements RegisterPartnerService {
    private static final Logger log = LoggerFactory.getLogger(RegisterPartnerServiceImpl.class);

    @Autowired
    private PartnerInfoRepository partnerInfoRepository;

    @Override
    public PartnerInfo registerPartner(PartnerInfo partnerInfo, String requestUUID) throws PartnerException {

        log.info("RequestUUID: {} Inside RegisterPartnerServiceImpl registerPartner", requestUUID);

        Optional<PartnerInfo> partner = partnerInfoRepository.findByPartner(partnerInfo.getPartner());
        if (partner.isPresent()) {
            throw new PartnerException("partner name already exists");
        }

        Optional<PartnerInfo> partnerContact = partnerInfoRepository
                .findByContactNumber(partnerInfo.getContactNumber());
        if (partnerContact.isPresent()) {
            throw new PartnerException("contact number already exists");
        }

        Optional<PartnerInfo> partnerEmail = partnerInfoRepository.findByEmail(partnerInfo.getEmail());
        if (partnerEmail.isPresent()) {
            throw new PartnerException("email already exists");
        }

        Optional<PartnerInfo> partnerUserName = partnerInfoRepository.findByUserName(partnerInfo.getUserName());
        if (partnerUserName.isPresent()) {
            throw new PartnerException("userName already exists");
        }

        partnerInfo.setLastModifiedDatetime(new Timestamp(System.currentTimeMillis()));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 6);
        Timestamp expirationTime = new Timestamp(c.getTimeInMillis());
        partnerInfo.setExpirationDateTime(expirationTime);

        partnerInfo.setApiKey(UUID.randomUUID().toString());

        partnerInfo.setPassword(partnerInfo.getPassword());

        partnerInfoRepository.save(partnerInfo);

        log.info("RequestUUID: {} Exit RegisterPartnerServiceImpl registerPartner", requestUUID);

        return partnerInfo;
    }

}
