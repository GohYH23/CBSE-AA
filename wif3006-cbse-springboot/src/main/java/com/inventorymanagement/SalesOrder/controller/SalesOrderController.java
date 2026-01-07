package com.inventorymanagement.SalesOrder.controller;

import com.inventorymanagement.SalesOrder.dto.*;
import com.inventorymanagement.SalesOrder.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    public SalesOrderDetailDto create(@Valid @RequestBody SalesOrderCreateRequest request) {
        return salesOrderService.create(request);
    }

    @PutMapping("/{id}")
    public SalesOrderDetailDto update(@PathVariable Long id, @Valid @RequestBody SalesOrderUpdateRequest request) {
        return salesOrderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam(required = false) String deletedById) {
        salesOrderService.delete(id, deletedById);
    }

    @GetMapping
    public List<SalesOrderListDto> list() {
        return salesOrderService.list();
    }

    @GetMapping("/{id}")
    public SalesOrderDetailDto getById(@PathVariable Long id) {
        return salesOrderService.getById(id);
    }

    @GetMapping("/statuses")
    public List<SalesOrderStatusDto> statusList() {
        return salesOrderService.getStatusList();
    }
}
