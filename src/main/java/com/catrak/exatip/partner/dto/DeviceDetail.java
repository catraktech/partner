package com.catrak.exatip.partner.dto;

import lombok.Data;

@Data
public class DeviceDetail {
    private Long imei;
    private Long dsn;
    private String manufacturer;
    private String model;
    private String catrakProductID;
    private String salesChannel;
    private String trackingNumber;
}
