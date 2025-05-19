package com.example.ordersearch.controller;

import com.example.ordersearch.dto.SearchResponse;
import com.example.ordersearch.model.Order;
import com.example.ordersearch.service.OrderSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderSearchController {
    private final OrderSearchService orderSearchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchOrders(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            log.debug("Searching with params: searchTerm={}, fromDate={}, toDate={}, page={}, size={}", 
                    searchTerm, fromDate, toDate, page, size);
            
            List<Order> results = orderSearchService.searchOrders(searchTerm, fromDate, toDate, page, size);
            SearchResponse response = new SearchResponse();
            response.setOrders(results);
            response.setTotal(results.size());
            
            log.debug("Found {} results", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching orders", e);
            return ResponseEntity.badRequest().build();
        }
    }

} 