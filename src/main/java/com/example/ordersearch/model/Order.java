package com.example.ordersearch.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Document(indexName = "orders")
@Setting(settingPath = "vietnamese-analyzer.json")
@Data
public class Order {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String bookingCode;

    @Field(type = FieldType.Text, analyzer = "vietnamese")
    private String customerName;

    @Field(type = FieldType.Keyword)
    private String customerEmail;

    @Field(type = FieldType.Keyword)
    private String orderDate;

    @Field(type = FieldType.Double)
    private double totalAmount;

    @Field(type = FieldType.Keyword)
    private String status;
} 