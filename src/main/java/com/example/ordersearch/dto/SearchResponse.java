package com.example.ordersearch.dto;

import com.example.ordersearch.model.Order;
import lombok.Data;
import java.util.List;

@Data
public class SearchResponse {
    private List<Order> orders;
    private long total;
} 