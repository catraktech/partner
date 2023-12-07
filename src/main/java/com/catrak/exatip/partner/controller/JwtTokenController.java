package com.catrak.exatip.partner.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catrak.exatip.commonlib.dto.JsonResponseDTO;
import com.catrak.exatip.partner.service.JwtTokenService;
import com.catrak.exatip.partner.util.Utility;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/token")
@Api(value = "Custom authentication API used when user tries to login in application using OTP in place of password")
public class JwtTokenController {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenController.class);

    @Autowired
    private JwtTokenService tokenEndpointService;

    @Autowired
    private Utility utility;

    @ApiOperation(value = "Provide JWT access token for valid user.", notes = "Valid username and password should be provided by the user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid user name and password is provided by user. Application returns valid token with  expiration date and time.", response = OAuth2AccessToken.class),
            @ApiResponse(code = 400, message = "Invalid user name and password is provided by user or it has expired. It is mandatory to provide valid user name along with password other wise application will fail validation."),
            @ApiResponse(code = 500, message = "Internal run time error has occured.") })
    @PostMapping("")
    public ResponseEntity<?> generateToken(@RequestHeader(name = "Api-Key") String apiKey) {
        String requestUUID = UUID.randomUUID().toString();
        log.info("Inside TokenJwtController generateToken");
        Gson g = new Gson();
        Map<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("apiKey", apiKey);

        StringBuilder validateField = utility.validateMandField(mandatoryFields);

        if (validateField.length() > 0) {
            return new ResponseEntity<>(
                    new JsonResponseDTO<>(validateField.toString(), false, JsonResponseDTO.BAD_REQUEST), HttpStatus.OK);
        }
        String token = tokenEndpointService.generateToken(apiKey);
        String jwtToken = "{" + "\"token\":\"" + token + "\"}";
        String registerPartnerResponse = g
                .toJson(new JsonResponseDTO<>(jwtToken, "Token generated successfully", true, JsonResponseDTO.OK));

        log.info("Exit TokenEndpointController generateToken");
        return new ResponseEntity<>(registerPartnerResponse, HttpStatus.OK);
    }

}