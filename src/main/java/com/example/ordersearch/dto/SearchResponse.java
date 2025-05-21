package com.example.ordersearch.dto;

import com.example.ordersearch.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private List<Order> orders;
    private long total;
} 