package com.example.ordersearch.dto;

import lombok.Data;
import java.util.Date;

@Data
public class SearchRequest {
    private String searchTerm; // For searching across multiple fields
    private Date fromDate;
    private Date toDate;
    private int page = 0;
    private int size = 20;
} 