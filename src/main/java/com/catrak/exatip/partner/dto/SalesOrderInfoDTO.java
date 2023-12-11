package com.catrak.exatip.partner.dto;

import java.util.List;

import lombok.Data;

@Data
public class SalesOrderInfoDTO {

    private Header header;

    private List<DeviceDetail> deviceDetails;

}
