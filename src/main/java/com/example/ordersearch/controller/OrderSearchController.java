package com.example.ordersearch.controller;

import com.example.ordersearch.dto.SearchResponse;
import com.example.ordersearch.model.Order;
import com.example.ordersearch.service.OrderSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderSearchController {
    private final OrderSearchService orderSearchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchOrders(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<Order> results = orderSearchService.searchOrders(q, fromDate, toDate, page, size);
        
        SearchResponse response = new SearchResponse();
        response.setOrders(results);
        return ResponseEntity.ok(response);
    }
} 