package com.catrak.exatip.partner.service;

import com.catrak.exatip.partner.exception.PartnerException;

public interface JwtTokenService {

    String generateToken(String apiKey) throws PartnerException;
}