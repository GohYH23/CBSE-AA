package com.inventorymanagement.SalesOrder.service;

import com.inventorymanagement.SalesOrder.dto.*;
import com.inventorymanagement.SalesOrder.entity.*;
import com.inventorymanagement.SalesOrder.repository.SalesOrderItemRepository;
import com.inventorymanagement.SalesOrder.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final NumberSequenceService numberSequenceService;

    // Simple tax percentage placeholder (since your original code reads Tax.Percentage)
    // If your team already has a Tax module, replace this with real lookup.
    private static final double DEFAULT_TAX_PERCENT = 6.0;

    public SalesOrderServiceImpl(
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            NumberSequenceService numberSequenceService
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.numberSequenceService = numberSequenceService;
    }

    @Override
    @Transactional
    public SalesOrderDetailDto create(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();
        order.setCreatedById(request.getCreatedById());
        order.setNumber(numberSequenceService.generateSoNumber());
        order.setOrderDate(request.getOrderDate());
        order.setOrderStatus(SalesOrderStatus.valueOf(request.getOrderStatus()));
        order.setDescription(request.getDescription());
        order.setCustomerId(request.getCustomerId());
        order.setTaxId(request.getTaxId());

        // Add items
        for (SalesOrderCreateRequest.Item it : request.getItems()) {
            SalesOrderItem item = new SalesOrderItem();
            item.setProductId(it.getProductId());
            item.setQuantity(it.getQuantity());
            item.setUnitPrice(it.getUnitPrice());
            item.setTotal(it.getUnitPrice() * it.getQuantity());
            order.addItem(item);
        }

        SalesOrder saved = salesOrderRepository.save(order);
        recalculate(saved.getId());
        return getById(saved.getId());
    }

    @Override
    @Transactional
    public SalesOrderDetailDto update(Long id, SalesOrderUpdateRequest request) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found: " + id));

        order.setUpdatedById(request.getUpdatedById());
        order.setOrderDate(request.getOrderDate());
        order.setOrderStatus(SalesOrderStatus.valueOf(request.getOrderStatus()));
        order.setDescription(request.getDescription());
        order.setCustomerId(request.getCustomerId());
        order.setTaxId(request.getTaxId());

        salesOrderRepository.save(order);
        recalculate(order.getId());
        return getById(order.getId());
    }

    @Override
    @Transactional
    public void delete(Long id, String deletedById) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found: " + id));

        // If you want soft delete, add an "isDeleted" boolean in entity instead.
        order.setUpdatedById(deletedById);
        salesOrderRepository.delete(order);
    }

    @Override
    public List<SalesOrderListDto> list() {
        return salesOrderRepository.findAll().stream().map(order -> {
            SalesOrderListDto dto = new SalesOrderListDto();
            dto.setId(order.getId());
            dto.setNumber(order.getNumber());
            dto.setOrderDate(order.getOrderDate());
            dto.setOrderStatus(order.getOrderStatus().name());
            dto.setOrderStatusName(order.getOrderStatus().name()); // friendly name optional
            dto.setDescription(order.getDescription());
            dto.setCustomerId(order.getCustomerId());
            dto.setTaxId(order.getTaxId());
            dto.setBeforeTaxAmount(order.getBeforeTaxAmount());
            dto.setTaxAmount(order.getTaxAmount());
            dto.setAfterTaxAmount(order.getAfterTaxAmount());
            return dto;
        }).toList();
    }

    @Override
    public SalesOrderDetailDto getById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found: " + id));

        SalesOrderDetailDto dto = new SalesOrderDetailDto();
        dto.setId(order.getId());
        dto.setNumber(order.getNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus().name());
        dto.setDescription(order.getDescription());
        dto.setCustomerId(order.getCustomerId());
        dto.setTaxId(order.getTaxId());
        dto.setBeforeTaxAmount(order.getBeforeTaxAmount());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setAfterTaxAmount(order.getAfterTaxAmount());

        dto.setItems(order.getItems().stream().map(item -> {
            SalesOrderDetailDto.Item it = new SalesOrderDetailDto.Item();
            it.setId(item.getId());
            it.setProductId(item.getProductId());
            it.setQuantity(item.getQuantity());
            it.setUnitPrice(item.getUnitPrice());
            it.setTotal(item.getTotal());
            return it;
        }).toList());

        return dto;
    }

    @Override
    public List<SalesOrderStatusDto> getStatusList() {
        return List.of(SalesOrderStatus.values()).stream()
                .map(s -> new SalesOrderStatusDto(String.valueOf(s.ordinal()), s.name()))
                .toList();
    }

    @Override
    @Transactional
    public void recalculate(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found: " + salesOrderId));

        double beforeTax = order.getItems().stream()
                .mapToDouble(i -> i.getTotal() == null ? 0.0 : i.getTotal())
                .sum();

        double tax = beforeTax * DEFAULT_TAX_PERCENT / 100.0;
        double afterTax = beforeTax + tax;

        order.setBeforeTaxAmount(beforeTax);
        order.setTaxAmount(tax);
        order.setAfterTaxAmount(afterTax);

        salesOrderRepository.save(order);
    }
}
