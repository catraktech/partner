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

import com.catrak.exatip.commonlib.constant.ResponseMessage;
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

    @PostMapping("/test")
    public ResponseEntity<?> test() {
        return new ResponseEntity<>("Hello JWT", HttpStatus.OK);
    }

    @ApiOperation(value = "Register partner with CATrak", nickname = "register", notes = "partner registration with catrak", response = String.class, tags = {})
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    @PostMapping("")
    public ResponseEntity<?> registerPartner(@ApiParam(value = "") @Valid @RequestBody String registerPartnerPayload) {

        String requestUUID = UUID.randomUUID().toString();

        try {
            log.info("RequestUUID: {} Inside RegisterPartnerController registerPartner", requestUUID);
            Gson g = new Gson();
            PartnerInfo partnerInfo = g.fromJson(registerPartnerPayload, PartnerInfo.class);

            StringBuilder validateField = validateMandatoryFields(partnerInfo);

            if (validateField.length() > 0) {
                return new ResponseEntity<>(
                        new JsonResponseDTO<>(validateField.toString(), false, JsonResponseDTO.BAD_REQUEST),
                        HttpStatus.OK);
            }

            validate(partnerInfo);

            partnerInfo = registerPartnerService.registerPartner(partnerInfo, requestUUID);

            String apiKey = "{" + "\"apiKey\":\"" + partnerInfo.getApiKey() + "\"}";

            String registerPartnerResponse = g.toJson(
                    new JsonResponseDTO<>(apiKey, "Partner registration is successful", true, JsonResponseDTO.CREATED));

            log.info("RequestUUID: {} Exit RegisterPartnerController registerPartner", requestUUID);
            return new ResponseEntity<>(registerPartnerResponse, HttpStatus.OK);
        } catch (PartnerException e) {
            log.error("RequestUUID: {} exception due to {}", requestUUID, e.getMessage());
            return new ResponseEntity<>(new JsonResponseDTO<>(e.getMessage(), false, JsonResponseDTO.BAD_REQUEST),
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("RequestUUID: {} exception due to {}", requestUUID, e.getMessage());
            return new ResponseEntity<>(new JsonResponseDTO<>(ResponseMessage.INTERNAL_SERVER_ERROR, false,
                    JsonResponseDTO.INTERNAL_SERVER_ERROR), HttpStatus.OK);
        }
    }

    private void validate(PartnerInfo partnerInfo) throws PartnerException {
        partnerInfo.setPartner(utility.trimWhiteSpace(partnerInfo.getPartner()));
        if (partnerInfo.getPartner() == null) {
            throw new PartnerException("Enter valid partner name");
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

    private StringBuilder validateMandatoryFields(PartnerInfo partnerInfo) {
        Map<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("partner", partnerInfo.getPartner());
        mandatoryFields.put("contactNumber", partnerInfo.getContactNumber());
        mandatoryFields.put("email", partnerInfo.getEmail());
        mandatoryFields.put("userName", partnerInfo.getUserName());
        mandatoryFields.put("password", partnerInfo.getPassword());

        StringBuilder validateField = utility.validateMandField(mandatoryFields);
        return validateField;
    }

}
