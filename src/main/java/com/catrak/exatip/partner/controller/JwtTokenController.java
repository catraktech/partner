package com.catrak.exatip.partner.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Api(value = "partner", description = "the token API")
public class JwtTokenController {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenController.class);

    @Autowired
    private JwtTokenService tokenEndpointService;

    @Autowired
    private Utility utility;

    @ApiOperation(value = "Provide JWT access token for valid user", notes = "API will return valid jwt token with expiry of 24 hours for valid user based on userName and password")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Token generated successfully", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    @PostMapping("")
    public ResponseEntity<?> generateToken(@RequestHeader(name = "Api-Key") String apiKey) {
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