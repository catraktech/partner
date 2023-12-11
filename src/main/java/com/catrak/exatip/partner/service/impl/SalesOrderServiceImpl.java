package com.catrak.exatip.partner.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catrak.exatip.commonlib.entity.Product;
import com.catrak.exatip.commonlib.entity.SalesOrderInfo;
import com.catrak.exatip.partner.dto.DeviceDetail;
import com.catrak.exatip.partner.dto.SalesOrderInfoDTO;
import com.catrak.exatip.partner.exception.PartnerException;
import com.catrak.exatip.partner.repository.ProductRepository;
import com.catrak.exatip.partner.repository.SalesOrderInfoRepository;
import com.catrak.exatip.partner.service.SalesOrderService;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private static final Logger log = LoggerFactory.getLogger(SalesOrderServiceImpl.class);

    @Autowired
    private SalesOrderInfoRepository salesOrderInfoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public SalesOrderInfoDTO save(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) throws PartnerException {
        log.info("RequestUUID: {} Inside SalesOrderServiceImpl save", requestUUID);
        List<SalesOrderInfo> salesOrderInfos = new ArrayList<>();
        List<Product> products = productRepository.findAll();

        Long salesOrderNumber = salesOrderInfoDTO.getHeader().getSalesOrderNumber();
        boolean isSalesOrderNumberExist = salesOrderInfoRepository.existsBySalesOrderNumber(salesOrderNumber);
        if (isSalesOrderNumberExist) {
            throw new PartnerException("salesOrderNumber " + salesOrderNumber + " already exist");
        }

        Integer can = salesOrderInfoDTO.getHeader().getCan();
        boolean isCanExist = salesOrderInfoRepository.existsByCan(can);
        if (isCanExist) {
            throw new PartnerException("can " + can + " already exist");
        }

        List<String> productIds = products.stream().map(Product::getProductId).collect(Collectors.toList());
        salesOrderInfoDTO.getDeviceDetails().forEach(s -> {
            SalesOrderInfo salesOrderInfo = new SalesOrderInfo();
            salesOrderInfo.setSalesOrderNumber(salesOrderInfoDTO.getHeader().getSalesOrderNumber());
            salesOrderInfo.setCan(salesOrderInfoDTO.getHeader().getCan());
            salesOrderInfo.setCustomerName(salesOrderInfoDTO.getHeader().getCustomerName());
            validate(s);
            if (!validateProductIds(productIds, s.getCatrakProductID())) {
                throw new PartnerException("invalid productId " + s.getCatrakProductID());
            }
            salesOrderInfo.setProductId(s.getCatrakProductID());
            salesOrderInfo.setImei(s.getImei());
            salesOrderInfo.setSerialNumber(s.getDsn());
            salesOrderInfo.setManufacturer(s.getManufacturer());
            salesOrderInfo.setModel(s.getModel());
            salesOrderInfo.setTrackingNumber(s.getTrackingNumber());
            salesOrderInfo.setSalesChannel(s.getSalesChannel());
            salesOrderInfo.setLastModifiedDatetime(new Timestamp(System.currentTimeMillis()));
            salesOrderInfos.add(salesOrderInfo);
        });
        salesOrderInfoRepository.saveAll(salesOrderInfos);
        log.info("RequestUUID: {} Exit SalesOrderServiceImpl save", requestUUID);
        return salesOrderInfoDTO;
    }

    private boolean validateProductIds(List<String> productIds, String productId) {
        return productIds.contains(productId);
    }

    private void validate(DeviceDetail deviceDetail) {
        if (deviceDetail.getDsn() == null) {
            throw new PartnerException("dsn cannot be null");
        } else if (deviceDetail.getImei() == null) {
            throw new PartnerException("imei cannot be null");
        } else if (deviceDetail.getManufacturer() == null) {
            throw new PartnerException("manufacturer cannot be null");
        } else if (deviceDetail.getModel() == null) {
            throw new PartnerException("model cannot be null");
        }
    }

}
