package com.catrak.exatip.partner.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catrak.exatip.commonlib.dto.JsonResponseDTO;
import com.catrak.exatip.commonlib.entity.PartnerInfo;
import com.catrak.exatip.partner.exception.PartnerException;
import com.catrak.exatip.partner.service.RegisterPartnerService;
import com.catrak.exatip.partner.util.Utility;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/register")
@Api(value = "partner", description = "the partner API")
public class RegisterPartnerController {
    private static final Logger log = LoggerFactory.getLogger(RegisterPartnerController.class);

    @Autowired
    public RegisterPartnerService registerPartnerService;

    @Autowired
    private Utility utility;

    @ApiOperation(value = "Register partner with CATrak", nickname = "register", notes = "partner registration with catrak", response = String.class, tags = {})
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    @PostMapping("")
    public ResponseEntity<?> registerPartner(@ApiParam(value = "") @Valid @RequestBody String registerPartnerPayload) {

        String requestUUID = UUID.randomUUID().toString();

        log.info("RequestUUID: {} Inside RegisterPartnerController registerPartner", requestUUID);
        Gson g = new Gson();
        PartnerInfo partnerInfo = g.fromJson(registerPartnerPayload, PartnerInfo.class);

        StringBuilder validateField = validateMandatoryFields(partnerInfo, false);

        if (validateField.length() > 0) {
            return new ResponseEntity<>(
                    new JsonResponseDTO<>(validateField.toString(), false, JsonResponseDTO.BAD_REQUEST), HttpStatus.OK);
        }

        validate(partnerInfo, false);

        partnerInfo = registerPartnerService.registerPartner(partnerInfo, requestUUID);

        String apiKey = "{" + "\"apiKey\":\"" + partnerInfo.getApiKey() + "\"}";

        String registerPartnerResponse = g.toJson(
                new JsonResponseDTO<>(apiKey, "Partner registration is successful", true, JsonResponseDTO.CREATED));

        log.info("RequestUUID: {} Exit RegisterPartnerController registerPartner", requestUUID);
        return new ResponseEntity<>(registerPartnerResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Renew partner api-key", nickname = "register", notes = "api-key renew of partner", response = String.class, tags = {})
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    @PostMapping("/renew")
    public ResponseEntity<?> renewApiKey(@ApiParam(value = "") @Valid @RequestBody String renewApiKeyPayload) {

        String requestUUID = UUID.randomUUID().toString();

        log.info("RequestUUID: {} Inside RegisterPartnerController renewApiKey", requestUUID);
        Gson g = new Gson();
        PartnerInfo partnerInfo = g.fromJson(renewApiKeyPayload, PartnerInfo.class);

        StringBuilder validateField = validateMandatoryFields(partnerInfo, true);

        if (validateField.length() > 0) {
            return new ResponseEntity<>(
                    new JsonResponseDTO<>(validateField.toString(), false, JsonResponseDTO.BAD_REQUEST), HttpStatus.OK);
        }

        validate(partnerInfo, true);

        partnerInfo = registerPartnerService.renewApiKey(partnerInfo, requestUUID);

        String apiKey = "{" + "\"apiKey\":\"" + partnerInfo.getApiKey() + "\"}";

        String registerPartnerResponse = g
                .toJson(new JsonResponseDTO<>(apiKey, "Api-key renewed successfully", true, JsonResponseDTO.CREATED));

        log.info("RequestUUID: {} Exit RegisterPartnerController renewApiKey", requestUUID);
        return new ResponseEntity<>(registerPartnerResponse, HttpStatus.OK);
    }

    private void validate(PartnerInfo partnerInfo, boolean isRenew) throws PartnerException {

        if (!isRenew) {
            partnerInfo.setPartner(utility.trimWhiteSpace(partnerInfo.getPartner()));
            if (partnerInfo.getPartner() == null) {
                throw new PartnerException("Enter valid partner name");
            }
            if (!utility.hasMobileNumberValid(partnerInfo.getContactNumber())) {
                throw new PartnerException("Invalid mobile number");
            }
            if (!utility.hasAllDigits(partnerInfo.getContactNumber())) {
                throw new PartnerException("Mobile number must contains digits only");
            }
            if (!utility.isEmailValidate(partnerInfo.getEmail())) {
                throw new PartnerException("Email is not valid");
            }
        }

        partnerInfo.setUserName(utility.trimWhiteSpace(partnerInfo.getUserName()));
        if (partnerInfo.getUserName() == null) {
            throw new PartnerException("Enter valid userName");
        }
        partnerInfo.setUserName(utility.userNameLengthCheck(partnerInfo.getUserName()));
        if (partnerInfo.getUserName() == null) {
            throw new PartnerException("UserName cannot be less than 3 characters");
        }

        partnerInfo.setPassword(utility.trimWhiteSpace(partnerInfo.getPassword()));
        if (partnerInfo.getPassword() == null) {
            throw new PartnerException("Enter valid password");
        }

        partnerInfo.setPassword(utility.userPaswordCheck(partnerInfo.getPassword()));
        if (partnerInfo.getPassword() == null) {
            throw new PartnerException("Password cannot be less than 8 characters");
        }

    }

    private StringBuilder validateMandatoryFields(PartnerInfo partnerInfo, boolean isRenew) {

        Map<String, Object> mandatoryFields = new HashMap<>();

        if (!isRenew) {
            mandatoryFields.put("partner", partnerInfo.getPartner());
            mandatoryFields.put("contactNumber", partnerInfo.getContactNumber());
            mandatoryFields.put("email", partnerInfo.getEmail());
        }
        mandatoryFields.put("userName", partnerInfo.getUserName());
        mandatoryFields.put("password", partnerInfo.getPassword());

        return utility.validateMandField(mandatoryFields);
    }

}
