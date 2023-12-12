package com.catrak.exatip.partner.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
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
import com.catrak.exatip.partner.dto.SalesOrderInfoDTO;
import com.catrak.exatip.partner.exception.PartnerException;
import com.catrak.exatip.partner.service.SalesOrderService;
import com.catrak.exatip.partner.util.JwtTokenValidator;
import com.catrak.exatip.partner.util.Utility;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/salesorder")
@Api(value = "partner", description = "the partner API")
public class SalesOrderController {
    private static final Logger log = LoggerFactory.getLogger(SalesOrderController.class);

    @Autowired
    public SalesOrderService salesOrderService;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Autowired
    private Utility utility;

    @ApiOperation(value = "Create sales order in catrack", nickname = "save", notes = "process and save sales order from fishbowl in catrack", response = String.class, tags = {})
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    @PostMapping("")
    public ResponseEntity<?> save(@ApiParam(value = "") @Valid @RequestBody String salesOrderPayload,
            HttpServletRequest request) {

        String requestUUID = UUID.randomUUID().toString();

        log.info("RequestUUID: {} Inside SalesOrderController save", requestUUID);

        Gson g = new Gson();

        SalesOrderInfoDTO salesOrderInfoDTO = g.fromJson(salesOrderPayload, SalesOrderInfoDTO.class);

        StringBuilder validateField = validateMandatoryFields(salesOrderInfoDTO);

        if (validateField.length() > 0) {
            return new ResponseEntity<>(
                    new JsonResponseDTO<>(validateField.toString(), false, JsonResponseDTO.BAD_REQUEST), HttpStatus.OK);
        }

        validate(salesOrderInfoDTO);

        String username = "";
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            username = jwtTokenValidator.extractUsername(token);
        }

        salesOrderInfoDTO = salesOrderService.save(salesOrderInfoDTO, username, requestUUID);

        String salesOrderNumber = "{" + "\"salesOrderNumber\":" + salesOrderInfoDTO.getHeader().getSalesOrderNumber()
                + "}";

        String registerPartnerResponse = g.toJson(new JsonResponseDTO<>(salesOrderNumber,
                "Sales order created successfully", true, JsonResponseDTO.CREATED));

        log.info("RequestUUID: {} Exit SalesOrderController save", requestUUID);
        return new ResponseEntity<>(registerPartnerResponse, HttpStatus.OK);
    }

    private void validate(SalesOrderInfoDTO salesOrderInfoDTO) throws PartnerException {
        salesOrderInfoDTO.getHeader()
                .setCustomerName(utility.trimWhiteSpace(salesOrderInfoDTO.getHeader().getCustomerName()));
    }

    private StringBuilder validateMandatoryFields(SalesOrderInfoDTO salesOrderInfoDTO) {
        Map<String, Object> mandatoryFields = new HashMap<>();
        if (salesOrderInfoDTO.getHeader() == null) {
            throw new PartnerException("header cannot be null");
        }
        mandatoryFields.put("salesOrderNumber", salesOrderInfoDTO.getHeader().getSalesOrderNumber());
        mandatoryFields.put("can", salesOrderInfoDTO.getHeader().getCan());
        mandatoryFields.put("customerName", salesOrderInfoDTO.getHeader().getCustomerName());
        if (salesOrderInfoDTO.getDeviceDetails() == null) {
            throw new PartnerException("deviceDetails cannot be null");
        }
        return utility.validateMandField(mandatoryFields);
    }

}
