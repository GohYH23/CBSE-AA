package com.inventorymanagement.SalesOrder.service;

import com.inventorymanagement.SalesOrder.dto.*;

import java.util.List;

public interface SalesOrderService {
    SalesOrderDetailDto create(SalesOrderCreateRequest request);
    SalesOrderDetailDto update(Long id, SalesOrderUpdateRequest request);
    void delete(Long id, String deletedById);

    List<SalesOrderListDto> list();
    SalesOrderDetailDto getById(Long id);

    List<SalesOrderStatusDto> getStatusList();

    void recalculate(Long salesOrderId);
}
