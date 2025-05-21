package com.example.ordersearch.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class SearchRequestMultiField {
    private String id;
    private String code;
    private String bookingCode;
    private String phoneNumber;
    private String customerName;
    private String customerEmail;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date fromCreatedDate;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date toCreatedDate;

    private int page = 0;
    private int pageSize = 20;
}
