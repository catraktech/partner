package com.catrak.exatip.partner.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catrak.exatip.commonlib.constant.ManufacturerIdValues;
import com.catrak.exatip.commonlib.constant.StatusValues;
import com.catrak.exatip.commonlib.entity.Device;
import com.catrak.exatip.commonlib.entity.DeviceInventory;
import com.catrak.exatip.commonlib.entity.Manufacturer;
import com.catrak.exatip.commonlib.entity.Organization;
import com.catrak.exatip.commonlib.entity.PartnerInfo;
import com.catrak.exatip.commonlib.entity.Product;
import com.catrak.exatip.commonlib.entity.SalesOrderInfo;
import com.catrak.exatip.commonlib.entity.Status;
import com.catrak.exatip.partner.dto.DeviceDetail;
import com.catrak.exatip.partner.dto.SalesOrderInfoDTO;
import com.catrak.exatip.partner.exception.PartnerException;
import com.catrak.exatip.partner.repository.DeviceInventoryRepository;
import com.catrak.exatip.partner.repository.DeviceRepository;
import com.catrak.exatip.partner.repository.ManufacturerRepository;
import com.catrak.exatip.partner.repository.OrganizationRepository;
import com.catrak.exatip.partner.repository.PartnerInfoRepository;
import com.catrak.exatip.partner.repository.ProductRepository;
import com.catrak.exatip.partner.repository.SalesOrderInfoRepository;
import com.catrak.exatip.partner.repository.StatusRepository;
import com.catrak.exatip.partner.service.SalesOrderService;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private static final Logger log = LoggerFactory.getLogger(SalesOrderServiceImpl.class);

    @Autowired
    private SalesOrderInfoRepository salesOrderInfoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private DeviceInventoryRepository deviceInventoryRepository;

    @Autowired
    private PartnerInfoRepository partnerInfoRepository;

    @Transactional
    @Override
    public SalesOrderInfoDTO save(SalesOrderInfoDTO salesOrderInfoDTO, String userName, String requestUUID)
            throws PartnerException {

        log.info("RequestUUID: {} started creating sales-order for salesOrderNumber {} ", requestUUID,
                salesOrderInfoDTO.getHeader().getSalesOrderNumber());

        List<SalesOrderInfo> salesOrderInfos = new ArrayList<>();

        List<Product> products = productRepository.findAll();

        validateSalesOrderNumberExist(salesOrderInfoDTO, requestUUID);

        Organization organization = isCanExistInCatrack(salesOrderInfoDTO, requestUUID);

        validateImeiExist(salesOrderInfoDTO, requestUUID);

        validateDsnExist(salesOrderInfoDTO, requestUUID);

        validateTrackingNumberExist(salesOrderInfoDTO, requestUUID);

        Optional<PartnerInfo> partnerOptional = partnerInfoRepository.findByUserName(userName);

        if (!partnerOptional.isPresent()) {
            log.error("RequestUUID: {} invalid userName", requestUUID);
            throw new PartnerException("invalid userName");
        }

        List<String> productIds = products.stream().map(Product::getProductId).collect(Collectors.toList());
        salesOrderInfoDTO.getDeviceDetails().forEach(s -> {
            SalesOrderInfo salesOrderInfo = new SalesOrderInfo();
            salesOrderInfo.setSalesOrderNumber(salesOrderInfoDTO.getHeader().getSalesOrderNumber());
            salesOrderInfo.setCan(salesOrderInfoDTO.getHeader().getCan());
            salesOrderInfo.setCustomerName(salesOrderInfoDTO.getHeader().getCustomerName());
            validate(s, requestUUID);
            if (s.getImei().toString().length() < 15 || s.getImei().toString().length() >= 16) {
                log.error("RequestUUID: {} imei must be of 15 digit {} ", requestUUID, s.getImei());
                throw new PartnerException("imei must be of 15 digit " + s.getImei());
            }

            List<String> validModels = Arrays.asList("XT2100", "ST4500");
            List<String> models = salesOrderInfoDTO.getDeviceDetails().stream().map(DeviceDetail::getModel)
                    .collect(Collectors.toList());
            List<String> notPresentElements = models.stream().filter(element -> !validModels.contains(element))
                    .collect(Collectors.toList());

            if (!notPresentElements.isEmpty()) {
                log.error("RequestUUID: {}  {} are not valid models ", requestUUID, notPresentElements);
                throw new PartnerException(notPresentElements + " are not valid models");
            }

            if (s.getModel().equals("XT2100") && !s.getManufacturer().equals("Sensata")) {
                log.error("RequestUUID: {} valid manufacturer is sensata for dsn {} ", requestUUID, s.getDsn());
                throw new PartnerException("valid manufacturer is sensata for dsn " + s.getDsn());
            } else if (s.getModel().equals("ST4500") && !s.getManufacturer().equals("Suntech")) {
                log.error("RequestUUID: {} valid manufacturer is suntech for dsn {} ", requestUUID, s.getDsn());
                throw new PartnerException("valid manufacturer is suntech for dsn " + s.getDsn());
            }

            if (s.getModel().equals("XT2100") && s.getDsn().toString().length() != 9) {
                log.error("RequestUUID: {} sensata dsn must be of 9 digit  {} ", requestUUID, s.getDsn());
                throw new PartnerException("sensata dsn must be of 9 digit " + s.getDsn());
            } else if (s.getModel().equals("ST4500") && s.getDsn().toString().length() != 10) {
                log.error("RequestUUID: {} suntech dsn must be of 10 digit {} ", requestUUID, s.getDsn());
                throw new PartnerException("suntech dsn must be of 10 digit " + s.getDsn());
            }

            if (!validateProductIds(productIds, s.getCatrakProductID())) {
                log.error("RequestUUID: {} invalid productId {} ", requestUUID, s.getCatrakProductID());
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

        log.info("RequestUUID: {} saving sales-order in sales-order-info table ", requestUUID);
        salesOrderInfoRepository.saveAll(salesOrderInfos);
        log.info("RequestUUID: {} saved sales-order in sales-order-info table ", requestUUID);

        saveDevice(salesOrderInfoDTO, organization, partnerOptional.get(), requestUUID);

        log.info("RequestUUID: {} completed creating sales-order for salesOrderNumber {} ", requestUUID,
                salesOrderInfoDTO.getHeader().getSalesOrderNumber());
        return salesOrderInfoDTO;
    }

    private void saveDevice(SalesOrderInfoDTO salesOrderInfoDTO, Organization organization, PartnerInfo partnerInfo,
            String requestUUID) {
        Optional<Manufacturer> sensataManufacturer = manufacturerRepository.findByName("SENSATA");
        Optional<Manufacturer> suntechManufacturer = manufacturerRepository.findByName("SUNTECH");
        salesOrderInfoDTO.getDeviceDetails().forEach(s -> {
            try {
                DeviceInventory deviceInventory = new DeviceInventory();

                if (s.getManufacturer().equalsIgnoreCase("Sensata")) {
                    Manufacturer manufacturer = sensataManufacturer.get();
                    deviceInventory.setManufacturer(manufacturer);
                } else if (s.getManufacturer().equalsIgnoreCase("Suntech")) {
                    Manufacturer manufacturer = suntechManufacturer.get();
                    deviceInventory.setManufacturer(manufacturer);
                }
                Status pendingStatus = statusRepository.findById(StatusValues.PENDING.id).orElse(null);
                Timestamp lastModifiedDatetime = new Timestamp(System.currentTimeMillis());
                deviceInventory.setImei(String.valueOf(s.getImei()));
                deviceInventory.setSerialNo(String.valueOf(s.getDsn()));
                deviceInventory.setDeviceModel(s.getModel());
                deviceInventory.setSalesChannel(s.getSalesChannel());
                deviceInventory.setStatus(pendingStatus);
                deviceInventory.setLastModifiedUserId(partnerInfo.getPartnerId());
                deviceInventory.setLastModifiedDatetime(lastModifiedDatetime);
                log.info("RequestUUID: {} saving sales-order in device_inventory table ", requestUUID);
                deviceInventoryRepository.save(deviceInventory);
                log.info("RequestUUID: {} saved sales-order in device_inventory table ", requestUUID);

                Long deviceId = deviceInventory.getDeviceId();
                Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
                Device device = null;
                if (!deviceOptional.isPresent()) {
                    device = new Device();
                } else {
                    device = deviceOptional.get();
                }

                device.setDeviceId(deviceId);
                device.setImei(deviceInventory.getImei());
                device.setLastModifiedDatetime(lastModifiedDatetime);
                device.setOrganization(organization);
                device.setSerialNo(deviceInventory.getSerialNo());
                device.setStatus(pendingStatus);
                device.setProductId(s.getCatrakProductID());
                device.setSalesChannel(deviceInventory.getSalesChannel());
                device.setDeviceModel(s.getModel());
                Manufacturer manufacturer = new Manufacturer();
                if (deviceInventory.getSerialNo().length() == 9) {
                    manufacturer.setManufacturerId(ManufacturerIdValues.SENSATA.id);
                } else {
                    manufacturer.setManufacturerId(ManufacturerIdValues.SUNTECH.id);
                }
                device.setManufacturer(manufacturer);
                log.info("RequestUUID: {} saving sales-order in device table ", requestUUID);
                deviceRepository.save(device);
                log.info("RequestUUID: {} saved sales-order in device table ", requestUUID);
            } catch (Exception e) {
                log.error("RequestUUID: {} exception due to {}", requestUUID, e.getMessage());
                throw new PartnerException(e.getMessage());
            }
        });
    }

    private void validateSalesOrderNumberExist(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) {
        Long salesOrderNumber = salesOrderInfoDTO.getHeader().getSalesOrderNumber();
        boolean isSalesOrderNumberExist = salesOrderInfoRepository.existsBySalesOrderNumber(salesOrderNumber);
        if (isSalesOrderNumberExist) {
            log.error("RequestUUID: {} salesOrderNumber {} already exist", requestUUID, salesOrderNumber);
            throw new PartnerException("salesOrderNumber " + salesOrderNumber + " already exist");
        }
    }

    private Organization isCanExistInCatrack(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) {
        Integer can = salesOrderInfoDTO.getHeader().getCan();
        Optional<Organization> orOptional = organizationRepository.findByCan(can);
        if (!orOptional.isPresent()) {
            log.error("RequestUUID: {} can {} not exist in catrak", requestUUID, can);
            throw new PartnerException("can " + can + " not exist in catrak");
        }
        return orOptional.get();
    }

    private void validateImeiExist(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) {
        List<Long> dsns = salesOrderInfoRepository.findAllDsn();
        List<Long> dsnsFB = salesOrderInfoDTO.getDeviceDetails().stream().map(DeviceDetail::getDsn)
                .collect(Collectors.toList());
        if (dsns.stream().anyMatch(dsnsFB::contains)) {
            log.error("RequestUUID: {} dsn already exist", requestUUID);
            throw new PartnerException("dsn already exist");
        }
    }

    private void validateDsnExist(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) {
        List<Long> imeis = salesOrderInfoRepository.findAllImeis();
        List<Long> imeisFB = salesOrderInfoDTO.getDeviceDetails().stream().map(DeviceDetail::getImei)
                .collect(Collectors.toList());
        if (imeis.stream().anyMatch(imeisFB::contains)) {
            log.error("RequestUUID: {} imei already exist", requestUUID);
            throw new PartnerException("imei already exist");
        }
    }

    private void validateTrackingNumberExist(SalesOrderInfoDTO salesOrderInfoDTO, String requestUUID) {
        List<String> trackinNumbers = salesOrderInfoRepository.findAllTrackingNumber();
        List<String> trackinNumbersFB = salesOrderInfoDTO.getDeviceDetails().stream()
                .map(DeviceDetail::getTrackingNumber).collect(Collectors.toList());
        if (trackinNumbers.stream().anyMatch(trackinNumbersFB::contains)) {
            log.error("RequestUUID: {} tracking number already exist", requestUUID);
            throw new PartnerException("tracking number already exist");
        }
    }

    private boolean validateProductIds(List<String> productIds, String productId) {
        return productIds.contains(productId);
    }

    private void validate(DeviceDetail deviceDetail, String requestUUID) {
        if (deviceDetail.getDsn() == null) {
            log.error("RequestUUID: {} dsn cannot be null", requestUUID);
            throw new PartnerException("dsn cannot be null");
        } else if (deviceDetail.getImei() == null) {
            log.error("RequestUUID: {} imei cannot be null", requestUUID);
            throw new PartnerException("imei cannot be null");
        } else if (deviceDetail.getManufacturer() == null) {
            log.error("RequestUUID: {} manufacturer cannot be null", requestUUID);
            throw new PartnerException("manufacturer cannot be null");
        } else if (deviceDetail.getModel() == null) {
            log.error("RequestUUID: {} model cannot be null", requestUUID);
            throw new PartnerException("model cannot be null");
        }
    }

}
