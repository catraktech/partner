package com.catrak.exatip.partner.service.impl;

import java.sql.Timestamp;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catrak.exatip.commonlib.entity.PartnerInfo;
import com.catrak.exatip.partner.exception.PartnerException;
import com.catrak.exatip.partner.repository.PartnerInfoRepository;
import com.catrak.exatip.partner.service.JwtTokenService;
import com.catrak.exatip.partner.util.JwtTokenCreator;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    @Autowired
    private PartnerInfoRepository partnerInfoRepository;

    @Autowired
    private JwtTokenCreator jwtTokenCreator;

    @Override
    public String generateToken(String apiKey) throws PartnerException {
        log.info("Inside JwtTokenServiceImpl generateToken");
        Optional<PartnerInfo> partnerOptional = partnerInfoRepository.findByApiKey(apiKey);
        if (!partnerOptional.isPresent()) {
            throw new PartnerException("invalid api-key");
        }
        if (partnerOptional.get().getExpirationDateTime().before(new Timestamp(System.currentTimeMillis()))) {
            throw new PartnerException("api-key expires, please renew the api-key");
        }
        return jwtTokenCreator.generateToken(partnerOptional.get().getUserName());
    }

}
